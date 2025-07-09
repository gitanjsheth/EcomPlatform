package com.gitanjsheth.userauthservice.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    
    @NotBlank(message = "Email, username, or phone number is required")
    private String identifier; // Can be email, username, or phone number
    
    @NotBlank(message = "Password is required")
    private String password;
}
