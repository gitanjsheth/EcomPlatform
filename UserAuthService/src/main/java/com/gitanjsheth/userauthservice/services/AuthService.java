package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.dtos.LoginDto;
import com.gitanjsheth.userauthservice.dtos.SignUpDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.models.User;

public interface AuthService {
    UserDto signUp(SignUpDto signUpDto);
    UserDto login(LoginDto loginDto);
    User findByEmail(String email);
    User findByUsername(String username);
    User findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
} 