package com.ecogrid.ems.notification.repository;

import com.ecogrid.ems.notification.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    // Find alerts by device
    List<Alert> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
    
    // Find alerts by site
    List<Alert> findBySiteIdOrderByCreatedAtDesc(Long siteId);
    
    // Find unacknowledged alerts
    List<Alert> findByAcknowledgedFalseOrderByCreatedAtDesc();
    
    // Find alerts by severity
    List<Alert> findBySeverityOrderByCreatedAtDesc(Alert.AlertSeverity severity);
    
    // Find alerts by type
    List<Alert> findByTypeOrderByCreatedAtDesc(String type);
    
    // Find alerts in date range
    List<Alert> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Find unacknowledged alerts by severity (for dashboard)
    @Query("SELECT a FROM Alert a WHERE a.acknowledged = false AND a.severity IN :severities ORDER BY a.createdAt DESC")
    List<Alert> findUnacknowledgedBySeverityIn(@Param("severities") List<Alert.AlertSeverity> severities);
    
    // Find alerts by device and date range
    @Query("SELECT a FROM Alert a WHERE a.deviceId = :deviceId AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    List<Alert> findByDeviceAndDateRange(@Param("deviceId") Long deviceId, 
                                       @Param("start") LocalDateTime start, 
                                       @Param("end") LocalDateTime end);
    
    // Find alerts by site and date range
    @Query("SELECT a FROM Alert a WHERE a.siteId = :siteId AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    List<Alert> findBySiteAndDateRange(@Param("siteId") Long siteId, 
                                     @Param("start") LocalDateTime start, 
                                     @Param("end") LocalDateTime end);
    
    // Count unacknowledged alerts by severity
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.acknowledged = false AND a.severity = :severity")
    long countUnacknowledgedBySeverity(@Param("severity") Alert.AlertSeverity severity);
    
    // Find recent alerts for a user's sites (based on user permissions)
    @Query("SELECT a FROM Alert a WHERE a.siteId IN :siteIds AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlertsForSites(@Param("siteIds") List<Long> siteIds, @Param("since") LocalDateTime since);
    
    // Paginated search with filters
    @Query("SELECT a FROM Alert a WHERE " +
           "(:deviceId IS NULL OR a.deviceId = :deviceId) AND " +
           "(:siteId IS NULL OR a.siteId = :siteId) AND " +
           "(:type IS NULL OR a.type LIKE %:type%) AND " +
           "(:severity IS NULL OR a.severity = :severity) AND " +
           "(:acknowledged IS NULL OR a.acknowledged = :acknowledged) AND " +
           "a.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY a.createdAt DESC")
    Page<Alert> findAlertsWithFilters(@Param("deviceId") Long deviceId,
                                    @Param("siteId") Long siteId,
                                    @Param("type") String type,
                                    @Param("severity") Alert.AlertSeverity severity,
                                    @Param("acknowledged") Boolean acknowledged,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);
    
    // Find alerts that need cleanup (older than specified days)
    @Query("SELECT a FROM Alert a WHERE a.createdAt < :cutoffDate AND a.acknowledged = true")
    List<Alert> findAlertsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Get alert statistics by type for a date range
    @Query("SELECT a.type, COUNT(a) FROM Alert a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.type")
    List<Object[]> getAlertStatsByType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Get alert statistics by severity for a date range
    @Query("SELECT a.severity, COUNT(a) FROM Alert a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.severity")
    List<Object[]> getAlertStatsBySeverity(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}