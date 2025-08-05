package com.gitanjsheth.paymentservice.repositories;

import com.gitanjsheth.paymentservice.models.PaymentGateway;
import com.gitanjsheth.paymentservice.models.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    
    // Find by user ID
    List<PaymentMethod> findByUserIdAndIsActiveTrue(Long userId);
    List<PaymentMethod> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    // Find default payment method for user
    Optional<PaymentMethod> findByUserIdAndIsDefaultTrueAndIsActiveTrue(Long userId);
    
    // Find by gateway token
    Optional<PaymentMethod> findByGatewayToken(String gatewayToken);
    
    // Find by gateway and user
    List<PaymentMethod> findByUserIdAndGatewayAndIsActiveTrue(Long userId, PaymentGateway gateway);
    
    // Find expired payment methods
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.isActive = true AND pm.expiryYear < :currentYear OR (pm.expiryYear = :currentYear AND pm.expiryMonth < :currentMonth)")
    List<PaymentMethod> findExpiredPaymentMethods(@Param("currentYear") Integer currentYear, @Param("currentMonth") Integer currentMonth);
    
    // Update default payment method (unset all others for user)
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId AND pm.id != :excludeId")
    void unsetDefaultForUser(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
    
    // Update default payment method (unset all for user)
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId")
    void unsetAllDefaultForUser(@Param("userId") Long userId);
    
    // Soft delete by setting inactive
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isActive = false WHERE pm.id = :id")
    void softDelete(@Param("id") Long id);
    
    // Count active payment methods for user
    Long countByUserIdAndIsActiveTrue(Long userId);
    
    // Find payment methods by card details (for duplicate detection)
    Optional<PaymentMethod> findByUserIdAndCardLastFourAndExpiryMonthAndExpiryYearAndIsActiveTrue(
        Long userId, String cardLastFour, Integer expiryMonth, Integer expiryYear);
}