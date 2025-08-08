package com.gitanjsheth.notificationservice.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "user_notification_preferences")
@EqualsAndHashCode(callSuper = true)
public class UserNotificationPreference extends BaseModel {
    
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;
    
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;
    
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = false;
    
    @Column(name = "marketing_emails_enabled", nullable = false)
    private Boolean marketingEmailsEnabled = true;
    
    @Column(name = "order_updates_enabled", nullable = false)
    private Boolean orderUpdatesEnabled = true;
    
    @Column(name = "promotional_emails_enabled", nullable = false)
    private Boolean promotionalEmailsEnabled = true;
    
    @Column(name = "unsubscribe_token", unique = true)
    private String unsubscribeToken;
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Boolean getEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    
    public Boolean getSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(Boolean smsEnabled) { this.smsEnabled = smsEnabled; }
    
    public Boolean getPushEnabled() { return pushEnabled; }
    public void setPushEnabled(Boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    
    public Boolean getMarketingEmailsEnabled() { return marketingEmailsEnabled; }
    public void setMarketingEmailsEnabled(Boolean marketingEmailsEnabled) { this.marketingEmailsEnabled = marketingEmailsEnabled; }
    
    public Boolean getOrderUpdatesEnabled() { return orderUpdatesEnabled; }
    public void setOrderUpdatesEnabled(Boolean orderUpdatesEnabled) { this.orderUpdatesEnabled = orderUpdatesEnabled; }
    
    public Boolean getPromotionalEmailsEnabled() { return promotionalEmailsEnabled; }
    public void setPromotionalEmailsEnabled(Boolean promotionalEmailsEnabled) { this.promotionalEmailsEnabled = promotionalEmailsEnabled; }
    
    public String getUnsubscribeToken() { return unsubscribeToken; }
    public void setUnsubscribeToken(String unsubscribeToken) { this.unsubscribeToken = unsubscribeToken; }
} 