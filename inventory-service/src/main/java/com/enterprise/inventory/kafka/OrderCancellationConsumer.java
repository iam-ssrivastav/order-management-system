package com.enterprise.inventory.kafka;

import com.enterprise.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderCancellationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderCancellationConsumer.class);
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    public OrderCancellationConsumer(InventoryService inventoryService, ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-cancellation-events", groupId = "inventory-group")
    public void consumeOrderCancellation(String message) {
        try {
            logger.info("Received order cancellation event: {}", message);
            Map<String, Object> cancellationData = objectMapper.readValue(message, Map.class);

            String productId = (String) cancellationData.get("productId");
            Integer quantity = (Integer) cancellationData.get("quantity");
            Long orderId = ((Number) cancellationData.get("orderId")).longValue();

            // Restore stock (compensating transaction)
            inventoryService.restoreStock(productId, quantity);
            logger.info("Stock restored for order cancellation. Order: {}, Product: {}, Quantity: {}",
                    orderId, productId, quantity);
        } catch (Exception e) {
            logger.error("Error processing order cancellation event", e);
        }
    }
}
