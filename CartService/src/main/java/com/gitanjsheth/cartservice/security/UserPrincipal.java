package com.gitanjsheth.cartservice.security;

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
    
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    public boolean isUser() {
        return hasRole("USER");
    }
}