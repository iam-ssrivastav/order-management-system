package com.enterprise.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPaymentSuccess(Long orderId, String customerId, BigDecimal amount) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PAYMENT_SUCCESS");
            event.put("orderId", orderId);
            event.put("customerId", customerId);
            event.put("amount", amount);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, message);
            logger.info("Published PaymentSuccessEvent for order: {}", orderId);
        } catch (Exception e) {
            logger.error("Failed to publish PaymentSuccessEvent", e);
        }
    }

    public void publishPaymentFailed(Long orderId, String customerId, String reason) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PAYMENT_FAILED");
            event.put("orderId", orderId);
            event.put("customerId", customerId);
            event.put("reason", reason);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, message);
            logger.info("Published PaymentFailedEvent for order: {}", orderId);
        } catch (Exception e) {
            logger.error("Failed to publish PaymentFailedEvent", e);
        }
    }
}
