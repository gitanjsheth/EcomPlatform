package com.gitanjsheth.paymentservice.repositories;

import com.gitanjsheth.paymentservice.models.Payment;
import com.gitanjsheth.paymentservice.models.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find by payment ID
    Optional<Payment> findByPaymentId(String paymentId);
    
    // Find by order ID
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findAllByOrderId(String orderId);
    
    // Find by user ID
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    // Find by status
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusIn(List<PaymentStatus> statuses);
    
    // Find by gateway transaction ID
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
    
    // Find payments that need retry
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < p.maxRetries AND p.failedAt > :cutoffTime")
    List<Payment> findPaymentsForRetry(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find pending payments (for timeout handling)
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING') AND p.createdAt < :cutoffTime")
    List<Payment> findPendingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find payments without webhook confirmation
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND p.webhookReceived = false AND p.processedAt < :cutoffTime")
    List<Payment> findPaymentsWithoutWebhookConfirmation(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Analytics queries
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.processedAt BETWEEN :startDate AND :endDate")
    Long countSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.processedAt BETWEEN :startDate AND :endDate")
    Double sumSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED' AND p.failedAt BETWEEN :startDate AND :endDate")
    Long countFailedPaymentsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find recent payments for monitoring
    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC")
    Page<Payment> findRecentPayments(Pageable pageable);
    
    // Count payments by user
    Long countByUserId(Long userId);
    
    // Find refundable payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND (p.refundedAmount IS NULL OR p.refundedAmount < p.amount)")
    List<Payment> findRefundablePayments();
}