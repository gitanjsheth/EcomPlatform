package com.gitanjsheth.cartservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CART_EVENTS_TOPIC = "cart.events";

    public void publishCartEvent(String eventType, Long userId, String sessionId, Long productId, Integer quantity) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("userId", userId);
            event.put("sessionId", sessionId);
            event.put("productId", productId);
            event.put("quantity", quantity);
            event.put("timestamp", System.currentTimeMillis());

            String key = userId != null ? String.valueOf(userId) : sessionId;
            kafkaTemplate.send(CART_EVENTS_TOPIC, key, event);
            log.info("Published cart event: {} for user/session: {}/{} product {} qty {}", eventType, userId, sessionId, productId, quantity);
        } catch (Exception e) {
            log.error("Failed to publish cart event {} for user/session: {}/{}", eventType, userId, sessionId, e);
        }
    }
}


