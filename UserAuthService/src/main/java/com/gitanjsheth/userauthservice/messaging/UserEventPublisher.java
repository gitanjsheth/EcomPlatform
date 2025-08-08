package com.gitanjsheth.userauthservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String USER_EVENTS_TOPIC = "user.events";

    public void publishUserRegistered(Long userId, String email) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_REGISTERED");
        event.put("userId", String.valueOf(userId));
        event.put("email", email);
        event.put("eventId", UUID.randomUUID().toString());
        kafkaTemplate.send(USER_EVENTS_TOPIC, String.valueOf(userId), event);
        log.info("Published USER_REGISTERED for {}", userId);
    }

    public void publishUserLogin(Long userId, String email) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_LOGIN");
        event.put("userId", String.valueOf(userId));
        event.put("email", email);
        event.put("eventId", UUID.randomUUID().toString());
        kafkaTemplate.send(USER_EVENTS_TOPIC, String.valueOf(userId), event);
        log.info("Published USER_LOGIN for {}", userId);
    }
}


