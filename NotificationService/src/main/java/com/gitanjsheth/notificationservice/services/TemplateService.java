package com.gitanjsheth.notificationservice.services;

import com.gitanjsheth.notificationservice.models.NotificationTemplate;
import com.gitanjsheth.notificationservice.models.NotificationType;

import java.util.Map;

public interface TemplateService {
    
    /**
     * Process a template with given data
     * @param templateName Name of the template
     * @param data Template variables
     * @return Processed template content
     */
    String processTemplate(String templateName, Map<String, Object> data);
    
    /**
     * Get template by name
     * @param templateName Name of the template
     * @return Template if found
     */
    NotificationTemplate getTemplate(String templateName);
    
    /**
     * Get default template for notification type
     * @param notificationType Type of notification
     * @return Default template for the type
     */
    NotificationTemplate getDefaultTemplate(NotificationType notificationType);
    
    /**
     * Create or update a template
     * @param template Template to save
     * @return Saved template
     */
    NotificationTemplate saveTemplate(NotificationTemplate template);
} 