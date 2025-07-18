package com.gitanjsheth.userauthservice.services;

import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;
    
    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate, JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }
    
    public void blacklistToken(String token) {
        try {
            Claims claims = jwtService.extractClaims(token);
            String jti = claims.getId();
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            
            if (ttl > 0) {
                // Store JTI in Redis until token naturally expires
                redisTemplate.opsForValue().set("blacklist:" + jti, "true", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // Token might be invalid, but we still want to try blacklisting by full token
            redisTemplate.opsForValue().set("blacklist:token:" + token, "true", 1, TimeUnit.HOURS);
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        try {
            String jti = jwtService.extractJwtId(token);
            return redisTemplate.hasKey("blacklist:" + jti) || 
                   redisTemplate.hasKey("blacklist:token:" + token);
        } catch (Exception e) {
            // If we can't extract JTI, check by full token
            return redisTemplate.hasKey("blacklist:token:" + token);
        }
    }
    
    public void blacklistAllUserTokens(Long userId) {
        // This is a simple implementation - in production you might want to track user tokens
        // For now, we'll rely on the user status check in token validation
        // Store a flag that will force re-validation of all tokens for this user
        redisTemplate.opsForValue().set("user:banned:" + userId, "true", 30, TimeUnit.DAYS);
    }
    
    public boolean isUserBanned(Long userId) {
        return redisTemplate.hasKey("user:banned:" + userId);
    }
    
    public void unbanUser(Long userId) {
        redisTemplate.delete("user:banned:" + userId);
    }
} 