package com.gitanjsheth.notificationservice.services;

import com.gitanjsheth.notificationservice.dtos.CreateNotificationDto;
import com.gitanjsheth.notificationservice.dtos.NotificationDto;
import com.gitanjsheth.notificationservice.models.NotificationStatus;
import com.gitanjsheth.notificationservice.models.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NotificationService {
    
    /**
     * Create and send a notification
     * @param createNotificationDto Notification data
     * @return Created notification
     */
    NotificationDto createNotification(CreateNotificationDto createNotificationDto);
    
    /**
     * Send notification immediately
     * @param notificationId ID of the notification to send
     * @return Updated notification
     */
    NotificationDto sendNotification(Long notificationId);
    
    /**
     * Process pending notifications (for scheduled tasks)
     */
    void processPendingNotifications();
    
    /**
     * Retry failed notifications
     */
    void retryFailedNotifications();
    
    /**
     * Get notifications for a user
     * @param userId User ID
     * @param pageable Pagination
     * @return Page of notifications
     */
    Page<NotificationDto> getUserNotifications(String userId, Pageable pageable);
    
    /**
     * Get notifications by status
     * @param status Notification status
     * @param pageable Pagination
     * @return Page of notifications
     */
    Page<NotificationDto> getNotificationsByStatus(NotificationStatus status, Pageable pageable);
    
    /**
     * Create notification from template
     * @param userId User ID
     * @param email User email
     * @param notificationType Type of notification
     * @param templateData Template variables
     * @param sourceEventId Source event ID
     * @return Created notification
     */
    NotificationDto createNotificationFromTemplate(String userId, String email, NotificationType notificationType, 
                                                  Map<String, Object> templateData, String sourceEventId);
    
    /**
     * Check if user should receive notification based on preferences
     * @param userId User ID
     * @param notificationType Type of notification
     * @return true if user should receive notification
     */
    boolean shouldSendNotification(String userId, NotificationType notificationType);
} 