package com.gitanjsheth.userauthservice.repositories;

import com.gitanjsheth.userauthservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
    
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByEmailOrUsernameOrPhoneNumber(@Param("identifier") String identifier);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email OR u.username = :username OR (:phoneNumber IS NOT NULL AND u.phoneNumber = :phoneNumber)")
    boolean existsByEmailOrUsernameOrPhoneNumber(@Param("email") String email, @Param("username") String username, @Param("phoneNumber") String phoneNumber);
} 