package com.enterprise.order.kafka;

import com.enterprise.order.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(OrderResponse orderEvent) {
        log.info("Sending order event to Kafka: {}", orderEvent);
        try {
            String message = objectMapper.writeValueAsString(orderEvent);
            kafkaTemplate.send("order-events", message);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Error serializing order event", e);
        }
    }
}
