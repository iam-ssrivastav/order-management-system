package com.enterprise.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderCancellationProducer {
    private static final Logger logger = LoggerFactory.getLogger(OrderCancellationProducer.class);
    private static final String TOPIC = "order-cancellation-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderCancellationProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCancellation(Long orderId, String customerId, String productId, Integer quantity,
            String reason) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_CANCELLED");
            event.put("orderId", orderId);
            event.put("customerId", customerId);
            event.put("productId", productId);
            event.put("quantity", quantity);
            event.put("reason", reason);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, message);
            logger.info("Published OrderCancelledEvent for order: {}", orderId);
        } catch (Exception e) {
            logger.error("Failed to publish OrderCancelledEvent", e);
        }
    }
}
