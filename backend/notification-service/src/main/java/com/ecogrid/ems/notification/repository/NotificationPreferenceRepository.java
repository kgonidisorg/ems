package com.ecogrid.ems.notification.repository;

import com.ecogrid.ems.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    // Find preferences by user ID
    Optional<NotificationPreference> findByUserId(Long userId);
    
    // Check if preferences exist for user
    boolean existsByUserId(Long userId);
    
    // Delete preferences by user ID
    void deleteByUserId(Long userId);
}