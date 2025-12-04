package com.enterprise.payment.service;

import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.repository.PaymentRepository;
import com.enterprise.payment.kafka.PaymentEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final com.enterprise.payment.repository.OutboxRepository outboxRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final java.util.Random random = new java.util.Random();

    // Topics
    private static final String PAYMENT_SUCCESS_TOPIC = "payment-success";
    private static final String PAYMENT_FAILED_TOPIC = "payment-failed";

    public PaymentService(PaymentRepository paymentRepository,
            com.enterprise.payment.repository.OutboxRepository outboxRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Payment processPayment(Long orderId, String customerId, BigDecimal amount, String paymentMethod) {
        logger.info("Processing payment for order: {}, customer: {}, amount: {}", orderId, customerId, amount);

        // ✅ IDEMPOTENCY CHECK: Return existing payment if already processed
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            logger.info("Payment already processed for order: {}. Returning existing payment with status: {}",
                    orderId, existingPayment.get().getStatus());
            return existingPayment.get();
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCustomerId(customerId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "CREDIT_CARD");
        payment.setStatus("PENDING");
        payment.setTransactionId(UUID.randomUUID().toString());

        // Mock payment processing: 80% success, 20% failure
        boolean paymentSuccess = random.nextInt(100) < 80;

        if (paymentSuccess) {
            payment.setStatus("SUCCESS");
            logger.info("Payment SUCCESS for order: {}, transaction: {}", orderId, payment.getTransactionId());
            paymentRepository.save(payment);

            // Save PaymentSuccessEvent to Outbox
            java.util.Map<String, Object> successEvent = new java.util.HashMap<>();
            successEvent.put("orderId", orderId);
            successEvent.put("customerId", customerId);
            successEvent.put("amount", amount);

            saveOutboxEvent(String.valueOf(payment.getId()), "PAYMENT_SUCCESS", successEvent, PAYMENT_SUCCESS_TOPIC);

        } else {
            payment.setStatus("FAILED");
            logger.warn("Payment FAILED for order: {}", orderId);
            paymentRepository.save(payment);

            // Save PaymentFailedEvent to Outbox
            java.util.Map<String, Object> failedEvent = new java.util.HashMap<>();
            failedEvent.put("orderId", orderId);
            failedEvent.put("customerId", customerId);
            failedEvent.put("reason", "Insufficient funds");

            saveOutboxEvent(String.valueOf(payment.getId()), "PAYMENT_FAILED", failedEvent, PAYMENT_FAILED_TOPIC);
        }

        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Payment refundPayment(Long orderId, String reason) {
        logger.info("Processing refund for order: {}, reason: {}", orderId, reason);

        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isEmpty()) {
            logger.warn("No payment found for order: {}. Cannot process refund.", orderId);
            throw new RuntimeException("Payment not found for order: " + orderId);
        }

        Payment payment = existingPayment.get();

        // Validate payment can be refunded
        if ("REFUNDED".equals(payment.getStatus())) {
            logger.info("Payment for order {} is already refunded. Skipping.", orderId);
            return payment; // Idempotency: already refunded
        }

        if (!"SUCCESS".equals(payment.getStatus())) {
            logger.warn("Cannot refund payment for order {}. Current status: {}", orderId, payment.getStatus());
            throw new RuntimeException("Cannot refund payment with status: " + payment.getStatus());
        }

        // Process refund (compensating transaction)
        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        logger.info("Refund processed successfully for order: {}, transaction: {}", orderId,
                payment.getTransactionId());

        // Save refund event to Outbox (using failed topic as per original code logic
        // for "cancellation" flow?)
        // Original code: paymentEventProducer.publishPaymentFailed(orderId,
        // payment.getCustomerId(), "Order cancelled - " + reason);
        // This seems to be reusing the failed event for refunds/cancellations.

        java.util.Map<String, Object> refundEvent = new java.util.HashMap<>();
        refundEvent.put("orderId", orderId);
        refundEvent.put("customerId", payment.getCustomerId());
        refundEvent.put("reason", "Order cancelled - " + reason);

        saveOutboxEvent(String.valueOf(payment.getId()), "PAYMENT_REFUNDED", refundEvent, PAYMENT_FAILED_TOPIC);

        return payment;
    }

    private void saveOutboxEvent(String aggregateId, String type, Object payload, String topic) {
        try {
            com.enterprise.payment.entity.OutboxEvent event = new com.enterprise.payment.entity.OutboxEvent();
            event.setAggregateType("PAYMENT");
            event.setAggregateId(aggregateId);
            event.setType(type);
            event.setTopic(topic);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setPayloadClass(payload.getClass().getName());

            outboxRepository.save(event);
            logger.info("Saved event to Outbox: {} - {}", type, aggregateId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }
}
