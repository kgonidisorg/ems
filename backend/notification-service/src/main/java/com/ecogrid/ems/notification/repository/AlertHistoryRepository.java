package com.ecogrid.ems.notification.repository;

import com.ecogrid.ems.notification.entity.AlertHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    
    // Find history by alert ID
    List<AlertHistory> findByAlertIdOrderByCreatedAtDesc(Long alertId);
    
    // Find history by user ID
    List<AlertHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find failed notifications for retry
    @Query("SELECT ah FROM AlertHistory ah WHERE ah.status = 'FAILED' AND ah.retryCount < :maxRetries")
    List<AlertHistory> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);
    
    // Find pending notifications
    List<AlertHistory> findByStatusOrderByCreatedAtDesc(AlertHistory.DeliveryStatus status);
    
    // Find history by notification type
    List<AlertHistory> findByNotificationTypeOrderByCreatedAtDesc(AlertHistory.NotificationType notificationType);
    
    // Find history by user and notification type
    List<AlertHistory> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(Long userId, AlertHistory.NotificationType notificationType);
    
    // Get delivery statistics for a date range
    @Query("SELECT ah.status, COUNT(ah) FROM AlertHistory ah WHERE ah.createdAt BETWEEN :start AND :end GROUP BY ah.status")
    List<Object[]> getDeliveryStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Get delivery statistics by notification type
    @Query("SELECT ah.notificationType, ah.status, COUNT(ah) FROM AlertHistory ah WHERE ah.createdAt BETWEEN :start AND :end GROUP BY ah.notificationType, ah.status")
    List<Object[]> getDeliveryStatsByType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Find history for cleanup (older than specified days)
    @Query("SELECT ah FROM AlertHistory ah WHERE ah.createdAt < :cutoffDate")
    List<AlertHistory> findHistoryForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count failed notifications by user
    @Query("SELECT COUNT(ah) FROM AlertHistory ah WHERE ah.userId = :userId AND ah.status = 'FAILED' AND ah.createdAt >= :since")
    long countFailedNotificationsByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    // Find recent notifications for a user with pagination
    Page<AlertHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Find notifications that need retry (with exponential backoff)
    @Query("SELECT ah FROM AlertHistory ah WHERE ah.status IN ('FAILED', 'RETRYING') AND " +
           "ah.retryCount < :maxRetries AND ah.createdAt <= :retryAfter")
    List<AlertHistory> findNotificationsForRetry(@Param("maxRetries") int maxRetries, 
                                                @Param("retryAfter") LocalDateTime retryAfter);
}