package com.gitanjsheth.userauthservice.controllers;

import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.models.User;
import com.gitanjsheth.userauthservice.repositories.UserRepository;
import com.gitanjsheth.userauthservice.utils.DtoUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(DtoUtils.convertToUserDto(user));
    }

    @PutMapping
    public ResponseEntity<UserDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (request.username != null) user.setUsername(request.username);
        if (request.phoneNumber != null) user.setPhoneNumber(request.phoneNumber);
        if (request.email != null) user.setEmail(request.email);

        user = userRepository.save(user);
        return ResponseEntity.ok(DtoUtils.convertToUserDto(user));
    }

    public static class UpdateProfileRequest {
        @Size(min = 3, max = 50)
        public String username;
        @Email
        public String email;
        public String phoneNumber;
    }
}


