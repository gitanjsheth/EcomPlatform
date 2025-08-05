package com.gitanjsheth.paymentservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class WebhookEventDto {
    
    private String id;
    private String type; // Event type from gateway
    private String gatewayEventId; // Original event ID from gateway
    private Object data; // Event data from gateway
    private String signature; // Webhook signature for verification
    private LocalDateTime eventCreatedAt;
    private LocalDateTime receivedAt;
    private Boolean processed = false;
    private String processingResult;
    private String errorMessage;
    
    // Stripe specific fields
    private String stripeAccount;
    private String apiVersion;
    private Boolean liveMode;
    private Integer pendingWebhooks;
    
    // Common event types
    public boolean isPaymentSucceeded() {
        return "payment_intent.succeeded".equals(type) || 
               "charge.succeeded".equals(type);
    }
    
    public boolean isPaymentFailed() {
        return "payment_intent.payment_failed".equals(type) || 
               "charge.failed".equals(type);
    }
    
    public boolean isPaymentCanceled() {
        return "payment_intent.canceled".equals(type);
    }
    
    public boolean isRefundCreated() {
        return "charge.dispute.created".equals(type) || 
               "refund.created".equals(type);
    }
    
    // Extract payment ID from webhook data
    public String extractPaymentId() {
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object object = dataMap.get("object");
            
            if (object instanceof Map) {
                Map<String, Object> objectMap = (Map<String, Object>) object;
                
                // Try different fields where payment ID might be stored
                String paymentId = (String) objectMap.get("payment_intent");
                if (paymentId == null) {
                    paymentId = (String) objectMap.get("id");
                }
                if (paymentId == null && objectMap.get("charges") instanceof Map) {
                    Map<String, Object> charges = (Map<String, Object>) objectMap.get("charges");
                    if (charges.get("data") instanceof java.util.List) {
                        java.util.List<Map<String, Object>> chargesList = 
                            (java.util.List<Map<String, Object>>) charges.get("data");
                        if (!chargesList.isEmpty()) {
                            paymentId = (String) chargesList.get(0).get("payment_intent");
                        }
                    }
                }
                
                return paymentId;
            }
        }
        
        return null;
    }
}