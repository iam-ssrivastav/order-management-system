package com.enterprise.notification.kafka;

import com.enterprise.notification.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void consumeOrderEvent(String message) {
        log.info("Received order event for notification: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String orderId = jsonNode.get("id").asText();
            String customerId = jsonNode.get("customerId").asText();

            notificationService.sendNotification(orderId, customerId);
        } catch (JsonProcessingException e) {
            log.error("Error processing order event", e);
        }
    }
}
