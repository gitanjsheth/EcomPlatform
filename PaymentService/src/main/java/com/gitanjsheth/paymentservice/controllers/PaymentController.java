package com.gitanjsheth.paymentservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    // Internal service endpoint for order service
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> paymentRequest) {
        // Simulate payment processing
        String orderId = (String) paymentRequest.get("orderId");
        Double amount = (Double) paymentRequest.get("amount");
        
        // Simulate success for amounts < 1000, fail for higher amounts
        boolean success = amount < 1000.0;
        
        Map<String, Object> response = Map.of(
            "paymentId", "PAY_" + System.currentTimeMillis(),
            "orderId", orderId,
            "status", success ? "COMPLETED" : "FAILED",
            "amount", amount,
            "currency", "USD",
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Webhook endpoint for payment gateway callbacks
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> webhookData) {
        // Process webhook from payment gateway
        return ResponseEntity.ok("Webhook processed");
    }
}