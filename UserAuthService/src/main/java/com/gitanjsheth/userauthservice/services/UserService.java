package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.dtos.LoginDto;
import com.gitanjsheth.userauthservice.dtos.SignUpDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.models.User;

public interface UserService {
    UserDto signUp(SignUpDto signUpDto) throws Exception;
    UserDto login(LoginDto loginDto) throws Exception;
    User findByEmail(String email);
    User findByUsername(String username);
    User findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
} 