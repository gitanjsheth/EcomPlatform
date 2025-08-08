package com.gitanjsheth.notificationservice.messaging;

import com.gitanjsheth.notificationservice.models.NotificationType;
import com.gitanjsheth.notificationservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventListener {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentEventListener.class);
    
    private final NotificationService notificationService;
    
    public PaymentEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @KafkaListener(topics = "payment.events", groupId = "notification-service")
    public void handlePaymentEvent(Map<String, Object> paymentEvent) {
        try {
            String eventType = (String) paymentEvent.get("eventType");
            String userId = (String) paymentEvent.get("userId");
            String email = (String) paymentEvent.get("email");
            String eventId = (String) paymentEvent.get("eventId");
            
            log.info("Received payment event: {} for user: {}", eventType, userId);
            
            switch (eventType) {
                case "PAYMENT_COMPLETED":
                    handlePaymentCompleted(userId, email, paymentEvent, eventId);
                    break;
                    
                case "PAYMENT_FAILED":
                    handlePaymentFailed(userId, email, paymentEvent, eventId);
                    break;
                    
                default:
                    log.warn("Unknown payment event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", paymentEvent, e);
        }
    }
    
    private void handlePaymentCompleted(String userId, String email, Map<String, Object> paymentEvent, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("orderNumber", paymentEvent.get("orderNumber"));
            templateData.put("paymentAmount", paymentEvent.get("amount"));
            templateData.put("paymentDate", formatDate(paymentEvent.get("paymentDate")));
            templateData.put("paymentMethod", paymentEvent.get("paymentMethod"));
            templateData.put("paymentId", paymentEvent.get("paymentId"));
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.PAYMENT_CONFIRMATION, templateData, eventId);
            
            log.info("Payment confirmation notification created for user: {} order: {}", 
                    userId, paymentEvent.get("orderNumber"));
            
        } catch (Exception e) {
            log.error("Failed to create payment confirmation notification for user: {}", userId, e);
        }
    }
    
    private void handlePaymentFailed(String userId, String email, Map<String, Object> paymentEvent, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("orderNumber", paymentEvent.get("orderNumber"));
            templateData.put("paymentAmount", paymentEvent.get("amount"));
            templateData.put("paymentDate", formatDate(paymentEvent.get("paymentDate")));
            templateData.put("paymentMethod", paymentEvent.get("paymentMethod"));
            templateData.put("failureReason", paymentEvent.get("failureReason"));
            templateData.put("paymentId", paymentEvent.get("paymentId"));
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.PAYMENT_FAILED, templateData, eventId);
            
            log.info("Payment failed notification created for user: {} order: {}", 
                    userId, paymentEvent.get("orderNumber"));
            
        } catch (Exception e) {
            log.error("Failed to create payment failed notification for user: {}", userId, e);
        }
    }
    
    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        if (dateObj instanceof String) {
            return (String) dateObj;
        }
        
        if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        return dateObj.toString();
    }
} 