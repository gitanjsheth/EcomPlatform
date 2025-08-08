package com.gitanjsheth.userauthservice.controllers;

import com.gitanjsheth.userauthservice.models.User;
import com.gitanjsheth.userauthservice.repositories.UserRepository;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String USER_EVENTS_TOPIC = "user.events";

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Do not leak existence; respond ok
            return ResponseEntity.ok(Map.of("status", "ok"));
        }

        String resetToken = UUID.randomUUID().toString();
        String resetLink = "https://frontend/reset?token=" + resetToken + "&email=" + user.getEmail();

        // Store token with TTL (15 minutes)
        String key = "pwdreset:" + resetToken;
        redisTemplate.opsForValue().set(key, String.valueOf(user.getId()), java.time.Duration.ofMinutes(15));

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PASSWORD_RESET_REQUESTED");
        event.put("userId", String.valueOf(user.getId()));
        event.put("email", user.getEmail());
        event.put("resetToken", resetToken);
        event.put("resetLink", resetLink);
        event.put("eventId", UUID.randomUUID().toString());

        kafkaTemplate.send(USER_EVENTS_TOPIC, String.valueOf(user.getId()), event);
        log.info("Published PASSWORD_RESET_REQUESTED for user {}", user.getId());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (email == null || token == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email, token and newPassword are required"));
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "user not found"));
        }
        String key = "pwdreset:" + token;
        String storedUserId = redisTemplate.opsForValue().get(key);
        if (storedUserId == null || !storedUserId.equals(String.valueOf(user.getId()))) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid or expired token"));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        // Invalidate token after use
        redisTemplate.delete(key);
        return ResponseEntity.ok(Map.of("status", "password_updated"));
    }
}


