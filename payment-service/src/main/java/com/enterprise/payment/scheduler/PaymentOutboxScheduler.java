package com.enterprise.payment.scheduler;

import com.enterprise.payment.entity.OutboxEvent;
import com.enterprise.payment.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PaymentOutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxScheduler.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentOutboxScheduler(OutboxRepository outboxRepository, KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findAll();

        if (!events.isEmpty()) {
            log.info("Found {} outbox events to process", events.size());
        }

        for (OutboxEvent event : events) {
            try {
                log.info("Publishing event: {} to topic: {}", event.getType(), event.getTopic());

                Object message;
                if (event.getPayloadClass() != null) {
                    Class<?> targetClass = Class.forName(event.getPayloadClass());
                    message = objectMapper.readValue(event.getPayload(), targetClass);
                } else {
                    message = objectMapper.readValue(event.getPayload(), java.util.Map.class);
                }

                kafkaTemplate.send(event.getTopic(), message);

                outboxRepository.delete(event);

            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
            }
        }
    }
}
