package com.ecogrid.ems.notification.repository;


import com.ecogrid.ems.notification.entity.NotificationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRuleRepository extends JpaRepository<NotificationRule, Long> {
    
    // Find rules by user
    List<NotificationRule> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find active rules by user
    List<NotificationRule> findByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);
    
    // Find rules by site (including global rules where siteId is null)
    @Query("SELECT nr FROM NotificationRule nr WHERE nr.active = true AND (nr.siteId = :siteId OR nr.siteId IS NULL)")
    List<NotificationRule> findActiveRulesBySite(@Param("siteId") Long siteId);
    
    // Find rules by device (including rules for the site and global rules)
    @Query("SELECT nr FROM NotificationRule nr WHERE nr.active = true AND " +
           "(nr.deviceId = :deviceId OR nr.siteId = :siteId OR (nr.siteId IS NULL AND nr.deviceId IS NULL))")
    List<NotificationRule> findActiveRulesByDevice(@Param("deviceId") Long deviceId, @Param("siteId") Long siteId);
    
    // Find rules that match alert criteria
    @Query("SELECT nr FROM NotificationRule nr WHERE nr.active = true AND " +
           "(nr.siteId IS NULL OR nr.siteId = :siteId) AND " +
           "(nr.deviceId IS NULL OR nr.deviceId = :deviceId) AND " +
           "(nr.alertType = :alertType OR nr.alertType LIKE CONCAT(:alertTypePrefix, '%')) AND " +
           "(nr.minSeverity = 'LOW' OR " +
           " (nr.minSeverity = 'MEDIUM' AND :severity IN ('MEDIUM', 'HIGH', 'CRITICAL')) OR " +
           " (nr.minSeverity = 'HIGH' AND :severity IN ('HIGH', 'CRITICAL')) OR " +
           " (nr.minSeverity = 'CRITICAL' AND :severity = 'CRITICAL'))")
    List<NotificationRule> findMatchingRules(@Param("siteId") Long siteId,
                                           @Param("deviceId") Long deviceId,
                                           @Param("alertType") String alertType,
                                           @Param("alertTypePrefix") String alertTypePrefix,
                                           @Param("severity") String severity);
    
    // Find rules by alert type pattern
    @Query("SELECT nr FROM NotificationRule nr WHERE nr.active = true AND " +
           "(nr.alertType = :alertType OR nr.alertType LIKE CONCAT(:alertTypePrefix, '%'))")
    List<NotificationRule> findByAlertTypePattern(@Param("alertType") String alertType, 
                                                @Param("alertTypePrefix") String alertTypePrefix);
    
    // Find rules by user and name (for uniqueness check)
    Optional<NotificationRule> findByUserIdAndName(Long userId, String name);
    
    // Count active rules by user
    long countByUserIdAndActiveTrue(Long userId);
    
    // Find rules with email notifications enabled
    @Query("SELECT nr FROM NotificationRule nr WHERE nr.active = true AND nr.emailEnabled = true AND nr.userId = :userId")
    List<NotificationRule> findEmailEnabledRulesByUser(@Param("userId") Long userId);
    
    // Find rules with WebSocket notifications enabled
    @Query("SELECT nr FROM NotificationRule nr WHERE nr.active = true AND nr.websocketEnabled = true AND nr.userId = :userId")
    List<NotificationRule> findWebsocketEnabledRulesByUser(@Param("userId") Long userId);
    
    // Find all active rules for notification processing
    @Query("SELECT nr FROM NotificationRule nr WHERE nr.active = true ORDER BY nr.userId, nr.createdAt")
    List<NotificationRule> findAllActiveRules();
}