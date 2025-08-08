package com.gitanjsheth.notificationservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitanjsheth.notificationservice.dtos.CreateNotificationDto;
import com.gitanjsheth.notificationservice.dtos.NotificationDto;
import com.gitanjsheth.notificationservice.models.Notification;
import com.gitanjsheth.notificationservice.models.NotificationStatus;
import com.gitanjsheth.notificationservice.models.NotificationType;
import com.gitanjsheth.notificationservice.repositories.NotificationRepository;
import com.gitanjsheth.notificationservice.repositories.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private final NotificationRepository notificationRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;
    
    public NotificationServiceImpl(NotificationRepository notificationRepository, 
                                 UserNotificationPreferenceRepository preferenceRepository,
                                 EmailService emailService, 
                                 TemplateService templateService, 
                                 ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.emailService = emailService;
        this.templateService = templateService;
        this.objectMapper = objectMapper;
    }
    
    @Value("${app.notification.max-retries}")
    private Integer maxRetries;
    
    @Value("${app.notification.retry-delay-minutes}")
    private Integer retryDelayMinutes;
    
    @Override
    public NotificationDto createNotification(CreateNotificationDto createNotificationDto) {
        try {
            log.info("Creating notification for user: {} with type: {}", 
                    createNotificationDto.getUserId(), createNotificationDto.getNotificationType());
            
            // Check if user should receive this notification
            if (!shouldSendNotification(createNotificationDto.getUserId(), createNotificationDto.getNotificationType())) {
                log.info("User {} has opted out of {} notifications", 
                        createNotificationDto.getUserId(), createNotificationDto.getNotificationType());
                return null;
            }
            
            Notification notification = new Notification();
            notification.setUserId(createNotificationDto.getUserId());
            notification.setEmail(createNotificationDto.getEmail());
            notification.setNotificationType(createNotificationDto.getNotificationType());
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSubject(createNotificationDto.getSubject());
            notification.setContent(createNotificationDto.getContent());
            notification.setTemplateName(createNotificationDto.getTemplateName());
            notification.setSourceEventId(createNotificationDto.getSourceEventId());
            
            // Convert template data to JSON string
            if (createNotificationDto.getTemplateData() != null) {
                notification.setTemplateData(objectMapper.writeValueAsString(createNotificationDto.getTemplateData()));
            }
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification created with ID: {}", savedNotification.getId());
            
            // Try to send immediately
            try {
                sendNotification(savedNotification.getId());
            } catch (Exception e) {
                log.warn("Failed to send notification immediately, will retry later: {}", savedNotification.getId(), e);
            }
            
            return convertToDto(savedNotification);
            
        } catch (Exception e) {
            log.error("Failed to create notification for user: {}", createNotificationDto.getUserId(), e);
            throw new RuntimeException("Failed to create notification", e);
        }
    }
    
    @Override
    public NotificationDto sendNotification(Long notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
            
            log.info("Sending notification ID: {} to: {}", notificationId, notification.getEmail());
            
            // Process template if needed
            String content = notification.getContent();
            if (content == null && notification.getTemplateName() != null) {
                Map<String, Object> templateData = null;
                if (notification.getTemplateData() != null) {
                    templateData = objectMapper.readValue(notification.getTemplateData(), Map.class);
                }
                content = templateService.processTemplate(notification.getTemplateName(), templateData);
                notification.setContent(content);
            }
            
            // Send email
            String awsMessageId = emailService.sendEmail(notification);
            
            // Update notification status
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setAwsMessageId(awsMessageId);
            notification.setErrorMessage(null);
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification sent successfully. AWS Message ID: {}", awsMessageId);
            
            return convertToDto(savedNotification);
            
        } catch (Exception e) {
            log.error("Failed to send notification ID: {}", notificationId, e);
            
            // Update notification with error and schedule retry
            Notification notification = notificationRepository.findById(notificationId).orElse(null);
            if (notification != null) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
                notification.setRetryCount(notification.getRetryCount() + 1);
                
                if (notification.getRetryCount() < maxRetries) {
                    notification.setStatus(NotificationStatus.RETRY_PENDING);
                    notification.setNextRetryAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
                    log.info("Scheduled retry for notification ID: {} at: {}", notificationId, notification.getNextRetryAt());
                }
                
                notificationRepository.save(notification);
            }
            
            throw new RuntimeException("Failed to send notification", e);
        }
    }
    
    @Override
    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    public void processPendingNotifications() {
        try {
            // Processing pending notifications
            
            org.springframework.data.domain.Page<Notification> pendingNotificationsPage = notificationRepository.findByStatus(NotificationStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 100));
            List<Notification> pendingNotifications = pendingNotificationsPage.getContent();
            
            for (Notification notification : pendingNotifications) {
                try {
                    sendNotification(notification.getId());
                } catch (Exception e) {
                    log.error("Failed to process pending notification ID: {}", notification.getId(), e);
                }
            }
            
            log.debug("Processed {} pending notifications", pendingNotifications.size());
            
        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
        }
    }
    
    @Override
    @Scheduled(fixedDelay = 60000) // Run every minute
    public void retryFailedNotifications() {
        try {
            log.debug("Processing retry pending notifications...");
            
            List<Notification> retryNotifications = notificationRepository.findPendingRetries(
                    NotificationStatus.RETRY_PENDING, LocalDateTime.now());
            
            for (Notification notification : retryNotifications) {
                try {
                    sendNotification(notification.getId());
                } catch (Exception e) {
                    log.error("Failed to retry notification ID: {}", notification.getId(), e);
                }
            }
            
            log.debug("Processed {} retry notifications", retryNotifications.size());
            
        } catch (Exception e) {
            log.error("Error processing retry notifications", e);
        }
    }
    
    @Override
    public Page<NotificationDto> getUserNotifications(String userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        return notifications.map(this::convertToDto);
    }
    
    @Override
    public Page<NotificationDto> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByStatus(status, pageable);
        return notifications.map(this::convertToDto);
    }
    
    @Override
    public NotificationDto createNotificationFromTemplate(String userId, String email, NotificationType notificationType, 
                                                        Map<String, Object> templateData, String sourceEventId) {
        try {
            log.info("Creating notification from template for user: {} with type: {}", userId, notificationType);
            
            // Check if user should receive this notification
            if (!shouldSendNotification(userId, notificationType)) {
                log.info("User {} has opted out of {} notifications", userId, notificationType);
                return null;
            }
            
            // Get default template for notification type
            var template = templateService.getDefaultTemplate(notificationType);
            if (template == null) {
                log.warn("No default template found for notification type: {}", notificationType);
                return null;
            }
            
            // Process template to get subject and content
            String processedSubject = template.getSubject();
            String processedContent = templateService.processTemplate(template.getTemplateName(), templateData);
            
            if (processedContent == null) {
                log.error("Failed to process template: {}", template.getTemplateName());
                return null;
            }
            
            // Create notification
            CreateNotificationDto createDto = new CreateNotificationDto();
            createDto.setUserId(userId);
            createDto.setEmail(email);
            createDto.setNotificationType(notificationType);
            createDto.setSubject(processedSubject);
            createDto.setContent(processedContent);
            createDto.setTemplateName(template.getTemplateName());
            createDto.setTemplateData(templateData);
            createDto.setSourceEventId(sourceEventId);
            
            return createNotification(createDto);
            
        } catch (Exception e) {
            log.error("Failed to create notification from template for user: {}", userId, e);
            throw new RuntimeException("Failed to create notification from template", e);
        }
    }
    
    @Override
    public boolean shouldSendNotification(String userId, NotificationType notificationType) {
        try {
            var preference = preferenceRepository.findByUserId(userId).orElse(null);
            
            // If no preference exists, default to sending notifications
            if (preference == null) {
                return true;
            }
            
            // Check general email preference
            if (!preference.getEmailEnabled()) {
                return false;
            }
            
            // Check specific notification type preferences
            switch (notificationType) {
                case USER_REGISTRATION:
                case USER_LOGIN:
                case PASSWORD_RESET:
                    return true; // Always send these critical notifications
                    
                case ORDER_CONFIRMATION:
                case ORDER_STATUS_UPDATE:
                case SHIPPING_UPDATE:
                case DELIVERY_CONFIRMATION:
                    return preference.getOrderUpdatesEnabled();
                    
                case PAYMENT_CONFIRMATION:
                case PAYMENT_FAILED:
                    return true; // Always send payment notifications
                    
                case CART_ABANDONMENT:
                case PRODUCT_BACK_IN_STOCK:
                case PROMOTIONAL_EMAIL:
                    return preference.getPromotionalEmailsEnabled() && preference.getMarketingEmailsEnabled();
                    
                default:
                    return true;
            }
            
        } catch (Exception e) {
            log.error("Error checking notification preferences for user: {}", userId, e);
            return true; // Default to sending if there's an error
        }
    }
    
    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setEmail(notification.getEmail());
        dto.setNotificationType(notification.getNotificationType());
        dto.setStatus(notification.getStatus());
        dto.setSubject(notification.getSubject());
        dto.setContent(notification.getContent());
        dto.setTemplateName(notification.getTemplateName());
        dto.setRetryCount(notification.getRetryCount());
        dto.setSentAt(notification.getSentAt());
        dto.setDeliveredAt(notification.getDeliveredAt());
        dto.setErrorMessage(notification.getErrorMessage());
        dto.setAwsMessageId(notification.getAwsMessageId());
        dto.setSourceEventId(notification.getSourceEventId());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        return dto;
    }
} 