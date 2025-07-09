package com.gitanjsheth.userauthservice.utils;

import com.gitanjsheth.userauthservice.dtos.LoginResponseDto;
import com.gitanjsheth.userauthservice.dtos.SignUpResponseDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {
    
    public static ResponseEntity<SignUpResponseDto> createSignUpSuccessResponse(UserDto userDto) {
        SignUpResponseDto response = new SignUpResponseDto(
            "User registered successfully", 
            true, 
            userDto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    public static ResponseEntity<LoginResponseDto> createLoginSuccessResponse(UserDto userDto) {
        LoginResponseDto response = new LoginResponseDto(
            "Login successful", 
            true, 
            userDto, 
            null // Token implementation can be added later
        );
        return ResponseEntity.ok(response);
    }
} 