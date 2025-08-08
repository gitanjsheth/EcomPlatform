package com.gitanjsheth.notificationservice.dtos;

import lombok.Data;

@Data
public class UserNotificationPreferenceDto {
    private Long id;
    private String userId;
    private Boolean emailEnabled = true;
    private Boolean smsEnabled = false;
    private Boolean pushEnabled = false;
    private Boolean marketingEmailsEnabled = true;
    private Boolean orderUpdatesEnabled = true;
    private Boolean promotionalEmailsEnabled = true;
    private String unsubscribeToken;
} 