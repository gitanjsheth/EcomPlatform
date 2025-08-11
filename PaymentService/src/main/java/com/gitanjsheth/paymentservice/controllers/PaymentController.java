package com.gitanjsheth.paymentservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.gitanjsheth.paymentservice.gateways.StripePaymentGateway;
import com.gitanjsheth.paymentservice.services.ReceiptService;
import com.gitanjsheth.paymentservice.services.PaymentService;
import com.gitanjsheth.paymentservice.dtos.PaymentResponseDto;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${app.service.token:}")
    private String configuredServiceToken;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    private final StripePaymentGateway stripePaymentGateway;
    private final ReceiptService receiptService;
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(KafkaTemplate<String, Object> kafkaTemplate,
                             StripePaymentGateway stripePaymentGateway,
                             ReceiptService receiptService,
                             PaymentService paymentService) {
        this.kafkaTemplate = kafkaTemplate;
        this.stripePaymentGateway = stripePaymentGateway;
        this.receiptService = receiptService;
        this.paymentService = paymentService;
    }

    // ============================================================================
    // RECEIPT ENDPOINTS
    // ============================================================================
    
    /**
     * Get payment receipt as HTML
     * @param paymentId The payment ID
     * @return HTML receipt
     */
    @GetMapping(value = "/{paymentId}/receipt", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getReceiptHtml(@PathVariable String paymentId) {
        log.info("Generating HTML receipt for payment: {}", paymentId);
        
        PaymentResponseDto payment = paymentService.getPaymentByPaymentId(paymentId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!payment.isSuccessful()) {
            return ResponseEntity.badRequest().body("<html><body><p>Receipt can only be generated for successful payments</p></body></html>");
        }
        
        // Convert DTO back to model for receipt generation
        // In a real implementation, you'd have a proper mapper
        String receiptHtml = receiptService.generateReceiptHtml(convertToPaymentModel(payment));
        return ResponseEntity.ok(receiptHtml);
    }
    
    /**
     * Get payment receipt as plain text
     * @param paymentId The payment ID
     * @return Plain text receipt
     */
    @GetMapping(value = "/{paymentId}/receipt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getReceiptText(@PathVariable String paymentId) {
        log.info("Generating text receipt for payment: {}", paymentId);
        
        PaymentResponseDto payment = paymentService.getPaymentByPaymentId(paymentId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!payment.isSuccessful()) {
            return ResponseEntity.badRequest().body("Receipt can only be generated for successful payments");
        }
        
        String receiptText = receiptService.generateReceiptText(convertToPaymentModel(payment));
        return ResponseEntity.ok(receiptText);
    }
    
    /**
     * Get receipt URL for a payment
     * @param paymentId The payment ID
     * @return Receipt URL information
     */
    @GetMapping("/{paymentId}/receipt-url")
    public ResponseEntity<Map<String, Object>> getReceiptUrl(@PathVariable String paymentId) {
        log.info("Getting receipt URL for payment: {}", paymentId);
        
        PaymentResponseDto payment = paymentService.getPaymentByPaymentId(paymentId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!payment.isSuccessful()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Receipt can only be generated for successful payments",
                "paymentId", paymentId,
                "status", payment.getStatus()
            ));
        }
        
        String receiptUrl = receiptService.getReceiptUrl(paymentId);
        return ResponseEntity.ok(Map.of(
            "paymentId", paymentId,
            "receiptUrl", receiptUrl,
            "formats", Map.of(
                "html", "/api/payments/" + paymentId + "/receipt",
                "text", "/api/payments/" + paymentId + "/receipt"
            )
        ));
    }
    
    /**
     * Generate receipt for a payment (admin/internal endpoint)
     * @param paymentId The payment ID
     * @param request HTTP request for authorization
     * @return Receipt generation result
     */
    @PostMapping("/{paymentId}/generate-receipt")
    public ResponseEntity<Map<String, Object>> generateReceipt(@PathVariable String paymentId,
                                                              HttpServletRequest request) {
        if (!isAuthorizedServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        log.info("Generating receipt for payment: {}", paymentId);
        
        PaymentResponseDto payment = paymentService.getPaymentByPaymentId(paymentId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!payment.isSuccessful()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Receipt can only be generated for successful payments",
                "paymentId", paymentId,
                "status", payment.getStatus()
            ));
        }
        
        String receiptId = receiptService.generateReceipt(convertToPaymentModel(payment));
        if (receiptId != null) {
            return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "receiptId", receiptId,
                "receiptUrl", receiptService.getReceiptUrl(paymentId),
                "message", "Receipt generated successfully"
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to generate receipt",
                "paymentId", paymentId
            ));
        }
    }

    // ============================================================================
    // EXISTING ENDPOINTS
    // ============================================================================

    // Internal service endpoint for order service
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> paymentRequest,
                                                              HttpServletRequest request) {
        if (!isAuthorizedServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }

        String orderId = String.valueOf(paymentRequest.get("orderId"));
        Double amount = paymentRequest.get("amount") instanceof Number
                ? ((Number) paymentRequest.get("amount")).doubleValue()
                : Double.parseDouble(String.valueOf(paymentRequest.get("amount")));
        String currency = paymentRequest.get("currency") != null ? String.valueOf(paymentRequest.get("currency")) : "USD";

        boolean success = amount < 1000.0;

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("paymentId", "PAY_" + System.currentTimeMillis());
        response.put("orderId", orderId);
        response.put("status", success ? "COMPLETED" : "FAILED");
        response.put("amount", amount);
        response.put("currency", currency);
        response.put("timestamp", System.currentTimeMillis());

        log.info("Processed payment for order {} amount {} {} -> {}", orderId, amount, currency, response.get("status"));
        // Emit payment event for OMS/Notifications
        java.util.Map<String, Object> event = new java.util.HashMap<>();
        event.put("orderId", orderId);
        event.put("eventType", success ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED");
        if (success) {
            event.put("paymentId", response.get("paymentId"));
        } else {
            event.put("reason", "Amount threshold simulation");
        }
        event.put("timestamp", System.currentTimeMillis());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, orderId, event);
        return ResponseEntity.ok(response);
    }

    // Webhook endpoint for payment gateway callbacks
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestHeader HttpHeaders headers,
                                                @RequestBody Map<String, Object> webhookData) {
        log.info("Received webhook: {}", webhookData);
        // Optional signature verification for Stripe
        String stripeSig = headers.getFirst("Stripe-Signature");
        String payload = webhookData.toString();
        if (stripeSig != null && !stripeSig.isEmpty()) {
            boolean verified = stripePaymentGateway.verifyWebhookSignature(payload, stripeSig, null);
            if (!verified) {
                return ResponseEntity.status(400).body("Invalid signature");
            }
        }
        String eventType = String.valueOf(webhookData.getOrDefault("type", ""));
        Map<String, Object> data = (Map<String, Object>) webhookData.getOrDefault("data", java.util.Map.of());
        String orderId = String.valueOf(data.getOrDefault("orderId", ""));
        if (!orderId.isEmpty()) {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            if ("payment_intent.succeeded".equals(eventType)) {
                event.put("eventType", "PAYMENT_COMPLETED");
                event.put("paymentId", data.getOrDefault("paymentId", "PAY_" + System.currentTimeMillis()));
            } else if ("payment_intent.payment_failed".equals(eventType)) {
                event.put("eventType", "PAYMENT_FAILED");
                event.put("reason", data.getOrDefault("reason", "gateway_webhook"));
            }
            if (!event.isEmpty()) {
                event.put("orderId", orderId);
                event.put("timestamp", System.currentTimeMillis());
                kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, orderId, event);
            }
        }
        return ResponseEntity.ok("Webhook processed");
    }

    // Internal: refund endpoint used by OrderManagementService
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Map<String, Object>> refund(@PathVariable String paymentId,
                                                      @RequestBody Map<String, Object> requestBody,
                                                      HttpServletRequest request) {
        if (!isAuthorizedServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        Double amount = requestBody.get("amount") instanceof Number
                ? ((Number) requestBody.get("amount")).doubleValue()
                : Double.parseDouble(String.valueOf(requestBody.get("amount")));
        String reason = requestBody.get("reason") != null ? String.valueOf(requestBody.get("reason")) : "unspecified";
        Map<String, Object> response = Map.of(
                "paymentId", paymentId,
                "status", "REFUND_INITIATED",
                "amount", amount,
                "reason", reason,
                "timestamp", System.currentTimeMillis()
        );
        log.info("Refund initiated for payment {} amount {} reason {}", paymentId, amount, reason);
        return ResponseEntity.ok(response);
    }

    private boolean isAuthorizedServiceRequest(HttpServletRequest request) {
        String token = request.getHeader("X-Service-Token");
        if (token != null && !token.isEmpty()) {
            if (configuredServiceToken != null && !configuredServiceToken.isEmpty()) {
                return configuredServiceToken.equals(token);
            }
            String envToken = System.getenv("INTERNAL_SERVICE_TOKEN");
            if (envToken != null && !envToken.isEmpty()) {
                return envToken.equals(token);
            }
            return "internal-service-secret-2024".equals(token);
        }
        return false;
    }
    
    // Helper method to convert DTO back to model (simplified)
    private com.gitanjsheth.paymentservice.models.Payment convertToPaymentModel(PaymentResponseDto dto) {
        com.gitanjsheth.paymentservice.models.Payment payment = new com.gitanjsheth.paymentservice.models.Payment();
        payment.setPaymentId(dto.getPaymentId());
        payment.setOrderId(dto.getOrderId());
        payment.setUserId(dto.getUserId());
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency());
        payment.setStatus(dto.getStatus());
        payment.setGateway(dto.getGateway());
        payment.setGatewayTransactionId(dto.getGatewayTransactionId());
        payment.setProcessedAt(dto.getProcessedAt());
        payment.setFailedAt(dto.getFailedAt());
        payment.setFailureReason(dto.getFailureReason());
        payment.setRefundedAmount(dto.getRefundedAmount());
        payment.setRefundedAt(dto.getRefundedAt());
        payment.setRetryCount(dto.getRetryCount());
        payment.setWebhookReceived(dto.getWebhookReceived());
        payment.setCreatedAt(dto.getCreatedAt());
        payment.setUpdatedAt(dto.getUpdatedAt());
        return payment;
    }
}