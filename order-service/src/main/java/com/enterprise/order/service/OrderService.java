package com.enterprise.order.service;

import com.enterprise.order.dto.OrderRequest;
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

    public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackPlaceOrder")
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setProductId(orderRequest.getProductId());
        order.setQuantity(orderRequest.getQuantity());
        order.setPrice(orderRequest.getPrice());
        order.setCustomerId(orderRequest.getCustomerId());
        order.setStatus("CREATED");
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
        response.setStatus("FAILED");
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
