package com.enterprise.inventory.kafka;

import com.enterprise.inventory.service.InventoryService;
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
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(InventoryService inventoryService, ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-group-v2")
    public void consumeOrderEvent(String message) {
        log.info("Received order event: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String productId = jsonNode.get("productId").asText();
            int quantity = jsonNode.get("quantity").asInt();

            inventoryService.deductStock(productId, quantity);
        } catch (JsonProcessingException e) {
            log.error("Error processing order event", e);
        }
    }
}
