package com.gitanjsheth.notificationservice.controllers;

import com.gitanjsheth.notificationservice.dtos.CreateNotificationDto;
import com.gitanjsheth.notificationservice.dtos.NotificationDto;
import com.gitanjsheth.notificationservice.models.NotificationStatus;
import com.gitanjsheth.notificationservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> createNotification(@Valid @RequestBody CreateNotificationDto createNotificationDto) {
        try {
            NotificationDto notification = notificationService.createNotification(createNotificationDto);
            if (notification != null) {
                return ResponseEntity.ok(notification);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("Failed to create notification", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{notificationId}/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> sendNotification(@PathVariable Long notificationId) {
        try {
            NotificationDto notification = notificationService.sendNotification(notificationId);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", notificationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("authentication.principal.username == #userId or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @PathVariable String userId, 
            Pageable pageable) {
        try {
            Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Failed to get notifications for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByStatus(
            @PathVariable NotificationStatus status, 
            Pageable pageable) {
        try {
            Page<NotificationDto> notifications = notificationService.getNotificationsByStatus(status, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Failed to get notifications by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
} 