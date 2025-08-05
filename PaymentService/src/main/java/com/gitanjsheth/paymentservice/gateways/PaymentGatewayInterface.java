package com.gitanjsheth.paymentservice.gateways;

import com.gitanjsheth.paymentservice.dtos.PaymentRequestDto;
import com.gitanjsheth.paymentservice.models.Payment;
import com.gitanjsheth.paymentservice.models.PaymentMethod;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayInterface {
    
    /**
     * Process a payment using the gateway
     */
    PaymentGatewayResponse processPayment(PaymentRequestDto request, Payment payment);
    
    /**
     * Create a payment method (save card for future use)
     */
    PaymentMethodResponse createPaymentMethod(PaymentRequestDto request, Long userId);
    
    /**
     * Process payment using existing payment method
     */
    PaymentGatewayResponse processPaymentWithSavedMethod(PaymentMethod paymentMethod, BigDecimal amount, String currency, String orderId);
    
    /**
     * Refund a payment
     */
    RefundResponse refundPayment(String gatewayTransactionId, BigDecimal amount, String reason);
    
    /**
     * Capture a payment (for two-step payments)
     */
    PaymentGatewayResponse capturePayment(String gatewayTransactionId, BigDecimal amount);
    
    /**
     * Cancel/void a payment
     */
    PaymentGatewayResponse cancelPayment(String gatewayTransactionId);
    
    /**
     * Verify webhook signature
     */
    boolean verifyWebhookSignature(String payload, String signature, String secret);
    
    /**
     * Parse webhook event
     */
    WebhookEvent parseWebhookEvent(String payload);
    
    // Response classes
    
    class PaymentGatewayResponse {
        public boolean success;
        public String transactionId;
        public String status;
        public String errorCode;
        public String errorMessage;
        public Map<String, Object> rawResponse;
        public String clientSecret; // For frontend integration
        public boolean requiresAction;
        public String actionUrl;
        
        public PaymentGatewayResponse(boolean success) {
            this.success = success;
        }
        
        public static PaymentGatewayResponse success(String transactionId, String status) {
            PaymentGatewayResponse response = new PaymentGatewayResponse(true);
            response.transactionId = transactionId;
            response.status = status;
            return response;
        }
        
        public static PaymentGatewayResponse failure(String errorCode, String errorMessage) {
            PaymentGatewayResponse response = new PaymentGatewayResponse(false);
            response.errorCode = errorCode;
            response.errorMessage = errorMessage;
            return response;
        }
    }
    
    class PaymentMethodResponse {
        public boolean success;
        public String paymentMethodId;
        public String cardLastFour;
        public String cardBrand;
        public Integer expiryMonth;
        public Integer expiryYear;
        public String errorCode;
        public String errorMessage;
        
        public PaymentMethodResponse(boolean success) {
            this.success = success;
        }
        
        public static PaymentMethodResponse success(String paymentMethodId, String cardLastFour, String cardBrand, Integer expiryMonth, Integer expiryYear) {
            PaymentMethodResponse response = new PaymentMethodResponse(true);
            response.paymentMethodId = paymentMethodId;
            response.cardLastFour = cardLastFour;
            response.cardBrand = cardBrand;
            response.expiryMonth = expiryMonth;
            response.expiryYear = expiryYear;
            return response;
        }
        
        public static PaymentMethodResponse failure(String errorCode, String errorMessage) {
            PaymentMethodResponse response = new PaymentMethodResponse(false);
            response.errorCode = errorCode;
            response.errorMessage = errorMessage;
            return response;
        }
    }
    
    class RefundResponse {
        public boolean success;
        public String refundId;
        public BigDecimal amount;
        public String status;
        public String errorCode;
        public String errorMessage;
        
        public RefundResponse(boolean success) {
            this.success = success;
        }
        
        public static RefundResponse success(String refundId, BigDecimal amount, String status) {
            RefundResponse response = new RefundResponse(true);
            response.refundId = refundId;
            response.amount = amount;
            response.status = status;
            return response;
        }
        
        public static RefundResponse failure(String errorCode, String errorMessage) {
            RefundResponse response = new RefundResponse(false);
            response.errorCode = errorCode;
            response.errorMessage = errorMessage;
            return response;
        }
    }
    
    class WebhookEvent {
        public String eventId;
        public String eventType;
        public String paymentId;
        public String status;
        public Map<String, Object> data;
        
        public WebhookEvent(String eventId, String eventType) {
            this.eventId = eventId;
            this.eventType = eventType;
        }
    }
}