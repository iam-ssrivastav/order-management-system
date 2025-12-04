package com.enterprise.inventory.service;

import com.enterprise.inventory.entity.Inventory;
import com.enterprise.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository inventoryRepository;
    private final com.enterprise.inventory.repository.OutboxRepository outboxRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final String INVENTORY_TOPIC = "inventory-events";

    public InventoryService(InventoryRepository inventoryRepository,
            com.enterprise.inventory.repository.OutboxRepository outboxRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void deductStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
        log.info("Stock deducted for product {}. New quantity: {}", productId, inventory.getQuantity());

        // Save INVENTORY_RESERVED event
        java.util.Map<String, Object> event = new java.util.HashMap<>();
        event.put("productId", productId);
        event.put("quantity", quantity);
        event.put("newQuantity", inventory.getQuantity());

        saveOutboxEvent(productId, "INVENTORY_RESERVED", event, INVENTORY_TOPIC);
    }

    @Transactional
    public Inventory addStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory(null, productId, 0, null));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        Inventory saved = inventoryRepository.save(inventory);

        // Save INVENTORY_ADDED event
        java.util.Map<String, Object> event = new java.util.HashMap<>();
        event.put("productId", productId);
        event.put("quantity", quantity);
        event.put("newQuantity", saved.getQuantity());

        saveOutboxEvent(productId, "INVENTORY_ADDED", event, INVENTORY_TOPIC);

        return saved;
    }

    public Inventory getInventory(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public void restoreStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
        log.info("Stock restored for product {}. New quantity: {} (compensating transaction)",
                productId, inventory.getQuantity());

        // Save INVENTORY_RELEASED event
        java.util.Map<String, Object> event = new java.util.HashMap<>();
        event.put("productId", productId);
        event.put("quantity", quantity);
        event.put("newQuantity", inventory.getQuantity());

        saveOutboxEvent(productId, "INVENTORY_RELEASED", event, INVENTORY_TOPIC);
    }

    private void saveOutboxEvent(String aggregateId, String type, Object payload, String topic) {
        try {
            com.enterprise.inventory.entity.OutboxEvent event = new com.enterprise.inventory.entity.OutboxEvent();
            event.setAggregateType("INVENTORY");
            event.setAggregateId(aggregateId);
            event.setType(type);
            event.setTopic(topic);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setPayloadClass(payload.getClass().getName());

            outboxRepository.save(event);
            log.info("Saved event to Outbox: {} - {}", type, aggregateId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }
}
