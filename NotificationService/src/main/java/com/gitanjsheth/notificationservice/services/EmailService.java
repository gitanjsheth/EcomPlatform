package com.gitanjsheth.notificationservice.services;

import com.gitanjsheth.notificationservice.models.Notification;

public interface EmailService {
    
    /**
     * Send an email notification using AWS SES
     * @param notification The notification to send
     * @return AWS message ID if successful
     * @throws Exception if sending fails
     */
    String sendEmail(Notification notification) throws Exception;
    
    /**
     * Send a simple email without using templates
     * @param to Recipient email
     * @param subject Email subject
     * @param htmlContent HTML content
     * @param textContent Plain text content
     * @return AWS message ID if successful
     * @throws Exception if sending fails
     */
    String sendSimpleEmail(String to, String subject, String htmlContent, String textContent) throws Exception;
    
    /**
     * Verify if an email address is valid and can receive emails
     * @param email Email address to verify
     * @return true if email is valid
     */
    boolean verifyEmailAddress(String email);
} 