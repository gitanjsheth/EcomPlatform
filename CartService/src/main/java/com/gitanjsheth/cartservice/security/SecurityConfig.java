package com.gitanjsheth.cartservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/health", "/actuator/health").permitAll()
                
                // Cart operations - require authentication
                .requestMatchers(HttpMethod.GET, "/api/carts/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/carts/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/carts/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/carts/**").authenticated()
                
                // Guest cart operations - allow without authentication but with session/user validation
                .requestMatchers(HttpMethod.POST, "/api/carts/guest/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/carts/guest/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/carts/guest/**").permitAll()
                
                // Internal service endpoints - secured by service tokens (handled in controller)
                .requestMatchers(HttpMethod.POST, "/api/carts/*/checkout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/carts/merge").permitAll()
                
                // Default: all other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}