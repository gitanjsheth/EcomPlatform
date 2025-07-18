package com.gitanjsheth.userauthservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponseDto {
    private boolean valid;
    private UserDto user;
    private String message;
} 