package com.gitanjsheth.notificationservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitanjsheth.notificationservice.models.NotificationTemplate;
import com.gitanjsheth.notificationservice.models.NotificationType;
import com.gitanjsheth.notificationservice.repositories.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Service
public class TemplateServiceImpl implements TemplateService {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TemplateServiceImpl.class);
    
    private final NotificationTemplateRepository templateRepository;
    
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    
    public TemplateServiceImpl(NotificationTemplateRepository templateRepository, TemplateEngine templateEngine, ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String processTemplate(String templateName, Map<String, Object> data) {
        try {
            NotificationTemplate template = getTemplate(templateName);
            if (template == null) {
                log.warn("Template not found: {}", templateName);
                return null;
            }
            
            Context context = new Context();
            if (data != null) {
                data.forEach(context::setVariable);
            }
            
            String processedContent = templateEngine.process(template.getHtmlContent(), context);
            log.debug("Template processed successfully: {}", templateName);
            return processedContent;
            
        } catch (Exception e) {
            log.error("Failed to process template: {}", templateName, e);
            return null;
        }
    }
    
    @Override
    public NotificationTemplate getTemplate(String templateName) {
        return templateRepository.findByTemplateNameAndIsActiveTrue(templateName).orElse(null);
    }
    
    @Override
    public NotificationTemplate getDefaultTemplate(NotificationType notificationType) {
        var templates = templateRepository.findByNotificationTypeAndIsActiveTrue(notificationType);
        return templates.isEmpty() ? null : templates.get(0);
    }
    
    @Override
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        try {
            NotificationTemplate savedTemplate = templateRepository.save(template);
            log.info("Template saved successfully: {}", savedTemplate.getTemplateName());
            return savedTemplate;
        } catch (Exception e) {
            log.error("Failed to save template: {}", template.getTemplateName(), e);
            throw e;
        }
    }
    
    /**
     * Initialize default templates if they don't exist
     */
    public void initializeDefaultTemplates() {
        createDefaultTemplateIfNotExists(
            "user-registration",
            "Welcome to Our E-commerce Platform!",
            createUserRegistrationTemplate(),
            NotificationType.USER_REGISTRATION
        );
        
        createDefaultTemplateIfNotExists(
            "order-confirmation",
            "Order Confirmation - Order #{{orderNumber}}",
            createOrderConfirmationTemplate(),
            NotificationType.ORDER_CONFIRMATION
        );
        
        createDefaultTemplateIfNotExists(
            "password-reset",
            "Password Reset Request",
            createPasswordResetTemplate(),
            NotificationType.PASSWORD_RESET
        );
        
        createDefaultTemplateIfNotExists(
            "order-status-update",
            "Order Status Update - Order #{{orderNumber}}",
            createOrderStatusUpdateTemplate(),
            NotificationType.ORDER_STATUS_UPDATE
        );
        
        createDefaultTemplateIfNotExists(
            "payment-confirmation",
            "Payment Confirmed - Order #{{orderNumber}}",
            createPaymentConfirmationTemplate(),
            NotificationType.PAYMENT_CONFIRMATION
        );
    }
    
    private void createDefaultTemplateIfNotExists(String templateName, String subject, String htmlContent, NotificationType notificationType) {
        if (!templateRepository.existsByTemplateName(templateName)) {
            NotificationTemplate template = new NotificationTemplate();
            template.setTemplateName(templateName);
            template.setSubject(subject);
            template.setHtmlContent(htmlContent);
            template.setNotificationType(notificationType);
            template.setIsActive(true);
            template.setDescription("Default template for " + notificationType.name());
            
            saveTemplate(template);
            log.info("Created default template: {}", templateName);
        }
    }
    
    private String createUserRegistrationTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome!</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c3e50;">Welcome to Our E-commerce Platform!</h1>
                    <p>Hello <strong>{{userName}}</strong>,</p>
                    <p>Thank you for registering with us! Your account has been successfully created.</p>
                    <p>You can now:</p>
                    <ul>
                        <li>Browse our product catalog</li>
                        <li>Add items to your cart</li>
                        <li>Complete purchases</li>
                        <li>Track your orders</li>
                    </ul>
                    <p>If you have any questions, please don't hesitate to contact our support team.</p>
                    <p>Best regards,<br>The E-commerce Team</p>
                </div>
            </body>
            </html>
            """;
    }
    
    private String createOrderConfirmationTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Order Confirmation</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c3e50;">Order Confirmation</h1>
                    <p>Hello <strong>{{userName}}</strong>,</p>
                    <p>Thank you for your order! We've received your order and it's being processed.</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3>Order Details:</h3>
                        <p><strong>Order Number:</strong> {{orderNumber}}</p>
                        <p><strong>Order Date:</strong> {{orderDate}}</p>
                        <p><strong>Total Amount:</strong> ${{totalAmount}}</p>
                        <p><strong>Status:</strong> {{orderStatus}}</p>
                    </div>
                    <p>We'll send you updates as your order progresses.</p>
                    <p>Best regards,<br>The E-commerce Team</p>
                </div>
            </body>
            </html>
            """;
    }
    
    private String createPasswordResetTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Password Reset</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c3e50;">Password Reset Request</h1>
                    <p>Hello <strong>{{userName}}</strong>,</p>
                    <p>We received a request to reset your password. Click the link below to create a new password:</p>
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="{{resetLink}}" style="background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px;">Reset Password</a>
                    </p>
                    <p>If you didn't request this password reset, please ignore this email.</p>
                    <p>This link will expire in 24 hours.</p>
                    <p>Best regards,<br>The E-commerce Team</p>
                </div>
            </body>
            </html>
            """;
    }
    
    private String createOrderStatusUpdateTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Order Status Update</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c3e50;">Order Status Update</h1>
                    <p>Hello <strong>{{userName}}</strong>,</p>
                    <p>Your order status has been updated!</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3>Order Details:</h3>
                        <p><strong>Order Number:</strong> {{orderNumber}}</p>
                        <p><strong>New Status:</strong> {{newStatus}}</p>
                        <p><strong>Updated Date:</strong> {{updateDate}}</p>
                        {% if trackingNumber %}
                        <p><strong>Tracking Number:</strong> {{trackingNumber}}</p>
                        {% endif %}
                    </div>
                    <p>Thank you for choosing us!</p>
                    <p>Best regards,<br>The E-commerce Team</p>
                </div>
            </body>
            </html>
            """;
    }
    
    private String createPaymentConfirmationTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Payment Confirmation</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c3e50;">Payment Confirmed!</h1>
                    <p>Hello <strong>{{userName}}</strong>,</p>
                    <p>Great news! Your payment has been successfully processed.</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3>Payment Details:</h3>
                        <p><strong>Order Number:</strong> {{orderNumber}}</p>
                        <p><strong>Payment Amount:</strong> ${{paymentAmount}}</p>
                        <p><strong>Payment Date:</strong> {{paymentDate}}</p>
                        <p><strong>Payment Method:</strong> {{paymentMethod}}</p>
                    </div>
                    <p>Your order is now being processed and will be shipped soon!</p>
                    <p>Best regards,<br>The E-commerce Team</p>
                </div>
            </body>
            </html>
            """;
    }
} 