package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.models.Role;
import com.gitanjsheth.userauthservice.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {
    
    @Value("${app.jwt.secret}")
    private String secretKey;
    
    @Value("${app.jwt.expiration-days:30}")
    private int tokenExpirationDays;
    
    @Value("${app.jwt.admin-expiration-days:7}")
    private int adminTokenExpirationDays;
    
    @Value("${app.jwt.remember-me-expiration-days:90}")
    private int rememberMeExpirationDays;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(User user, boolean rememberMe) {
        int expirationDays = determineExpirationDays(user, rememberMe);
        long expirationTime = System.currentTimeMillis() + (expirationDays * 24 * 60 * 60 * 1000L);
        
        List<String> roles = user.getRoles().stream()
            .map(Role::getRoleName)
            .collect(Collectors.toList());
        
        return Jwts.builder()
            .setId(UUID.randomUUID().toString()) // JWT ID for blacklist tracking
            .setSubject(user.getEmail())
            .claim("userId", user.getId())
            .claim("username", user.getUsername())
            .claim("roles", roles)
            .setIssuedAt(new Date())
            .setExpiration(new Date(expirationTime))
            .signWith(getSigningKey())
            .compact();
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return Long.valueOf(claims.get("userId").toString());
    }
    
    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims.get("username").toString();
    }
    
    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }
    
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        return (List<String>) claims.get("roles");
    }
    
    public String extractJwtId(String token) {
        Claims claims = extractClaims(token);
        return claims.getId();
    }
    
    public Date extractExpiration(String token) {
        Claims claims = extractClaims(token);
        return claims.getExpiration();
    }
    
    private int determineExpirationDays(User user, boolean rememberMe) {
        // Check if user has admin role
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> "ADMIN".equals(role.getRoleName()));
        
        if (isAdmin) {
            return adminTokenExpirationDays; // 7 days for admins
        } else if (rememberMe) {
            return rememberMeExpirationDays; // 90 days for remember me
        } else {
            return tokenExpirationDays; // 30 days default
        }
    }
} 