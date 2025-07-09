package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.dtos.LoginDto;
import com.gitanjsheth.userauthservice.dtos.SignUpDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.models.Role;
import com.gitanjsheth.userauthservice.models.Status;
import com.gitanjsheth.userauthservice.models.User;
import com.gitanjsheth.userauthservice.repositories.RoleRepository;
import com.gitanjsheth.userauthservice.repositories.UserRepository;
import com.gitanjsheth.userauthservice.utils.DtoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDto signUp(SignUpDto signUpDto) throws Exception {
        // Check if user already exists
        if (existsByEmail(signUpDto.getEmail())) {
            throw new Exception("User with email " + signUpDto.getEmail() + " already exists");
        }
        
        if (existsByUsername(signUpDto.getUsername())) {
            throw new Exception("User with username " + signUpDto.getUsername() + " already exists");
        }
        
        if (signUpDto.getPhoneNumber() != null && !signUpDto.getPhoneNumber().trim().isEmpty() 
            && existsByPhoneNumber(signUpDto.getPhoneNumber())) {
            throw new Exception("User with phone number " + signUpDto.getPhoneNumber() + " already exists");
        }
        
        // Create new user
        User user = createUserFromSignUpDto(signUpDto);
        user.setRoles(Arrays.asList(getOrCreateDefaultRole()));
        
        User savedUser = userRepository.save(user);
        return DtoUtils.convertToUserDto(savedUser);
    }

    @Override
    public UserDto login(LoginDto loginDto) throws Exception {
        User user = findUserByIdentifier(loginDto.getIdentifier());
        
        if (user == null) {
            throw new Exception("Invalid credentials");
        }
        
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new Exception("Invalid credentials");
        }
        
        if (user.getStatus() != Status.ACTIVE) {
            throw new Exception("User account is not active");
        }
        
        return DtoUtils.convertToUserDto(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    private User createUserFromSignUpDto(SignUpDto signUpDto) {
        User user = new User();
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setPhoneNumber(signUpDto.getPhoneNumber());
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setCreatedBy("Self-Signup");
        // updatedBy should be null for new records
        return user;
    }
    
    private Role getOrCreateDefaultRole() {
        return roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("USER");
                    newRole.setRoleDescription("Default user role");
                    newRole.setStatus(Status.ACTIVE);
                    newRole.setCreatedAt(new Date());
                    newRole.setUpdatedAt(new Date());
                    newRole.setCreatedBy("SYSTEM");
                    // updatedBy should be null for new records
                    return roleRepository.save(newRole);
                });
    }
    
    private User findUserByIdentifier(String identifier) {
        // Try to find by email first
        User user = findByEmail(identifier);
        if (user != null) {
            return user;
        }
        
        // Try to find by username
        user = findByUsername(identifier);
        if (user != null) {
            return user;
        }
        
        // Try to find by phone number
        return findByPhoneNumber(identifier);
    }
} 