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
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
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

            // Publish PaymentSuccessEvent
            paymentEventProducer.publishPaymentSuccess(orderId, customerId, amount);
        } else {
            payment.setStatus("FAILED");
            logger.warn("Payment FAILED for order: {}", orderId);
            paymentRepository.save(payment);

            // Publish PaymentFailedEvent
            paymentEventProducer.publishPaymentFailed(orderId, customerId, "Insufficient funds");
        }

        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
