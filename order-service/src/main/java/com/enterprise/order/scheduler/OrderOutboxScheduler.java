package com.enterprise.order.scheduler;

import com.enterprise.order.entity.OutboxEvent;
import com.enterprise.order.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OrderOutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderOutboxScheduler.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderOutboxScheduler(OutboxRepository outboxRepository, KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findAll();

        if (!events.isEmpty()) {
            log.info("Found {} outbox events to process", events.size());
        }

        for (OutboxEvent event : events) {
            try {
                // Deserialize payload to Object if needed, or send as String/Bytes
                // Here we assume the payload is already a JSON string compatible with what
                // consumers expect
                // Or we can parse it back to specific DTOs if the KafkaTemplate expects
                // specific types.
                // For simplicity and generic handling, we'll send the payload string or map it.
                // However, existing producers sent specific DTOs (OrderResponse, etc.)
                // To keep it simple and robust, we will send the JSON string payload.
                // BUT, if downstream expects a specific class, we might need to handle that.
                // Let's check how consumers deserialize. They likely use Jackson.
                // Sending the JSON string as the value is usually safe if consumers can handle
                // it.

                // Better approach for this demo: Convert JSON string back to object?
                // Or just send the string. Let's try sending the raw object if we can,
                // but we stored it as a string.
                // Let's send the string payload. Consumers using Spring Kafka's
                // JsonDeserializer
                // might need configuration to accept strings or we need to deserialize here.

                // Let's assume we send the JSON string.

                log.info("Publishing event: {} to topic: {}", event.getType(), event.getTopic());

                Object message;
                if (event.getPayloadClass() != null) {
                    Class<?> targetClass = Class.forName(event.getPayloadClass());
                    message = objectMapper.readValue(event.getPayload(), targetClass);
                } else {
                    // Fallback to Map if class not found or not set
                    message = objectMapper.readValue(event.getPayload(), java.util.Map.class);
                }

                kafkaTemplate.send(event.getTopic(), message);

                // Delete after successful publish
                outboxRepository.delete(event);

            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
            }
        }
    }
}
