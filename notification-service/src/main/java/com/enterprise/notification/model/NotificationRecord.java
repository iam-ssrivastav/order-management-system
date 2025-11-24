package com.enterprise.notification.model;

import java.time.LocalDateTime;

public class NotificationRecord {
    private String orderId;
    private String customerId;
    private String message;
    private LocalDateTime timestamp;
    private String status;

    public NotificationRecord(String orderId, String customerId, String message, LocalDateTime timestamp,
            String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }
}
