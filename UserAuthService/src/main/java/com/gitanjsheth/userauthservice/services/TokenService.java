package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.dtos.LoginDto;
import com.gitanjsheth.userauthservice.dtos.TokenValidationResponseDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.models.Status;
import com.gitanjsheth.userauthservice.models.User;
import com.gitanjsheth.userauthservice.utils.DtoUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TokenService {
    
    private final AuthServiceImpl authService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    
    public TokenService(AuthServiceImpl authService, JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    public TokenGenerationResult generateTokenForUser(LoginDto loginDto) {
        // Authenticate user and get User entity for JWT generation
        User user = authService.getUserForJwtGeneration(loginDto);
        
        // Generate JWT token
        String jwtToken = jwtService.generateToken(user, loginDto.isRememberMe());
        
        // Get token expiration
        LocalDateTime expiresAt = jwtService.extractExpiration(jwtToken)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        
        // Convert to DTO for response
        UserDto userDto = DtoUtils.convertToUserDto(user);
        
        return new TokenGenerationResult(userDto, jwtToken, expiresAt);
    }
    
    public TokenValidationResponseDto validateToken(String token) {
        // 1. Check JWT validity
        if (!jwtService.isTokenValid(token)) {
            return new TokenValidationResponseDto(false, null, "Invalid token");
        }
        
        // 2. Check blacklist
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            return new TokenValidationResponseDto(false, null, "Token revoked");
        }
        
        // 3. Check user status in database
        try {
            Long userId = jwtService.extractUserId(token);
            User user = authService.findById(userId);
            
            if (user == null) {
                return new TokenValidationResponseDto(false, null, "User not found");
            }
            
            if (user.getStatus() != Status.ACTIVE) {
                tokenBlacklistService.blacklistToken(token); // Add to blacklist for future
                return new TokenValidationResponseDto(false, null, "User account is not active");
            }
            
            // 4. Check if user is banned via Redis
            if (tokenBlacklistService.isUserBanned(userId)) {
                tokenBlacklistService.blacklistToken(token);
                return new TokenValidationResponseDto(false, null, "User is banned");
            }
            
            UserDto userDto = DtoUtils.convertToUserDto(user);
            return new TokenValidationResponseDto(true, userDto, "Valid token");
            
        } catch (Exception e) {
            return new TokenValidationResponseDto(false, null, "Token validation failed");
        }
    }
    
    public void invalidateToken(String token) {
        tokenBlacklistService.blacklistToken(token);
    }
    
    public void banUserAndInvalidateTokens(Long userId) {
        authService.banUser(userId);
        tokenBlacklistService.blacklistAllUserTokens(userId);
    }
    
    public static class TokenGenerationResult {
        private final UserDto user;
        private final String token;
        private final LocalDateTime expiresAt;
        
        public TokenGenerationResult(UserDto user, String token, LocalDateTime expiresAt) {
            this.user = user;
            this.token = token;
            this.expiresAt = expiresAt;
        }
        
        public UserDto getUser() { return user; }
        public String getToken() { return token; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
    }
} 