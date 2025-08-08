package com.gitanjsheth.notificationservice.config;

import com.gitanjsheth.notificationservice.services.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);

    private final TemplateService templateService;
    
    public DataInitializer(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing NotificationService data...");
        
        try {
            // Initialize default templates
            if (templateService instanceof com.gitanjsheth.notificationservice.services.TemplateServiceImpl) {
                ((com.gitanjsheth.notificationservice.services.TemplateServiceImpl) templateService).initializeDefaultTemplates();
            }
            
            log.info("NotificationService data initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize NotificationService data", e);
        }
    }
} 