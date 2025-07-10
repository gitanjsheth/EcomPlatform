package com.gitanjsheth.userauthservice.controllerAdvice;

import com.gitanjsheth.userauthservice.dtos.ErrorResponseDto;
import com.gitanjsheth.userauthservice.exceptions.InvalidCredentialsException;
import com.gitanjsheth.userauthservice.exceptions.UserAlreadyExistsException;
import com.gitanjsheth.userauthservice.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class UserAuthExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ValidationUtils.handleValidationErrors(ex.getBindingResult());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getMessage(), false);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getMessage(), false);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters",
                false
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        // Log the actual error for debugging
        logger.error("Unexpected error occurred", ex);
        
        // Return generic error to client
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Internal server error occurred",
                false,
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 