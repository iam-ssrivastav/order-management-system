package com.enterprise.order.service;

import com.enterprise.order.dto.OrderRequest;
import com.enterprise.order.model.OrderStatus;
import com.enterprise.order.kafka.OrderStatusEventProducer;
import com.enterprise.order.kafka.OrderCancellationProducer;
import com.enterprise.order.dto.OrderResponse;
import com.enterprise.order.entity.Order;
import com.enterprise.order.kafka.OrderEventProducer;
import com.enterprise.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final OrderStatusEventProducer orderStatusEventProducer;
    private final OrderCancellationProducer orderCancellationProducer;

    public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer,
            OrderStatusEventProducer orderStatusEventProducer, OrderCancellationProducer orderCancellationProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
        this.orderStatusEventProducer = orderStatusEventProducer;
        this.orderCancellationProducer = orderCancellationProducer;
    }

    @Transactional
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackPlaceOrder")
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setProductId(orderRequest.getProductId());
        order.setQuantity(orderRequest.getQuantity());
        order.setPrice(orderRequest.getPrice());
        order.setCustomerId(orderRequest.getCustomerId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setUpdatedAt(java.time.LocalDateTime.now());

        orderRepository.save(order);
        log.info("Order placed successfully: {}", order.getId());

        OrderResponse orderResponse = mapToOrderResponse(order);
        orderEventProducer.sendMessage(orderResponse);

        return orderResponse;
    }

    public OrderResponse fallbackPlaceOrder(OrderRequest orderRequest, RuntimeException runtimeException) {
        log.error("Cannot place order. Inventory service is down or error occurred: {}", runtimeException.getMessage());
        OrderResponse response = new OrderResponse();
        response.setStatus(OrderStatus.CANCELLED);
        response.setCustomerId(orderRequest.getCustomerId());
        return response;
    }

    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponse getOrder(Long orderId) {
        log.info("Fetching order from database: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        // publish status change event
        orderStatusEventProducer.sendStatusChanged(orderId, oldStatus, newStatus);
        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Validate order can be cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new RuntimeException("Cannot cancel order in " + order.getStatus() + " status");
        }

        // Update order status
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order {} cancelled successfully. Previous status: {}", orderId, oldStatus);

        // Publish cancellation event for compensating transactions
        orderCancellationProducer.publishOrderCancellation(
                orderId,
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                reason != null ? reason : "Customer requested");

        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getPrice(),
                order.getCustomerId(),
                order.getStatus(),
                order.getCreatedAt());
    }
}
