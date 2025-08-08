package com.gitanjsheth.notificationservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsSesConfig {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AwsSesConfig.class);
    
    @Value("${aws.ses.region}")
    private String region;
    
    @Value("${aws.ses.access-key}")
    private String accessKey;
    
    @Value("${aws.ses.secret-key}")
    private String secretKey;
    
    @Bean
    public AmazonSimpleEmailService amazonSimpleEmailService() {
        log.info("Initializing AWS SES client for region: {}", region);
        
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
} 