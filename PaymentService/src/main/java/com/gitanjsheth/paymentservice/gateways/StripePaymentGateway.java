package com.gitanjsheth.paymentservice.gateways;

import com.gitanjsheth.paymentservice.dtos.PaymentRequestDto;
import com.gitanjsheth.paymentservice.models.Payment;
import com.gitanjsheth.paymentservice.models.PaymentMethod;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StripePaymentGateway implements PaymentGatewayInterface {
    
    @Value("${app.payment.gateway.api-key}")
    private String stripeApiKey;
    
    @Value("${app.payment.gateway.webhook-secret}")
    private String webhookSecret;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe gateway initialized");
    }
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentRequestDto request, Payment payment) {
        try {
            log.info("Processing Stripe payment for order: {}", request.getOrderId());
            
            // Create customer if needed
            Customer customer = createOrGetCustomer(request.getUserId(), request.getOrderId());
            
            // Create payment intent
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(convertToStripeAmount(request.getAmount()))
                .setCurrency(request.getCurrency().toLowerCase())
                .setCustomer(customer.getId())
                .putMetadata("order_id", request.getOrderId())
                .putMetadata("user_id", request.getUserId().toString())
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL);
            
            // Add description
            if (request.getDescription() != null) {
                paramsBuilder.setDescription(request.getDescription());
            }
            
            // Handle payment method
            if (request.hasExistingPaymentMethod()) {
                // Use existing payment method
                PaymentMethod existingMethod = getPaymentMethodFromDatabase(request.getPaymentMethodId());
                paramsBuilder.setPaymentMethod(existingMethod.getGatewayToken());
                paramsBuilder.setConfirm(true);
            } else if (request.hasNewCardDetails()) {
                // Create new payment method from card details
                com.stripe.model.PaymentMethod stripePaymentMethod = createStripePaymentMethod(request);
                paramsBuilder.setPaymentMethod(stripePaymentMethod.getId());
                
                if (request.getSavePaymentMethod()) {
                    // Attach to customer for future use
                    stripePaymentMethod.attach(PaymentMethodAttachParams.builder()
                        .setCustomer(customer.getId())
                        .build());
                }
                
                paramsBuilder.setConfirm(true);
            }
            
            PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());
            
            log.info("Stripe PaymentIntent created: {} with status: {}", intent.getId(), intent.getStatus());
            
            // Handle different statuses
            switch (intent.getStatus()) {
                case "succeeded":
                    return PaymentGatewayResponse.success(intent.getId(), "COMPLETED");
                    
                case "requires_action":
                case "requires_source_action":
                    PaymentGatewayResponse actionResponse = PaymentGatewayResponse.success(intent.getId(), "REQUIRES_ACTION");
                    actionResponse.requiresAction = true;
                    actionResponse.clientSecret = intent.getClientSecret();
                    if (intent.getNextAction() != null && intent.getNextAction().getRedirectToUrl() != null) {
                        actionResponse.actionUrl = intent.getNextAction().getRedirectToUrl().getUrl();
                    }
                    return actionResponse;
                    
                case "requires_payment_method":
                    return PaymentGatewayResponse.failure("REQUIRES_PAYMENT_METHOD", "Payment method required");
                    
                case "processing":
                    return PaymentGatewayResponse.success(intent.getId(), "PROCESSING");
                    
                default:
                    return PaymentGatewayResponse.failure("UNKNOWN_STATUS", "Unknown payment status: " + intent.getStatus());
            }
            
        } catch (StripeException e) {
            log.error("Stripe payment failed for order {}: {}", request.getOrderId(), e.getMessage(), e);
            return PaymentGatewayResponse.failure(e.getCode(), e.getUserMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing Stripe payment for order {}: {}", request.getOrderId(), e.getMessage(), e);
            return PaymentGatewayResponse.failure("INTERNAL_ERROR", "An unexpected error occurred");
        }
    }
    
    @Override
    public PaymentMethodResponse createPaymentMethod(PaymentRequestDto request, Long userId) {
        try {
            log.info("Creating Stripe payment method for user: {}", userId);
            
            // Create customer if needed
            Customer customer = createOrGetCustomer(userId, "payment_method_creation");
            
            // Create payment method
            com.stripe.model.PaymentMethod stripePaymentMethod = createStripePaymentMethod(request);
            
            // Attach to customer
            stripePaymentMethod.attach(PaymentMethodAttachParams.builder()
                .setCustomer(customer.getId())
                .build());
            
            // Extract card details
            com.stripe.model.PaymentMethod.Card card = stripePaymentMethod.getCard();
            
            log.info("Stripe payment method created: {}", stripePaymentMethod.getId());
            
            return PaymentMethodResponse.success(
                stripePaymentMethod.getId(),
                card.getLast4(),
                card.getBrand(),
                Math.toIntExact(card.getExpMonth()),
                Math.toIntExact(card.getExpYear())
            );
            
        } catch (StripeException e) {
            log.error("Failed to create Stripe payment method for user {}: {}", userId, e.getMessage(), e);
            return PaymentMethodResponse.failure(e.getCode(), e.getUserMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating Stripe payment method for user {}: {}", userId, e.getMessage(), e);
            return PaymentMethodResponse.failure("INTERNAL_ERROR", "An unexpected error occurred");
        }
    }
    
    @Override
    public PaymentGatewayResponse processPaymentWithSavedMethod(PaymentMethod paymentMethod, BigDecimal amount, String currency, String orderId) {
        try {
            log.info("Processing Stripe payment with saved method for order: {}", orderId);
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(convertToStripeAmount(amount))
                .setCurrency(currency.toLowerCase())
                .setPaymentMethod(paymentMethod.getGatewayToken())
                .setConfirm(true)
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .putMetadata("order_id", orderId)
                .putMetadata("user_id", paymentMethod.getUserId().toString())
                .build();
            
            PaymentIntent intent = PaymentIntent.create(params);
            
            log.info("Stripe PaymentIntent created with saved method: {} with status: {}", intent.getId(), intent.getStatus());
            
            if ("succeeded".equals(intent.getStatus())) {
                return PaymentGatewayResponse.success(intent.getId(), "COMPLETED");
            } else {
                return PaymentGatewayResponse.failure("PAYMENT_FAILED", "Payment failed with status: " + intent.getStatus());
            }
            
        } catch (StripeException e) {
            log.error("Stripe payment with saved method failed for order {}: {}", orderId, e.getMessage(), e);
            return PaymentGatewayResponse.failure(e.getCode(), e.getUserMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing Stripe payment with saved method for order {}: {}", orderId, e.getMessage(), e);
            return PaymentGatewayResponse.failure("INTERNAL_ERROR", "An unexpected error occurred");
        }
    }
    
    @Override
    public RefundResponse refundPayment(String gatewayTransactionId, BigDecimal amount, String reason) {
        try {
            log.info("Creating Stripe refund for payment: {} amount: {}", gatewayTransactionId, amount);
            
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(gatewayTransactionId);
            
            if (amount != null) {
                paramsBuilder.setAmount(convertToStripeAmount(amount));
            }
            
            if (reason != null) {
                paramsBuilder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
                paramsBuilder.putMetadata("reason", reason);
            }
            
            Refund refund = Refund.create(paramsBuilder.build());
            
            log.info("Stripe refund created: {} with status: {}", refund.getId(), refund.getStatus());
            
            return RefundResponse.success(
                refund.getId(),
                convertFromStripeAmount(refund.getAmount()),
                refund.getStatus()
            );
            
        } catch (StripeException e) {
            log.error("Stripe refund failed for payment {}: {}", gatewayTransactionId, e.getMessage(), e);
            return RefundResponse.failure(e.getCode(), e.getUserMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating Stripe refund for payment {}: {}", gatewayTransactionId, e.getMessage(), e);
            return RefundResponse.failure("INTERNAL_ERROR", "An unexpected error occurred");
        }
    }
    
    @Override
    public PaymentGatewayResponse capturePayment(String gatewayTransactionId, BigDecimal amount) {
        try {
            log.info("Capturing Stripe payment: {} amount: {}", gatewayTransactionId, amount);
            
            PaymentIntent intent = PaymentIntent.retrieve(gatewayTransactionId);
            
            PaymentIntentCaptureParams.Builder paramsBuilder = PaymentIntentCaptureParams.builder();
            if (amount != null) {
                paramsBuilder.setAmountToCapture(convertToStripeAmount(amount));
            }
            
            intent = intent.capture(paramsBuilder.build());
            
            log.info("Stripe payment captured: {} with status: {}", intent.getId(), intent.getStatus());
            
            return PaymentGatewayResponse.success(intent.getId(), intent.getStatus().toUpperCase());
            
        } catch (StripeException e) {
            log.error("Stripe capture failed for payment {}: {}", gatewayTransactionId, e.getMessage(), e);
            return PaymentGatewayResponse.failure(e.getCode(), e.getUserMessage());
        } catch (Exception e) {
            log.error("Unexpected error capturing Stripe payment {}: {}", gatewayTransactionId, e.getMessage(), e);
            return PaymentGatewayResponse.failure("INTERNAL_ERROR", "An unexpected error occurred");
        }
    }
    
    @Override
    public PaymentGatewayResponse cancelPayment(String gatewayTransactionId) {
        try {
            log.info("Canceling Stripe payment: {}", gatewayTransactionId);
            
            PaymentIntent intent = PaymentIntent.retrieve(gatewayTransactionId);
            intent = intent.cancel();
            
            log.info("Stripe payment canceled: {} with status: {}", intent.getId(), intent.getStatus());
            
            return PaymentGatewayResponse.success(intent.getId(), intent.getStatus().toUpperCase());
            
        } catch (StripeException e) {
            log.error("Stripe cancellation failed for payment {}: {}", gatewayTransactionId, e.getMessage(), e);
            return PaymentGatewayResponse.failure(e.getCode(), e.getUserMessage());
        } catch (Exception e) {
            log.error("Unexpected error canceling Stripe payment {}: {}", gatewayTransactionId, e.getMessage(), e);
            return PaymentGatewayResponse.failure("INTERNAL_ERROR", "An unexpected error occurred");
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {
        try {
            Webhook.constructEvent(payload, signature, secret != null ? secret : webhookSecret);
            return true;
        } catch (Exception e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public WebhookEvent parseWebhookEvent(String payload) {
        try {
            // Parse the JSON payload manually for webhook events
            // In production, you might want to use a more sophisticated JSON parser
            WebhookEvent webhookEvent = new WebhookEvent("webhook_event", "payment.webhook");
            
            // Basic parsing - in a real implementation, you'd parse the JSON properly
            if (payload.contains("payment_intent.succeeded")) {
                webhookEvent.eventType = "payment_intent.succeeded";
            } else if (payload.contains("payment_intent.payment_failed")) {
                webhookEvent.eventType = "payment_intent.payment_failed";
            } else if (payload.contains("charge.succeeded")) {
                webhookEvent.eventType = "charge.succeeded";
            } else if (payload.contains("charge.failed")) {
                webhookEvent.eventType = "charge.failed";
            }
            
            // For now, return a basic webhook event
            // In production, you'd properly parse the Stripe Event object
            log.info("Parsed webhook event type: {}", webhookEvent.eventType);
            
            return webhookEvent;
            
        } catch (Exception e) {
            log.error("Failed to parse Stripe webhook event: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // Helper methods
    
    private Customer createOrGetCustomer(Long userId, String context) throws StripeException {
        // In a real implementation, you might want to store Stripe customer IDs
        // For now, create a new customer each time or implement caching
        CustomerCreateParams params = CustomerCreateParams.builder()
            .putMetadata("user_id", userId.toString())
            .putMetadata("context", context)
            .build();
        
        return Customer.create(params);
    }
    
    private com.stripe.model.PaymentMethod createStripePaymentMethod(PaymentRequestDto request) throws StripeException {
        PaymentMethodCreateParams.Builder paramsBuilder = PaymentMethodCreateParams.builder()
            .setType(PaymentMethodCreateParams.Type.CARD)
            .setCard(PaymentMethodCreateParams.CardDetails.builder()
                .setNumber(request.getCardNumber())
                .setExpMonth(Long.parseLong(request.getExpiryMonth()))
                .setExpYear(Long.parseLong(request.getExpiryYear()))
                .setCvc(request.getCvv())
                .build());
        
        // Add billing details if provided
        if (request.getBillingAddressLine1() != null) {
            PaymentMethodCreateParams.BillingDetails.Builder billingBuilder = PaymentMethodCreateParams.BillingDetails.builder()
                .setName(request.getCardholderName())
                .setAddress(PaymentMethodCreateParams.BillingDetails.Address.builder()
                    .setLine1(request.getBillingAddressLine1())
                    .setLine2(request.getBillingAddressLine2())
                    .setCity(request.getBillingCity())
                    .setState(request.getBillingState())
                    .setPostalCode(request.getBillingZipCode())
                    .setCountry(request.getBillingCountry())
                    .build());
            
            paramsBuilder.setBillingDetails(billingBuilder.build());
        }
        
        return com.stripe.model.PaymentMethod.create(paramsBuilder.build());
    }
    
    private PaymentMethod getPaymentMethodFromDatabase(Long paymentMethodId) {
        // This should call the PaymentMethodRepository to get the saved payment method
        // For now, return a mock - this will be implemented in the service layer
        throw new UnsupportedOperationException("Payment method retrieval should be handled by service layer");
    }
    
    private Long convertToStripeAmount(BigDecimal amount) {
        // Stripe expects amounts in cents for USD
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }
    
    private BigDecimal convertFromStripeAmount(Long stripeAmount) {
        // Convert cents back to dollars
        return BigDecimal.valueOf(stripeAmount).divide(BigDecimal.valueOf(100));
    }
}