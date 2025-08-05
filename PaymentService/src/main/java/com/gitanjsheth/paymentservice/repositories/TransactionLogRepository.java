package com.gitanjsheth.paymentservice.repositories;

import com.gitanjsheth.paymentservice.models.PaymentGateway;
import com.gitanjsheth.paymentservice.models.TransactionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    
    // Find logs by payment ID
    List<TransactionLog> findByPaymentIdOrderByLoggedAtDesc(String paymentId);
    
    // Find logs by action
    List<TransactionLog> findByActionOrderByLoggedAtDesc(String action);
    
    // Find logs by gateway
    List<TransactionLog> findByGatewayOrderByLoggedAtDesc(PaymentGateway gateway);
    
    // Find failed transactions
    List<TransactionLog> findBySuccessFalseOrderByLoggedAtDesc();
    
    // Find logs in date range
    @Query("SELECT tl FROM TransactionLog tl WHERE tl.loggedAt BETWEEN :startDate AND :endDate ORDER BY tl.loggedAt DESC")
    List<TransactionLog> findLogsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find recent logs with pagination
    Page<TransactionLog> findAllByOrderByLoggedAtDesc(Pageable pageable);
    
    // Analytics queries
    @Query("SELECT COUNT(tl) FROM TransactionLog tl WHERE tl.success = true AND tl.loggedAt BETWEEN :startDate AND :endDate")
    Long countSuccessfulTransactionsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(tl) FROM TransactionLog tl WHERE tl.success = false AND tl.loggedAt BETWEEN :startDate AND :endDate")
    Long countFailedTransactionsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(tl.processingTimeMs) FROM TransactionLog tl WHERE tl.success = true AND tl.loggedAt BETWEEN :startDate AND :endDate")
    Double averageProcessingTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find slow transactions
    @Query("SELECT tl FROM TransactionLog tl WHERE tl.processingTimeMs > :thresholdMs ORDER BY tl.processingTimeMs DESC")
    List<TransactionLog> findSlowTransactions(@Param("thresholdMs") Long thresholdMs);
    
    // Find transactions by error code
    List<TransactionLog> findByErrorCodeOrderByLoggedAtDesc(String errorCode);
    
    // Clean up old logs (for maintenance)
    @Query("DELETE FROM TransactionLog tl WHERE tl.loggedAt < :cutoffDate")
    void deleteLogsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}