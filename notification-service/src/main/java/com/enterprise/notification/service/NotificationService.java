package com.enterprise.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    // Demo email for notifications
    private static final String DEMO_EMAIL = "shivamsriv961@gmail.com";

    public void sendNotification(String orderId, String customerId) {
        // In a real application, this would send an actual email
        // For now, we'll log it with the email address

        String emailMessage = String.format(
                "To: %s\n" +
                        "Subject: Order Confirmation - Order #%s\n" +
                        "Body: Dear %s, your order #%s has been placed successfully!\n" +
                        "Thank you for your purchase.",
                DEMO_EMAIL, orderId, customerId, orderId);

        log.info("SENT EMAIL NOTIFICATION for Order {} to Customer {}", orderId, customerId);
        log.info("Email Details:\n{}", emailMessage);
    }
}
