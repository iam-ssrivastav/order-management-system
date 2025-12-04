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

    // ============ MANAGER METHODS ============

    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders (MANAGER/AUDITOR access)");
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    // ============ SUPPORT METHODS ============

    @Transactional
    public OrderResponse requestRefund(Long orderId, String reason) {
        log.info("Refund requested for order: {} by SUPPORT. Reason: {}", orderId, reason);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update status to indicate refund requested
        order.setStatus(OrderStatus.REFUND_REQUESTED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    // ============ FINANCE METHODS ============

    @Transactional
    public OrderResponse processRefund(Long orderId) {
        log.info("Processing refund for order: {} by FINANCE", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.REFUND_REQUESTED) {
            throw new RuntimeException("Order must be in REFUND_REQUESTED status");
        }

        // Update status to refunded
        order.setStatus(OrderStatus.REFUNDED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        // TODO: Trigger payment service to process actual refund

        return mapToOrderResponse(order);
    }

    // ============ WAREHOUSE METHODS ============

    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingNumber) {
        log.info("Shipping order: {} with tracking: {}", orderId, trackingNumber);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.SHIPPED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        // Note: In a real app, you'd have a trackingNumber field in Order entity
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse markDelivered(Long orderId) {
        log.info("Marking order as delivered: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Order must be in SHIPPED status");
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    // ============ AUDITOR METHODS ============

    public String exportOrdersAsCsv() {
        log.info("Exporting all orders as CSV (AUDITOR access)");
        List<Order> orders = orderRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,ProductID,Quantity,Price,CustomerID,Status,CreatedAt\n");

        for (Order order : orders) {
            csv.append(order.getId()).append(",")
                    .append(order.getProductId()).append(",")
                    .append(order.getQuantity()).append(",")
                    .append(order.getPrice()).append(",")
                    .append(order.getCustomerId()).append(",")
                    .append(order.getStatus()).append(",")
                    .append(order.getCreatedAt()).append("\n");
        }

        return csv.toString();
    }
}
