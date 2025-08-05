package com.gitanjsheth.ordermanagementservice.messaging;

import com.gitanjsheth.ordermanagementservice.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    
    private final OrderService orderService;
    
    @KafkaListener(topics = "payment.events", groupId = "order-management-service")
    public void handlePaymentEvent(Map<String, Object> paymentEvent) {
        try {
            String eventType = (String) paymentEvent.get("eventType");
            String orderId = (String) paymentEvent.get("orderId");
            
            log.info("Received payment event: {} for order: {}", eventType, orderId);
            
            switch (eventType) {
                case "PAYMENT_COMPLETED":
                    String paymentId = (String) paymentEvent.get("paymentId");
                    orderService.handlePaymentCompleted(orderId, paymentId);
                    break;
                    
                case "PAYMENT_FAILED":
                    String reason = (String) paymentEvent.get("reason");
                    orderService.handlePaymentFailed(orderId, reason);
                    break;
                    
                default:
                    log.warn("Unknown payment event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", paymentEvent, e);
        }
    }
}