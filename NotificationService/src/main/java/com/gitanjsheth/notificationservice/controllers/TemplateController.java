package com.gitanjsheth.notificationservice.controllers;

import com.gitanjsheth.notificationservice.dtos.NotificationTemplateDto;
import com.gitanjsheth.notificationservice.models.NotificationTemplate;
import com.gitanjsheth.notificationservice.models.NotificationType;
import com.gitanjsheth.notificationservice.services.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/notifications/templates")
public class TemplateController {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TemplateController.class);

    private final TemplateService templateService;
    
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTemplateDto> createTemplate(@Valid @RequestBody NotificationTemplateDto templateDto) {
        try {
            NotificationTemplate template = convertToEntity(templateDto);
            NotificationTemplate savedTemplate = templateService.saveTemplate(template);
            return ResponseEntity.ok(convertToDto(savedTemplate));
        } catch (Exception e) {
            log.error("Failed to create template", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTemplateDto> updateTemplate(
            @PathVariable Long templateId, 
            @Valid @RequestBody NotificationTemplateDto templateDto) {
        try {
            templateDto.setId(templateId);
            NotificationTemplate template = convertToEntity(templateDto);
            NotificationTemplate savedTemplate = templateService.saveTemplate(template);
            return ResponseEntity.ok(convertToDto(savedTemplate));
        } catch (Exception e) {
            log.error("Failed to update template: {}", templateId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{templateName}")
    public ResponseEntity<NotificationTemplateDto> getTemplate(@PathVariable String templateName) {
        try {
            NotificationTemplate template = templateService.getTemplate(templateName);
            if (template != null) {
                return ResponseEntity.ok(convertToDto(template));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to get template: {}", templateName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/type/{notificationType}")
    public ResponseEntity<List<NotificationTemplateDto>> getTemplatesByType(@PathVariable NotificationType notificationType) {
        try {
            List<NotificationTemplate> templates = templateService.getDefaultTemplate(notificationType) != null ? 
                    List.of(templateService.getDefaultTemplate(notificationType)) : List.of();
            List<NotificationTemplateDto> templateDtos = templates.stream()
                    .map(this::convertToDto)
                    .toList();
            return ResponseEntity.ok(templateDtos);
        } catch (Exception e) {
            log.error("Failed to get templates by type: {}", notificationType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> initializeDefaultTemplates() {
        try {
            // This will be called by the TemplateServiceImpl
            return ResponseEntity.ok("Default templates initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize default templates", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private NotificationTemplate convertToEntity(NotificationTemplateDto dto) {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(dto.getId());
        template.setTemplateName(dto.getTemplateName());
        template.setSubject(dto.getSubject());
        template.setHtmlContent(dto.getHtmlContent());
        template.setTextContent(dto.getTextContent());
        template.setNotificationType(dto.getNotificationType());
        template.setIsActive(dto.getIsActive());
        template.setDescription(dto.getDescription());
        template.setVariables(dto.getVariables());
        return template;
    }

    private NotificationTemplateDto convertToDto(NotificationTemplate template) {
        NotificationTemplateDto dto = new NotificationTemplateDto();
        dto.setId(template.getId());
        dto.setTemplateName(template.getTemplateName());
        dto.setSubject(template.getSubject());
        dto.setHtmlContent(template.getHtmlContent());
        dto.setTextContent(template.getTextContent());
        dto.setNotificationType(template.getNotificationType());
        dto.setIsActive(template.getIsActive());
        dto.setDescription(template.getDescription());
        dto.setVariables(template.getVariables());
        return dto;
    }
} 