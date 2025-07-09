package com.gitanjsheth.userauthservice.utils;

import com.gitanjsheth.userauthservice.dtos.ErrorResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationUtils {
    
    public static ResponseEntity<ErrorResponseDto> handleValidationErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Validation failed", 
                false, 
                LocalDateTime.now(), 
                errors
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return null;
    }
    
    public static ResponseEntity<ErrorResponseDto> createErrorResponse(String message) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(message, false);
        return ResponseEntity.badRequest().body(errorResponse);
    }
} 