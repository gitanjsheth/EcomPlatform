package com.gitanjsheth.cartservice.security;

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
        
        logger.debug("JWT Filter executing for: " + request.getRequestURI());
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.debug("JWT token found, validating...");
            
            UserPrincipal userPrincipal = jwtValidationService.validateTokenAndGetUser(token);
            
            if (userPrincipal != null) {
                logger.debug("JWT token valid for user: " + userPrincipal.getUsername());
                // Convert roles to Spring Security authorities
                List<SimpleGrantedAuthority> authorities = userPrincipal.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal.getUsername(), 
                        null, 
                        authorities
                    );
                
                // Store user ID in authentication details
                authentication.setDetails(userPrincipal.getUserId());
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.warn("JWT token validation failed");
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip JWT filter for health checks and internal service calls
        return path.equals("/health") || 
               path.equals("/actuator/health") ||
               request.getHeader("X-Service-Token") != null ||
               request.getHeader("X-Service-Name") != null;
    }
}