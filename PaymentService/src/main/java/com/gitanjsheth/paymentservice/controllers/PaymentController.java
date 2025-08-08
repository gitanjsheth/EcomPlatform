package com.gitanjsheth.paymentservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${app.service.token:}")
    private String configuredServiceToken;

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

        Map<String, Object> response = Map.of(
            "paymentId", "PAY_" + System.currentTimeMillis(),
            "orderId", orderId,
            "status", success ? "COMPLETED" : "FAILED",
            "amount", amount,
            "currency", currency,
            "timestamp", System.currentTimeMillis()
        );

        log.info("Processed payment for order {} amount {} {} -> {}", orderId, amount, currency, response.get("status"));
        return ResponseEntity.ok(response);
    }

    // Webhook endpoint for payment gateway callbacks
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> webhookData) {
        log.info("Received webhook: {}", webhookData);
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