package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.dtos.LoginDto;
import com.gitanjsheth.userauthservice.dtos.SignUpDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.models.User;

public interface AuthService {
    UserDto signUp(SignUpDto signUpDto);
    UserDto login(LoginDto loginDto); // Returns UserDto for API contract
    User findByEmail(String email);
    User findByUsername(String username);
    User findByPhoneNumber(String phoneNumber);
    User findById(Long userId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
    void updateLastLogin(Long userId);
    void banUser(Long userId);
    void handleFailedLoginAttempt(String identifier);
    boolean isAccountLocked(String identifier);
} 