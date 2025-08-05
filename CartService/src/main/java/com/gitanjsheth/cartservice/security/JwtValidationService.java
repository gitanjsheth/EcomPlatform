package com.gitanjsheth.cartservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class JwtValidationService {
    
    @Value("${app.security.jwt.secret}")
    private String jwtSecret;
    
    public UserPrincipal validateTokenAndGetUser(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // Extract user information from claims
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String email = claims.get("email", String.class);
            List<String> roles = claims.get("roles", List.class);
            
            log.debug("JWT validation successful for user: {} (ID: {})", username, userId);
            
            return new UserPrincipal(userId, username, email, roles);
            
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return null;
        }
    }
    
    public Long extractUserIdFromToken(String token) {
        UserPrincipal userPrincipal = validateTokenAndGetUser(token);
        return userPrincipal != null ? userPrincipal.getUserId() : null;
    }
}