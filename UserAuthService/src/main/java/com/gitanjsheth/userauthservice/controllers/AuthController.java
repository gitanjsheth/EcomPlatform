package com.gitanjsheth.userauthservice.controllers;

import com.gitanjsheth.userauthservice.dtos.*;
import com.gitanjsheth.userauthservice.services.UserService;
import com.gitanjsheth.userauthservice.utils.ResponseUtils;
import com.gitanjsheth.userauthservice.utils.ValidationUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpDto signUpDto, BindingResult bindingResult) {
        // Handle validation errors
        ResponseEntity<ErrorResponseDto> validationError = ValidationUtils.handleValidationErrors(bindingResult);
        if (validationError != null) {
            return validationError;
        }

        try {
            UserDto userDto = userService.signUp(signUpDto);
            return ResponseUtils.createSignUpSuccessResponse(userDto);
        } catch (Exception e) {
            return ValidationUtils.createErrorResponse(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, BindingResult bindingResult) {
        // Handle validation errors
        ResponseEntity<ErrorResponseDto> validationError = ValidationUtils.handleValidationErrors(bindingResult);
        if (validationError != null) {
            return validationError;
        }

        try {
            UserDto userDto = userService.login(loginDto);
            return ResponseUtils.createLoginSuccessResponse(userDto);
        } catch (Exception e) {
            return ValidationUtils.createErrorResponse(e.getMessage());
        }
    }
}
