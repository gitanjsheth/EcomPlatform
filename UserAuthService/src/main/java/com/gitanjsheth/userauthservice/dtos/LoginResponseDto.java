package com.gitanjsheth.userauthservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String message;
    private boolean success;
    private UserDto user;
    private String token; // JWT token for future implementation
} 