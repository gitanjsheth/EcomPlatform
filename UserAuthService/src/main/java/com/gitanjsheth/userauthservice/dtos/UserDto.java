package com.gitanjsheth.userauthservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private List<RoleDto> roles;
    private LocalDateTime createdAt;
    // Removed sensitive fields: phoneNumber, status, updatedAt, createdBy, updatedBy
}
