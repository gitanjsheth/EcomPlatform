package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.dtos.LoginDto;
import com.gitanjsheth.userauthservice.dtos.SignUpDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.exceptions.InvalidCredentialsException;
import com.gitanjsheth.userauthservice.exceptions.UserAlreadyExistsException;
import com.gitanjsheth.userauthservice.models.Role;
import com.gitanjsheth.userauthservice.models.Status;
import com.gitanjsheth.userauthservice.models.User;
import com.gitanjsheth.userauthservice.repositories.RoleRepository;
import com.gitanjsheth.userauthservice.repositories.UserRepository;
import com.gitanjsheth.userauthservice.utils.DtoUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDto signUp(SignUpDto signUpDto) {
        // Check if user already exists
        if (existsByEmail(signUpDto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + signUpDto.getEmail() + " already exists");
        }
        
        if (existsByUsername(signUpDto.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + signUpDto.getUsername() + " already exists");
        }
        
        if (signUpDto.getPhoneNumber() != null && !signUpDto.getPhoneNumber().trim().isEmpty() 
            && existsByPhoneNumber(signUpDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("User with phone number " + signUpDto.getPhoneNumber() + " already exists");
        }
        
        // Create new user
        User user = createUserFromSignUpDto(signUpDto);
        user.setRoles(Collections.singletonList(getOrCreateDefaultRole()));
        
        User savedUser = userRepository.save(user);
        return DtoUtils.convertToUserDto(savedUser);
    }

    @Override
    public UserDto login(LoginDto loginDto) {
        User user = findUserByIdentifier(loginDto.getIdentifier());
        
        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        if (user.getStatus() != Status.ACTIVE) {
            throw new InvalidCredentialsException("User account is not active");
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