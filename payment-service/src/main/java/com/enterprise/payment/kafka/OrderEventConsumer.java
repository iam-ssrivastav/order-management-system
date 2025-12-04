package com.enterprise.payment.kafka;

import com.enterprise.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class OrderEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orders", groupId = "payment-group")
    public void consumeOrderEvent(String message) {
        try {
            logger.info("Received order event: {}", message);
            Map<String, Object> orderData = objectMapper.readValue(message, Map.class);

            Long orderId = ((Number) orderData.get("id")).longValue();
            String customerId = (String) orderData.get("customerId");
            BigDecimal price = new BigDecimal(orderData.get("price").toString());
            Integer quantity = (Integer) orderData.get("quantity");
            BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));

            // Process payment
            paymentService.processPayment(orderId, customerId, totalAmount, "CREDIT_CARD");
        } catch (Exception e) {
            logger.error("Error processing order event", e);
        }
    }
}
