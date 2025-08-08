package com.gitanjsheth.notificationservice.repositories;

import com.gitanjsheth.notificationservice.models.Notification;
import com.gitanjsheth.notificationservice.models.NotificationStatus;
import com.gitanjsheth.notificationservice.models.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserId(String userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndNotificationType(String userId, NotificationType notificationType, Pageable pageable);
    
    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);
    
    List<Notification> findByStatusAndNextRetryAtBefore(NotificationStatus status, LocalDateTime nextRetryAt);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)")
    List<Notification> findPendingRetries(@Param("status") NotificationStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.notificationType = :type AND n.createdAt >= :since")
    long countByUserIdAndTypeSince(@Param("userId") String userId, @Param("type") NotificationType type, @Param("since") LocalDateTime since);
    
    List<Notification> findBySourceEventId(String sourceEventId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.createdAt < :before")
    List<Notification> findOldNotificationsByStatus(@Param("status") NotificationStatus status, @Param("before") LocalDateTime before);
} 