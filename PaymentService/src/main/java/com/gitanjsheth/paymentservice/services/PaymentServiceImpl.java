package com.gitanjsheth.paymentservice.services;

import com.gitanjsheth.paymentservice.dtos.PaymentMethodDto;
import com.gitanjsheth.paymentservice.dtos.PaymentRequestDto;
import com.gitanjsheth.paymentservice.dtos.PaymentResponseDto;
import com.gitanjsheth.paymentservice.models.*;
import com.gitanjsheth.paymentservice.repositories.PaymentMethodRepository;
import com.gitanjsheth.paymentservice.repositories.PaymentRepository;
import com.gitanjsheth.paymentservice.repositories.TransactionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final ReceiptService receiptService;
    
    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                            PaymentMethodRepository paymentMethodRepository,
                            TransactionLogRepository transactionLogRepository,
                            ReceiptService receiptService) {
        this.paymentRepository = paymentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.receiptService = receiptService;
    }
    
    // ============================================================================
    // PAYMENT PROCESSING
    // ============================================================================
    
    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.info("Processing payment for order: {}, amount: {} {}", 
                request.getOrderId(), request.getAmount(), request.getCurrency());
        
        // Create payment record
        Payment payment = new Payment();
        payment.setPaymentId("PAY_" + UUID.randomUUID().toString().substring(0, 8));
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setGateway(PaymentGateway.STRIPE); // Default for now
        payment.setStatus(PaymentStatus.PENDING);
        
        // Simulate payment processing (basic simulation as mentioned in requirements)
        boolean success = request.getAmount().compareTo(new BigDecimal("1000.00")) < 0;
        
        if (success) {
            payment.markAsCompleted("TXN_" + UUID.randomUUID().toString().substring(0, 8), 
                    "{\"status\": \"success\", \"message\": \"Payment processed successfully\"}");
            
            // Generate receipt
            String receiptUrl = receiptService.generateReceipt(payment);
            payment.setMetadata("{\"receiptUrl\": \"" + receiptUrl + "\"}");
            
            log.info("Payment completed successfully for order: {}, paymentId: {}", 
                    request.getOrderId(), payment.getPaymentId());
        } else {
            payment.markAsFailed("Amount threshold exceeded", 
                    "{\"status\": \"failed\", \"message\": \"Amount exceeds simulation threshold\"}");
            log.warn("Payment failed for order: {}, reason: Amount threshold exceeded", request.getOrderId());
        }
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        // Log transaction
        TransactionLog logEntry = new TransactionLog(payment.getPaymentId(), 
                success ? "PROCESS_SUCCESS" : "PROCESS_FAIL", payment.getGateway());
        if (success) {
            logEntry.markSuccess("200", "Payment processed successfully", 150L);
        } else {
            logEntry.markFailure("400", "Amount threshold exceeded", 50L);
        }
        transactionLogRepository.save(logEntry);
        
        return convertToResponseDto(savedPayment);
    }
    
    @Override
    @Transactional
    public PaymentResponseDto processPaymentWithSavedMethod(Long paymentMethodId, String orderId, 
                                                         BigDecimal amount, String currency, Long userId) {
        // Implementation for using saved payment method
        PaymentRequestDto request = new PaymentRequestDto();
        request.setOrderId(orderId);
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setUserId(userId);
        return processPayment(request);
    }
    
    @Override
    public PaymentResponseDto getPayment(Long paymentId) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        return payment.map(this::convertToResponseDto).orElse(null);
    }
    
    @Override
    public PaymentResponseDto getPaymentByPaymentId(String paymentId) {
        Optional<Payment> payment = paymentRepository.findByPaymentId(paymentId);
        return payment.map(this::convertToResponseDto).orElse(null);
    }
    
    @Override
    public PaymentResponseDto getPaymentByOrderId(String orderId) {
        Optional<Payment> payment = paymentRepository.findByOrderId(orderId);
        return payment.map(this::convertToResponseDto).orElse(null);
    }
    
    @Override
    public Page<PaymentResponseDto> getUserPayments(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return payments.map(this::convertToResponseDto);
    }
    
    // ============================================================================
    // PAYMENT METHOD MANAGEMENT
    // ============================================================================
    
    @Override
    @Transactional
    public PaymentMethodDto createPaymentMethod(PaymentRequestDto request, Long userId) {
        // Implementation for creating payment method
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setUserId(userId);
        paymentMethod.setType("CARD");
        paymentMethod.setCardLastFour("1234");
        paymentMethod.setExpiryMonth(12);
        paymentMethod.setExpiryYear(2025);
        paymentMethod.setIsDefault(false);
        
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        return convertToPaymentMethodDto(saved);
    }
    
    @Override
    public List<PaymentMethodDto> getUserPaymentMethods(Long userId) {
        List<PaymentMethod> methods = paymentMethodRepository.findByUserId(userId);
        return methods.stream().map(this::convertToPaymentMethodDto).toList();
    }
    
    @Override
    public PaymentMethodDto getDefaultPaymentMethod(Long userId) {
        Optional<PaymentMethod> defaultMethod = paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId);
        return defaultMethod.map(this::convertToPaymentMethodDto).orElse(null);
    }
    
    @Override
    @Transactional
    public PaymentMethodDto setDefaultPaymentMethod(Long paymentMethodId, Long userId) {
        // Implementation for setting default payment method
        return null; // Placeholder
    }
    
    @Override
    @Transactional
    public void deletePaymentMethod(Long paymentMethodId, Long userId) {
        // Implementation for deleting payment method
    }
    
    // ============================================================================
    // REFUNDS AND CANCELLATIONS
    // ============================================================================
    
    @Override
    @Transactional
    public PaymentResponseDto refundPayment(String paymentId, BigDecimal amount, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentId);
        if (paymentOpt.isEmpty()) {
            return null;
        }
        
        Payment payment = paymentOpt.get();
        payment.markAsRefunded(amount);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Log refund transaction
        TransactionLog logEntry = new TransactionLog(paymentId, "REFUND", payment.getGateway());
        logEntry.markSuccess("200", "Refund processed successfully", 100L);
        transactionLogRepository.save(logEntry);
        
        return convertToResponseDto(savedPayment);
    }
    
    @Override
    @Transactional
    public PaymentResponseDto cancelPayment(String paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentId);
        if (paymentOpt.isEmpty()) {
            return null;
        }
        
        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.CANCELLED);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Log cancellation
        TransactionLog logEntry = new TransactionLog(paymentId, "CANCEL", payment.getGateway());
        logEntry.markSuccess("200", "Payment cancelled successfully", 50L);
        transactionLogRepository.save(logEntry);
        
        return convertToResponseDto(savedPayment);
    }
    
    // ============================================================================
    // WEBHOOK HANDLING
    // ============================================================================
    
    @Override
    public void processWebhookEvent(String payload, String signature, String gatewayType) {
        // Implementation for webhook processing
        log.info("Processing webhook event: {}", payload);
    }
    
    // ============================================================================
    // PAYMENT RETRY AND RECOVERY
    // ============================================================================
    
    @Override
    @Transactional
    public PaymentResponseDto retryPayment(String paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentId);
        if (paymentOpt.isEmpty()) {
            return null;
        }
        
        Payment payment = paymentOpt.get();
        if (!payment.canBeRetried()) {
            return convertToResponseDto(payment);
        }
        
        // Reset status and retry
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRetryCount(payment.getRetryCount() + 1);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Log retry
        TransactionLog logEntry = new TransactionLog(paymentId, "RETRY", payment.getGateway());
        logEntry.markSuccess("200", "Payment retry initiated", 75L);
        transactionLogRepository.save(logEntry);
        
        return convertToResponseDto(savedPayment);
    }
    
    @Override
    public void processExpiredPayments() {
        // Implementation for processing expired payments
        log.info("Processing expired payments");
    }
    
    @Override
    public void retryFailedPayments() {
        // Implementation for retrying failed payments
        log.info("Retrying failed payments");
    }
    
    // ============================================================================
    // ADMIN OPERATIONS
    // ============================================================================
    
    @Override
    public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findRecentPayments(pageable);
        return payments.map(this::convertToResponseDto);
    }
    
    @Override
    public List<PaymentResponseDto> getPaymentsByStatus(String status) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            List<Payment> payments = paymentRepository.findByStatus(paymentStatus);
            return payments.stream().map(this::convertToResponseDto).toList();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment status: {}", status);
            return List.of();
        }
    }
    
    @Override
    public PaymentAnalytics getPaymentAnalytics(String period) {
        // Implementation for payment analytics
        return new PaymentAnalytics(0L, 0L, 0L, 0.0, 0.0, 0.0, 0.0);
    }
    
    @Override
    @Transactional
    public PaymentResponseDto capturePayment(String paymentId, BigDecimal amount) {
        // Implementation for capturing payment
        return null; // Placeholder
    }
    
    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================
    
    private PaymentResponseDto convertToResponseDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrderId());
        dto.setUserId(payment.getUserId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus());
        dto.setGateway(payment.getGateway());
        dto.setGatewayTransactionId(payment.getGatewayTransactionId());
        dto.setProcessedAt(payment.getProcessedAt());
        dto.setFailedAt(payment.getFailedAt());
        dto.setFailureReason(payment.getFailureReason());
        dto.setRefundedAmount(payment.getRefundedAmount());
        dto.setRefundedAt(payment.getRefundedAt());
        dto.setRetryCount(payment.getRetryCount());
        dto.setWebhookReceived(payment.getWebhookReceived());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        
        // Set receipt URL if available
        if (payment.getMetadata() != null && payment.getMetadata().contains("receiptUrl")) {
            // Extract receipt URL from metadata (simplified)
            String receiptUrl = receiptService.getReceiptUrl(payment.getPaymentId());
            dto.setReceiptUrl(receiptUrl);
        }
        
        return dto;
    }
    
    private PaymentMethodDto convertToPaymentMethodDto(PaymentMethod paymentMethod) {
        PaymentMethodDto dto = new PaymentMethodDto();
        dto.setId(paymentMethod.getId());
        dto.setUserId(paymentMethod.getUserId());
        dto.setType(paymentMethod.getType());
        dto.setCardLastFour(paymentMethod.getCardLastFour());
        dto.setExpiryMonth(paymentMethod.getExpiryMonth());
        dto.setExpiryYear(paymentMethod.getExpiryYear());
        dto.setIsDefault(paymentMethod.getIsDefault());
        dto.setCreatedAt(paymentMethod.getCreatedAt());
        dto.setUpdatedAt(paymentMethod.getUpdatedAt());
        return dto;
    }
}
