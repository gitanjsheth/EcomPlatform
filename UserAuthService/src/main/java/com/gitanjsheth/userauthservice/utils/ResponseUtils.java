package com.gitanjsheth.userauthservice.utils;

import com.gitanjsheth.userauthservice.dtos.LoginResponseDto;
import com.gitanjsheth.userauthservice.dtos.SignUpResponseDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ResponseUtils {
    
    public static ResponseEntity<SignUpResponseDto> createSignUpSuccessResponse(UserDto userDto) {
        SignUpResponseDto response = new SignUpResponseDto(
            "User registered successfully", 
            true, 
            userDto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    public static ResponseEntity<LoginResponseDto> createLoginSuccessResponse(UserDto userDto, String token, LocalDateTime expiresAt) {
        LoginResponseDto response = new LoginResponseDto(
            "Login successful", 
            true, 
            userDto, 
            token,
            expiresAt
        );
        return ResponseEntity.ok(response);
    }
} 