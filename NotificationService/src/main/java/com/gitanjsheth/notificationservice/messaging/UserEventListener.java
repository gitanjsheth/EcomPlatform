package com.gitanjsheth.notificationservice.messaging;

import com.gitanjsheth.notificationservice.models.NotificationType;
import com.gitanjsheth.notificationservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserEventListener {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserEventListener.class);
    
    private final NotificationService notificationService;
    
    public UserEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @KafkaListener(topics = "user.events", groupId = "notification-service")
    public void handleUserEvent(Map<String, Object> userEvent) {
        try {
            String eventType = (String) userEvent.get("eventType");
            String userId = (String) userEvent.get("userId");
            String email = (String) userEvent.get("email");
            String eventId = (String) userEvent.get("eventId");
            
            log.info("Received user event: {} for user: {}", eventType, userId);
            
            switch (eventType) {
                case "USER_REGISTERED":
                    handleUserRegistration(userId, email, eventId);
                    break;
                    
                case "PASSWORD_RESET_REQUESTED":
                    handlePasswordResetRequest(userId, email, userEvent, eventId);
                    break;
                    
                case "USER_LOGIN":
                    handleUserLogin(userId, email, eventId);
                    break;
                    
                default:
                    log.warn("Unknown user event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process user event: {}", userEvent, e);
        }
    }
    
    private void handleUserRegistration(String userId, String email, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId); // You might want to get actual name from user service
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.USER_REGISTRATION, templateData, eventId);
            
            log.info("User registration notification created for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to create user registration notification for user: {}", userId, e);
        }
    }
    
    private void handlePasswordResetRequest(String userId, String email, Map<String, Object> userEvent, String eventId) {
        try {
            String resetToken = (String) userEvent.get("resetToken");
            String resetLink = (String) userEvent.get("resetLink");
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("resetLink", resetLink);
            templateData.put("resetToken", resetToken);
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.PASSWORD_RESET, templateData, eventId);
            
            log.info("Password reset notification created for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to create password reset notification for user: {}", userId, e);
        }
    }
    
    private void handleUserLogin(String userId, String email, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("loginTime", System.currentTimeMillis());
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.USER_LOGIN, templateData, eventId);
            
            log.info("User login notification created for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to create user login notification for user: {}", userId, e);
        }
    }
} 