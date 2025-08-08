package com.gitanjsheth.notificationservice.repositories;

import com.gitanjsheth.notificationservice.models.NotificationTemplate;
import com.gitanjsheth.notificationservice.models.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByTemplateNameAndIsActiveTrue(String templateName);
    
    List<NotificationTemplate> findByNotificationTypeAndIsActiveTrue(NotificationType notificationType);
    
    List<NotificationTemplate> findByIsActiveTrue();
    
    boolean existsByTemplateName(String templateName);
} 