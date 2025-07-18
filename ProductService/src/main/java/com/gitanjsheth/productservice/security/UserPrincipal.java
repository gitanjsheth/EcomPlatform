package com.gitanjsheth.productservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String username;
    private String email;
    private List<String> roles;
    
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null) return false;
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
} 