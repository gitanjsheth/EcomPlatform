package com.gitanjsheth.paymentservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    @KafkaListener(topics = "order.events", groupId = "payment-service")
    public void handleOrderEvent(Map<String, Object> orderEvent) {
        try {
            String eventType = (String) orderEvent.get("eventType");
            String orderId = (String) orderEvent.get("orderId");

            // For demo: only trigger payment on CREATED
            if ("CREATED".equals(eventType)) {
                log.info("Received order CREATED event for order {}. Simulating payment...", orderId);

                // Simple simulation: odd/even millis to decide success
                boolean success = (System.currentTimeMillis() % 2) == 0;

                if (success) {
                    Map<String, Object> paymentCompleted = new HashMap<>();
                    paymentCompleted.put("eventType", "PAYMENT_COMPLETED");
                    paymentCompleted.put("orderId", orderId);
                    paymentCompleted.put("paymentId", "PAY_" + System.currentTimeMillis());
                    paymentCompleted.put("timestamp", System.currentTimeMillis());
                    kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, orderId, paymentCompleted);
                    log.info("Published PAYMENT_COMPLETED for order {}", orderId);
                } else {
                    Map<String, Object> paymentFailed = new HashMap<>();
                    paymentFailed.put("eventType", "PAYMENT_FAILED");
                    paymentFailed.put("orderId", orderId);
                    paymentFailed.put("reason", "Simulated failure");
                    paymentFailed.put("timestamp", System.currentTimeMillis());
                    kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, orderId, paymentFailed);
                    log.info("Published PAYMENT_FAILED for order {}", orderId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process order event: {}", orderEvent, e);
        }
    }
}


