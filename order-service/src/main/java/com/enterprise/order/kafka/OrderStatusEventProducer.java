package com.enterprise.order.kafka;

import com.enterprise.order.model.OrderStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusEventProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "order-status-changed";

    public OrderStatusEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendStatusChanged(Long orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        String payload = String.format("{\"orderId\":%d,\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}", orderId, oldStatus,
                newStatus);
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        kafkaTemplate.send(message);
        log.info("Published order-status-changed event for order {}: {} -> {}", orderId, oldStatus, newStatus);
    }
}
