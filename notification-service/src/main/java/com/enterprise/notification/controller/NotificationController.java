package com.enterprise.notification.controller;

import com.enterprise.notification.dto.NotificationRequest;
import com.enterprise.notification.model.NotificationRecord;
import com.enterprise.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'AUDITOR')")
    public List<NotificationRecord> getNotifications() {
        return notificationService.getNotificationHistory();
    }

    @PostMapping("/send")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public String sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendNotification(request.getOrderId(), request.getCustomerId());
        return "Notification triggered for Order ID: " + request.getOrderId();
    }
}
