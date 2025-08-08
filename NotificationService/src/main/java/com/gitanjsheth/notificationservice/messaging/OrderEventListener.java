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
public class OrderEventListener {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderEventListener.class);
    
    private final NotificationService notificationService;
    
    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @KafkaListener(topics = "order.events", groupId = "notification-service")
    public void handleOrderEvent(Map<String, Object> orderEvent) {
        try {
            String eventType = (String) orderEvent.get("eventType");
            String userId = (String) orderEvent.get("userId");
            String email = (String) orderEvent.get("email");
            String eventId = (String) orderEvent.get("eventId");
            
            log.info("Received order event: {} for user: {}", eventType, userId);
            
            switch (eventType) {
                case "ORDER_CREATED":
                    handleOrderCreated(userId, email, orderEvent, eventId);
                    break;
                    
                case "ORDER_STATUS_UPDATED":
                    handleOrderStatusUpdated(userId, email, orderEvent, eventId);
                    break;
                    
                case "ORDER_SHIPPED":
                    handleOrderShipped(userId, email, orderEvent, eventId);
                    break;
                    
                case "ORDER_DELIVERED":
                    handleOrderDelivered(userId, email, orderEvent, eventId);
                    break;
                    
                default:
                    log.warn("Unknown order event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process order event: {}", orderEvent, e);
        }
    }
    
    private void handleOrderCreated(String userId, String email, Map<String, Object> orderEvent, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("orderNumber", orderEvent.get("orderNumber"));
            templateData.put("orderDate", formatDate(orderEvent.get("orderDate")));
            templateData.put("totalAmount", orderEvent.get("totalAmount"));
            templateData.put("orderStatus", orderEvent.get("orderStatus"));
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.ORDER_CONFIRMATION, templateData, eventId);
            
            log.info("Order confirmation notification created for user: {} order: {}", 
                    userId, orderEvent.get("orderNumber"));
            
        } catch (Exception e) {
            log.error("Failed to create order confirmation notification for user: {}", userId, e);
        }
    }
    
    private void handleOrderStatusUpdated(String userId, String email, Map<String, Object> orderEvent, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("orderNumber", orderEvent.get("orderNumber"));
            templateData.put("newStatus", orderEvent.get("newStatus"));
            templateData.put("updateDate", formatDate(orderEvent.get("updateDate")));
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.ORDER_STATUS_UPDATE, templateData, eventId);
            
            log.info("Order status update notification created for user: {} order: {}", 
                    userId, orderEvent.get("orderNumber"));
            
        } catch (Exception e) {
            log.error("Failed to create order status update notification for user: {}", userId, e);
        }
    }
    
    private void handleOrderShipped(String userId, String email, Map<String, Object> orderEvent, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("orderNumber", orderEvent.get("orderNumber"));
            templateData.put("newStatus", "SHIPPED");
            templateData.put("updateDate", formatDate(orderEvent.get("shippedDate")));
            templateData.put("trackingNumber", orderEvent.get("trackingNumber"));
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.SHIPPING_UPDATE, templateData, eventId);
            
            log.info("Order shipped notification created for user: {} order: {}", 
                    userId, orderEvent.get("orderNumber"));
            
        } catch (Exception e) {
            log.error("Failed to create order shipped notification for user: {}", userId, e);
        }
    }
    
    private void handleOrderDelivered(String userId, String email, Map<String, Object> orderEvent, String eventId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userId);
            templateData.put("orderNumber", orderEvent.get("orderNumber"));
            templateData.put("newStatus", "DELIVERED");
            templateData.put("updateDate", formatDate(orderEvent.get("deliveredDate")));
            
            notificationService.createNotificationFromTemplate(
                    userId, email, NotificationType.DELIVERY_CONFIRMATION, templateData, eventId);
            
            log.info("Order delivered notification created for user: {} order: {}", 
                    userId, orderEvent.get("orderNumber"));
            
        } catch (Exception e) {
            log.error("Failed to create order delivered notification for user: {}", userId, e);
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