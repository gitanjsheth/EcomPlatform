package com.gitanjsheth.notificationservice.repositories;

import com.gitanjsheth.notificationservice.models.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {
    
    Optional<UserNotificationPreference> findByUserId(String userId);
    
    Optional<UserNotificationPreference> findByUnsubscribeToken(String unsubscribeToken);
    
    boolean existsByUserId(String userId);
} 