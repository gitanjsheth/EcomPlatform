package com.gitanjsheth.productservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtValidationService jwtValidationService;
    
    public JwtAuthenticationFilter(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        System.out.println("🚀 JWT Filter executing for: " + request.getRequestURI());
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("🔑 JWT token found, validating...");
            
            UserPrincipal userPrincipal = jwtValidationService.validateTokenAndGetUser(token);
            
            if (userPrincipal != null) {
                System.out.println("✅ JWT token valid for user: " + userPrincipal.getUsername());
                // Convert roles to Spring Security authorities
                List<SimpleGrantedAuthority> authorities = userPrincipal.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal, 
                        null, 
                        authorities
                    );
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println("❌ JWT token validation failed");
            }
        } else {
            System.out.println("ℹ️ No JWT token in request");
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("🔍 JWT Filter Check - Path: " + path + ", Method: " + method);
        
        // Skip JWT validation for public endpoints
        if (path.startsWith("/actuator/")) {
            System.out.println("✅ Skipping JWT filter for actuator endpoint");
            return true;
        }
        
        // Allow public GET requests to basic product endpoints
        if ("GET".equals(method)) {
            if (path.equals("/products") || path.equals("/products/")) {
                System.out.println("✅ Skipping JWT filter for public products endpoint");
                return true;
            }
            if (path.matches("/products/\\d+")) {
                System.out.println("✅ Skipping JWT filter for public product detail endpoint");
                return true;
            }
            if (path.startsWith("/categories")) {
                System.out.println("✅ Skipping JWT filter for categories endpoint");
                return true;
            }
        }
        
        // All other requests need JWT validation
        System.out.println("❌ JWT filter will run for this endpoint");
        return false;
    }
} 