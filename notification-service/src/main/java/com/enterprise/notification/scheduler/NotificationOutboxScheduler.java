package com.enterprise.notification.scheduler;

import com.enterprise.notification.entity.OutboxEvent;
import com.enterprise.notification.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationOutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxScheduler.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationOutboxScheduler(OutboxRepository outboxRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findAll();

        if (!events.isEmpty()) {
            log.info("Found {} outbox events to process", events.size());
        }

        for (OutboxEvent event : events) {
            try {
                // Deserialize payload using stored class name
                Class<?> payloadClass = Class.forName(event.getPayloadClass());
                Object payload = objectMapper.readValue(event.getPayload(), payloadClass);

                // Publish to Kafka
                log.info("Publishing event: {} to topic: {}", event.getType(), event.getTopic());
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), payload);

                // Delete after successful publish
                outboxRepository.delete(event);
                log.info("Published and deleted event: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
            }
        }
    }
}
