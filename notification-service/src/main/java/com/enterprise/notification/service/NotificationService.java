package com.enterprise.notification.service;

import com.enterprise.notification.entity.OutboxEvent;
import com.enterprise.notification.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private static final String FROM_EMAIL = "shivamsriv961@gmail.com";
    private static final String TO_EMAIL = "shivamsriv961@gmail.com";
    private static final String NOTIFICATION_TOPIC = "notifications";

    private final java.util.List<com.enterprise.notification.model.NotificationRecord> notificationHistory = new java.util.ArrayList<>();

    @org.springframework.beans.factory.annotation.Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;

    public NotificationService(JavaMailSender mailSender,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public java.util.List<com.enterprise.notification.model.NotificationRecord> getNotificationHistory() {
        return notificationHistory;
    }

    @Async
    @Transactional
    public void sendNotification(String orderId, String customerId) {
        String status = "SUCCESS";
        String messageContent = "";
        String eventType = "NOTIFICATION_SENT";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(TO_EMAIL);
            message.setSubject("Order Confirmation - Order #" + orderId);
            messageContent = String.format(
                    "Dear %s,\\n\\n" +
                            "Your order #%s has been placed successfully!\\n\\n" +
                            "Order Details:\\n" +
                            "- Order ID: %s\\n" +
                            "- Customer: %s\\n\\n" +
                            "Thank you for your purchase!\\n\\n" +
                            "Best regards,\\n" +
                            "Order Management System",
                    customerId, orderId, orderId, customerId);
            message.setText(messageContent);

            if (emailEnabled) {
                mailSender.send(message);
                log.info("✅ EMAIL SENT SUCCESSFULLY for Order {} to {}", orderId, TO_EMAIL);
                eventType = "NOTIFICATION_SENT";
            } else {
                status = "SIMULATED_SUCCESS";
                log.info("📝 SIMULATION MODE: Email for Order {} would be sent to {}. Content:\\n{}", orderId, TO_EMAIL,
                        messageContent);
                eventType = "NOTIFICATION_SENT";
            }

        } catch (Exception e) {
            status = "FAILED: " + e.getMessage();
            eventType = "NOTIFICATION_FAILED";
            log.error("❌ Failed to send email for Order {}: {}", orderId, e.getMessage());
            log.info("📧 Email would have been sent to: {}", TO_EMAIL);
        } finally {
            // Save notification record
            com.enterprise.notification.model.NotificationRecord record = new com.enterprise.notification.model.NotificationRecord(
                    orderId, customerId, messageContent, java.time.LocalDateTime.now(), status);
            notificationHistory.add(record);

            // Save event to outbox
            saveOutboxEvent(orderId, eventType, record, NOTIFICATION_TOPIC);
        }
    }

    private void saveOutboxEvent(String aggregateId, String type, Object payload, String topic) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("NOTIFICATION");
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
