package com.enterprise.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "shivamsriv961@gmail.com";
    private static final String TO_EMAIL = "shivamsriv961@gmail.com";

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendNotification(String orderId, String customerId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(TO_EMAIL);
            message.setSubject("Order Confirmation - Order #" + orderId);
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your order #%s has been placed successfully!\n\n" +
                            "Order Details:\n" +
                            "- Order ID: %s\n" +
                            "- Customer: %s\n\n" +
                            "Thank you for your purchase!\n\n" +
                            "Best regards,\n" +
                            "Order Management System",
                    customerId, orderId, orderId, customerId));

            mailSender.send(message);
            log.info("✅ EMAIL SENT SUCCESSFULLY for Order {} to {}", orderId, TO_EMAIL);

        } catch (Exception e) {
            log.error("❌ Failed to send email for Order {}: {}", orderId, e.getMessage());
            log.info("📧 Email would have been sent to: {}", TO_EMAIL);
        }
    }
}
