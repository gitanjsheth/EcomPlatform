package com.gitanjsheth.productservice.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JwtValidationService {
    
    @Value("${app.auth-service.url}")
    private String authServiceUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public JwtValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    public UserPrincipal validateTokenAndGetUser(String token) {
        try {
            String url = authServiceUrl + "/auth/validate-token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                
                if (responseBody.get("valid").asBoolean()) {
                    JsonNode user = responseBody.get("user");
                    JsonNode roles = user.get("roles");
                    
                    List<String> roleNames = new ArrayList<>();
                    if (roles.isArray()) {
                        for (JsonNode role : roles) {
                            roleNames.add(role.get("roleName").asText());
                        }
                    }
                    
                    return new UserPrincipal(
                        user.get("id").asLong(),
                        user.get("username").asText(),
                        user.get("email").asText(),
                        roleNames
                    );
                }
            }
            
            return null;
        } catch (Exception e) {
            // Log error and return null for invalid tokens
            System.err.println("Token validation failed: " + e.getMessage());
            return null;
        }
    }
    
    public boolean isTokenValid(String token) {
        return validateTokenAndGetUser(token) != null;
    }
} 