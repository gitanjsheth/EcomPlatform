package com.gitanjsheth.userauthservice.services;

import com.gitanjsheth.userauthservice.config.DataInitializer;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataInitializer dataInitializer;
    
    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${app.security.account-lock-duration-minutes:15}")
    private int accountLockDurationMinutes;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, 
                          PasswordEncoder passwordEncoder, DataInitializer dataInitializer) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataInitializer = dataInitializer;
    }

    @Override
    @Transactional
    public UserDto signUp(SignUpDto signUpDto) {
        // Check if user already exists with optimized single query
        if (userRepository.existsByEmailOrUsernameOrPhoneNumber(
            signUpDto.getEmail(), 
            signUpDto.getUsername(), 
            signUpDto.getPhoneNumber())) {
            
            // If exists, do individual checks to provide specific error message
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
        }
        
        // Create new user
        User user = createUserFromSignUpDto(signUpDto);
        user.setRoles(Collections.singletonList(getDefaultRole()));
        
        User savedUser = userRepository.save(user);
        return DtoUtils.convertToUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto login(LoginDto loginDto) {
        // Check if account is locked
        if (isAccountLocked(loginDto.getIdentifier())) {
            throw new InvalidCredentialsException("Account is temporarily locked due to multiple failed login attempts");
        }
        
        User user = findUserByIdentifier(loginDto.getIdentifier());
        
        if (user == null) {
            handleFailedLoginAttempt(loginDto.getIdentifier());
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            handleFailedLoginAttempt(loginDto.getIdentifier());
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        if (user.getStatus() != Status.ACTIVE) {
            throw new InvalidCredentialsException("User account is not active");
        }
        
        // Successful login - reset login attempts and update last login
        updateLastLogin(user.getId());
        
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
        // Audit fields (createdAt, lastUpdatedAt, createdBy, updatedBy) are handled by JPA auditing
        return user;
    }

    private Role getDefaultRole() {
        return roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    // If role not found, trigger data initialization and try again
                    dataInitializer.initializeDefaultRoles();
                    return roleRepository.findByRoleName("USER")
                            .orElseThrow(() -> new RuntimeException("Failed to create default USER role."));
                });
    }
    
    private User findUserByIdentifier(String identifier) {
        return userRepository.findByEmailOrUsernameOrPhoneNumber(identifier).orElse(null);
    }
    
    // Internal method for JWT generation - returns User entity
    private User authenticateUser(LoginDto loginDto) {
        UserDto userDto = login(loginDto); // Use the public login method
        return findById(userDto.getId()); // Get the full User entity
    }
    
    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            user.setLoginAttempts(0); // Reset login attempts on successful login
            userRepository.save(user);
        });
    }
    
    @Override
    @Transactional
    public void banUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(Status.INACTIVE); // Using existing INACTIVE status for banned users
            userRepository.save(user);
        });
    }
    
    @Override
    @Transactional
    public void handleFailedLoginAttempt(String identifier) {
        User user = findUserByIdentifier(identifier);
        if (user != null) {
            int attempts = (user.getLoginAttempts() != null) ? user.getLoginAttempts() : 0;
            user.setLoginAttempts(attempts + 1);
            
            if (user.getLoginAttempts() >= maxLoginAttempts) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(accountLockDurationMinutes));
            }
            
            userRepository.save(user);
        }
    }
    
    @Override
    public boolean isAccountLocked(String identifier) {
        User user = findUserByIdentifier(identifier);
        if (user == null || user.getAccountLockedUntil() == null) {
            return false;
        }
        
        // Check if lock has expired
        if (LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            // Lock has expired, clear it
            user.setAccountLockedUntil(null);
            user.setLoginAttempts(0);
            userRepository.save(user);
            return false;
        }
        
        return true; // Account is still locked
    }
    
    // Method to get User entity for JWT generation (package private)
    User getUserForJwtGeneration(LoginDto loginDto) {
        return authenticateUser(loginDto);
    }
} 