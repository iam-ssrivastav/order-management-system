package com.enterprise.payment.kafka;

import com.enterprise.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderCancellationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderCancellationConsumer.class);
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public OrderCancellationConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-cancellation-events", groupId = "payment-group")
    public void consumeOrderCancellation(String message) {
        try {
            logger.info("Received order cancellation event: {}", message);
            Map<String, Object> cancellationData = objectMapper.readValue(message, Map.class);

            Long orderId = ((Number) cancellationData.get("orderId")).longValue();
            String reason = (String) cancellationData.get("reason");

            // Process refund (compensating transaction)
            paymentService.refundPayment(orderId, reason);
        } catch (Exception e) {
            logger.error("Error processing order cancellation event", e);
        }
    }
}
