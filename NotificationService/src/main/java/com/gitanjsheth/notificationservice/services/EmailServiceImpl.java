package com.gitanjsheth.notificationservice.services;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitanjsheth.notificationservice.models.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private final AmazonSimpleEmailService sesClient;
    
    private final ObjectMapper objectMapper;
    
    public EmailServiceImpl(AmazonSimpleEmailService sesClient, ObjectMapper objectMapper) {
        this.sesClient = sesClient;
        this.objectMapper = objectMapper;
    }
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    @Value("${aws.ses.reply-to}")
    private String replyToEmail;
    
    @Override
    public String sendEmail(Notification notification) throws Exception {
        try {
            log.info("Sending email notification to: {} with type: {}", notification.getEmail(), notification.getNotificationType());
            
            SendEmailRequest request = new SendEmailRequest()
                    .withSource(fromEmail)
                    .withDestination(new Destination().withToAddresses(notification.getEmail()))
                    .withMessage(createMessage(notification));
            
            // Add reply-to header if specified
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                request.withReplyToAddresses(replyToEmail);
            }
            
            SendEmailResult result = sesClient.sendEmail(request);
            String messageId = result.getMessageId();
            
            log.info("Email sent successfully. Message ID: {}", messageId);
            return messageId;
            
        } catch (Exception e) {
            log.error("Failed to send email to: {} with type: {}", notification.getEmail(), notification.getNotificationType(), e);
            throw e;
        }
    }
    
    @Override
    public String sendSimpleEmail(String to, String subject, String htmlContent, String textContent) throws Exception {
        try {
            log.info("Sending simple email to: {}", to);
            
            Message message = new Message()
                    .withSubject(new Content().withCharset(StandardCharsets.UTF_8.name()).withData(subject))
                    .withBody(new Body()
                            .withHtml(new Content().withCharset(StandardCharsets.UTF_8.name()).withData(htmlContent))
                            .withText(new Content().withCharset(StandardCharsets.UTF_8.name()).withData(textContent)));
            
            SendEmailRequest request = new SendEmailRequest()
                    .withSource(fromEmail)
                    .withDestination(new Destination().withToAddresses(to))
                    .withMessage(message);
            
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                request.withReplyToAddresses(replyToEmail);
            }
            
            SendEmailResult result = sesClient.sendEmail(request);
            String messageId = result.getMessageId();
            
            log.info("Simple email sent successfully. Message ID: {}", messageId);
            return messageId;
            
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw e;
        }
    }
    
    @Override
    public boolean verifyEmailAddress(String email) {
        try {
            log.info("Verifying email address: {}", email);
            
            GetIdentityVerificationAttributesRequest request = new GetIdentityVerificationAttributesRequest()
                    .withIdentities(email);
            
            GetIdentityVerificationAttributesResult result = sesClient.getIdentityVerificationAttributes(request);
            
            boolean isVerified = result.getVerificationAttributes().containsKey(email) &&
                    "Success".equals(result.getVerificationAttributes().get(email).getVerificationStatus());
            
            log.info("Email verification result for {}: {}", email, isVerified);
            return isVerified;
            
        } catch (Exception e) {
            log.error("Failed to verify email address: {}", email, e);
            return false;
        }
    }
    
    private Message createMessage(Notification notification) {
        Content subjectContent = new Content()
                .withCharset(StandardCharsets.UTF_8.name())
                .withData(notification.getSubject());
        
        Body body = new Body();
        
        // Add HTML content
        if (notification.getContent() != null && !notification.getContent().isEmpty()) {
            body.withHtml(new Content()
                    .withCharset(StandardCharsets.UTF_8.name())
                    .withData(notification.getContent()));
        }
        
        // Add text content (fallback)
        String textContent = extractTextFromHtml(notification.getContent());
        body.withText(new Content()
                .withCharset(StandardCharsets.UTF_8.name())
                .withData(textContent));
        
        return new Message()
                .withSubject(subjectContent)
                .withBody(body);
    }
    
    private String extractTextFromHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        
        // Simple HTML to text conversion
        return htmlContent
                .replaceAll("<[^>]*>", "") // Remove HTML tags
                .replaceAll("&nbsp;", " ") // Replace non-breaking spaces
                .replaceAll("&amp;", "&") // Replace HTML entities
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .trim();
    }
} 