package com.gitanjsheth.productservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        
        return null;
    }
    
    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }
    
    public static String getCurrentUsername() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
    
    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
    
    public static boolean hasRole(String role) {
        UserPrincipal user = getCurrentUser();
        return user != null && user.hasRole(role);
    }
    
    public static boolean hasAnyRole(String... roles) {
        UserPrincipal user = getCurrentUser();
        return user != null && user.hasAnyRole(roles);
    }
} 