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
import com.gitanjsheth.paymentservice.gateways.StripePaymentGateway;

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

    @Autowired
    public PaymentController(KafkaTemplate<String, Object> kafkaTemplate,
                             StripePaymentGateway stripePaymentGateway) {
        this.kafkaTemplate = kafkaTemplate;
        this.stripePaymentGateway = stripePaymentGateway;
    }

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
}