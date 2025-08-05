package com.gitanjsheth.paymentservice.services;

import com.gitanjsheth.paymentservice.dtos.PaymentMethodDto;
import com.gitanjsheth.paymentservice.dtos.PaymentRequestDto;
import com.gitanjsheth.paymentservice.dtos.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    
    // ============================================================================
    // PAYMENT PROCESSING
    // ============================================================================
    
    /**
     * Process a payment for an order
     */
    PaymentResponseDto processPayment(PaymentRequestDto request);
    
    /**
     * Process payment using existing payment method
     */
    PaymentResponseDto processPaymentWithSavedMethod(Long paymentMethodId, String orderId, BigDecimal amount, String currency, Long userId);
    
    /**
     * Get payment by ID
     */
    PaymentResponseDto getPayment(Long paymentId);
    
    /**
     * Get payment by payment ID (external)
     */
    PaymentResponseDto getPaymentByPaymentId(String paymentId);
    
    /**
     * Get payment by order ID
     */
    PaymentResponseDto getPaymentByOrderId(String orderId);
    
    /**
     * Get user's payment history
     */
    Page<PaymentResponseDto> getUserPayments(Long userId, Pageable pageable);
    
    // ============================================================================
    // PAYMENT METHOD MANAGEMENT
    // ============================================================================
    
    /**
     * Create and save a payment method
     */
    PaymentMethodDto createPaymentMethod(PaymentRequestDto request, Long userId);
    
    /**
     * Get user's saved payment methods
     */
    List<PaymentMethodDto> getUserPaymentMethods(Long userId);
    
    /**
     * Get user's default payment method
     */
    PaymentMethodDto getDefaultPaymentMethod(Long userId);
    
    /**
     * Set payment method as default
     */
    PaymentMethodDto setDefaultPaymentMethod(Long paymentMethodId, Long userId);
    
    /**
     * Delete payment method
     */
    void deletePaymentMethod(Long paymentMethodId, Long userId);
    
    // ============================================================================
    // REFUNDS AND CANCELLATIONS
    // ============================================================================
    
    /**
     * Refund a payment (full or partial)
     */
    PaymentResponseDto refundPayment(String paymentId, BigDecimal amount, String reason);
    
    /**
     * Cancel a pending payment
     */
    PaymentResponseDto cancelPayment(String paymentId);
    
    // ============================================================================
    // WEBHOOK HANDLING
    // ============================================================================
    
    /**
     * Process webhook event from payment gateway
     */
    void processWebhookEvent(String payload, String signature, String gatewayType);
    
    // ============================================================================
    // PAYMENT RETRY AND RECOVERY
    // ============================================================================
    
    /**
     * Retry failed payment
     */
    PaymentResponseDto retryPayment(String paymentId);
    
    /**
     * Process expired/stuck payments (scheduled task)
     */
    void processExpiredPayments();
    
    /**
     * Retry failed payments (scheduled task)
     */
    void retryFailedPayments();
    
    // ============================================================================
    // ADMIN OPERATIONS
    // ============================================================================
    
    /**
     * Get all payments (admin)
     */
    Page<PaymentResponseDto> getAllPayments(Pageable pageable);
    
    /**
     * Get payments by status (admin)
     */
    List<PaymentResponseDto> getPaymentsByStatus(String status);
    
    /**
     * Get payment analytics (admin)
     */
    PaymentAnalytics getPaymentAnalytics(String period);
    
    /**
     * Capture payment (for two-step payments)
     */
    PaymentResponseDto capturePayment(String paymentId, BigDecimal amount);
    
    // Analytics DTO
    class PaymentAnalytics {
        public Long totalPayments;
        public Long successfulPayments;
        public Long failedPayments;
        public Double totalAmount;
        public Double averageAmount;
        public Double successRate;
        public Double averageProcessingTime;
        
        public PaymentAnalytics(Long totalPayments, Long successfulPayments, Long failedPayments, 
                              Double totalAmount, Double averageAmount, Double successRate, Double averageProcessingTime) {
            this.totalPayments = totalPayments;
            this.successfulPayments = successfulPayments;
            this.failedPayments = failedPayments;
            this.totalAmount = totalAmount;
            this.averageAmount = averageAmount;
            this.successRate = successRate;
            this.averageProcessingTime = averageProcessingTime;
        }
    }
}