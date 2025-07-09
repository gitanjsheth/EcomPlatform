package com.gitanjsheth.userauthservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseModel {
    
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;
    
    @Size(max = 255, message = "Role description cannot exceed 255 characters")
    @Column(name = "role_description")
    private String roleDescription;
}
