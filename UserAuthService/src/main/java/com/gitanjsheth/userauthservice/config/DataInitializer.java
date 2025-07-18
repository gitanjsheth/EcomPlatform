package com.gitanjsheth.userauthservice.config;

import com.gitanjsheth.userauthservice.models.Role;
import com.gitanjsheth.userauthservice.models.Status;
import com.gitanjsheth.userauthservice.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultRoles();
    }
    
    public void initializeDefaultRoles() {
        // Create USER role if it doesn't exist
        if (!roleRepository.existsByRoleName("USER")) {
            Role userRole = new Role();
            userRole.setRoleName("USER");
            userRole.setRoleDescription("Default user role");
            userRole.setStatus(Status.ACTIVE);
            roleRepository.save(userRole);
        }
        
        // Create ADMIN role if it doesn't exist
        if (!roleRepository.existsByRoleName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole.setRoleDescription("Administrator role");
            adminRole.setStatus(Status.ACTIVE);
            roleRepository.save(adminRole);
        }
    }
} 