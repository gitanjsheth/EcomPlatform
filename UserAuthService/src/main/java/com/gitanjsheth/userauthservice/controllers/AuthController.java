package com.gitanjsheth.userauthservice.controllers;

import com.gitanjsheth.userauthservice.dtos.*;
import com.gitanjsheth.userauthservice.services.AuthService;
import com.gitanjsheth.userauthservice.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(@Valid @RequestBody SignUpDto signUpDto) {
        UserDto userDto = authService.signUp(signUpDto);
        return ResponseUtils.createSignUpSuccessResponse(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        UserDto userDto = authService.login(loginDto);
        return ResponseUtils.createLoginSuccessResponse(userDto);
    }
}
