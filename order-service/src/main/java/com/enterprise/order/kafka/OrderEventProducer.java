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
    private final KafkaTemplate<String, OrderResponse> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(OrderResponse orderEvent) {
        log.info("Sending order event to Kafka: {}", orderEvent);
        Message<OrderResponse> message = MessageBuilder
                .withPayload(orderEvent)
                .setHeader(KafkaHeaders.TOPIC, "order-events")
                .build();
        kafkaTemplate.send(message);
    }
}
