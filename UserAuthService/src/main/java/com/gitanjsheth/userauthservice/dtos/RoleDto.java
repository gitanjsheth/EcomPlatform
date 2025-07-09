package com.gitanjsheth.userauthservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDto {
    private Long id;
    private String roleName;
    private String roleDescription;
} 