package com.gitanjsheth.userauthservice.controllers;

import com.gitanjsheth.userauthservice.dtos.*;
import com.gitanjsheth.userauthservice.services.AuthService;
import com.gitanjsheth.userauthservice.services.TokenService;
import com.gitanjsheth.userauthservice.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"}) // Restrict to specific origins
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    
    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(@Valid @RequestBody SignUpDto signUpDto) {
        UserDto userDto = authService.signUp(signUpDto);
        return ResponseUtils.createSignUpSuccessResponse(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        TokenService.TokenGenerationResult result = tokenService.generateTokenForUser(loginDto);
        return ResponseUtils.createLoginSuccessResponse(result.getUser(), result.getToken(), result.getExpiresAt());
    }
    
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponseDto> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new TokenValidationResponseDto(false, null, "Invalid authorization header"));
        }
        
        String token = authHeader.substring(7);
        TokenValidationResponseDto result = tokenService.validateToken(token);
        
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.invalidateToken(token);
            return ResponseEntity.ok("Logout successful");
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }
    
    @PostMapping("/admin/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> banUser(@PathVariable Long userId) {
        tokenService.banUserAndInvalidateTokens(userId);
        return ResponseEntity.ok("User banned and all sessions invalidated");
    }
}
