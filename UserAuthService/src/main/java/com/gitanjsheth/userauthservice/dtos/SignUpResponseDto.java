package com.gitanjsheth.userauthservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseDto {
    private String message;
    private boolean success;
    private UserDto user;
} 