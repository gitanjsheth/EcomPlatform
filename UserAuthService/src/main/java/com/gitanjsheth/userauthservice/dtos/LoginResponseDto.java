package com.gitanjsheth.userauthservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String message;
    private boolean success;
    private UserDto user;
    private String token;
    private LocalDateTime expiresAt;
} 