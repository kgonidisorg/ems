package com.ecogrid.ems.notification.service;

import com.ecogrid.ems.notification.dto.AlertRequest;
import com.ecogrid.ems.notification.dto.AlertResponse;
import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new alert and trigger notifications
     */
    public AlertResponse createAlert(AlertRequest request) {
        logger.info("Creating alert: type={}, severity={}, deviceId={}, siteId={}", 
                   request.getType(), request.getSeverity(), request.getDeviceId(), request.getSiteId());
        
        Alert alert = new Alert(
            request.getDeviceId(),
            request.getSiteId(),
            request.getType(),
            request.getSeverity(),
            request.getMessage()
        );
        
        alert.setDescription(request.getDescription());
        alert.setMetadata(request.getMetadata());
        
        Alert savedAlert = alertRepository.save(alert);
        
        // Trigger notifications asynchronously
        notificationService.processAlert(savedAlert);
        
        logger.info("Alert created successfully with ID: {}", savedAlert.getId());
        return new AlertResponse(savedAlert);
    }
    
    /**
     * Get alert by ID
     */
    @Transactional(readOnly = true)
    public Optional<AlertResponse> getAlert(Long id) {
        return alertRepository.findById(id)
                .map(AlertResponse::new);
    }
    
    /**
     * Get all alerts with pagination
     */
    @Transactional(readOnly = true)
    public Page<AlertResponse> getAllAlerts(Pageable pageable) {
        return alertRepository.findAll(pageable)
                .map(AlertResponse::new);
    }
    
    /**
     * Get alerts by device
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByDevice(Long deviceId) {
        return alertRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId)
                .stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get alerts by site
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsBySite(Long siteId) {
        return alertRepository.findBySiteIdOrderByCreatedAtDesc(siteId)
                .stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unacknowledged alerts
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getUnacknowledgedAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc()
                .stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get alerts by severity
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsBySeverity(Alert.AlertSeverity severity) {
        return alertRepository.findBySeverityOrderByCreatedAtDesc(severity)
                .stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get alerts in date range
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsInDateRange(LocalDateTime start, LocalDateTime end) {
        return alertRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                .stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Search alerts with filters
     */
    @Transactional(readOnly = true)
    public Page<AlertResponse> searchAlerts(Long deviceId, Long siteId, String type, 
                                          Alert.AlertSeverity severity, Boolean acknowledged,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          Pageable pageable) {
        return alertRepository.findAlertsWithFilters(
                deviceId, siteId, type, severity, acknowledged, startDate, endDate, pageable)
                .map(AlertResponse::new);
    }
    
    /**
     * Acknowledge an alert
     */
    public AlertResponse acknowledgeAlert(Long alertId, Long userId) {
        logger.info("Acknowledging alert ID: {} by user: {}", alertId, userId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with ID: " + alertId));
        
        if (alert.isAcknowledged()) {
            logger.warn("Alert ID: {} is already acknowledged", alertId);
            throw new RuntimeException("Alert is already acknowledged");
        }
        
        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(userId);
        alert.setAcknowledgedAt(LocalDateTime.now());
        
        Alert savedAlert = alertRepository.save(alert);
        
        // Notify subscribers about acknowledgment
        notificationService.notifyAlertAcknowledged(savedAlert);
        
        logger.info("Alert ID: {} acknowledged successfully", alertId);
        return new AlertResponse(savedAlert);
    }
    
    /**
     * Resolve an alert
     */
    public AlertResponse resolveAlert(Long alertId, Long userId) {
        logger.info("Resolving alert ID: {} by user: {}", alertId, userId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with ID: " + alertId));
        
        if (alert.getResolvedAt() != null) {
            logger.warn("Alert ID: {} is already resolved", alertId);
            throw new RuntimeException("Alert is already resolved");
        }
        
        alert.setResolvedAt(LocalDateTime.now());
        
        // Auto-acknowledge if not already acknowledged
        if (!alert.isAcknowledged()) {
            alert.setAcknowledged(true);
            alert.setAcknowledgedBy(userId);
            alert.setAcknowledgedAt(LocalDateTime.now());
        }
        
        Alert savedAlert = alertRepository.save(alert);
        
        // Notify subscribers about resolution
        notificationService.notifyAlertResolved(savedAlert);
        
        logger.info("Alert ID: {} resolved successfully", alertId);
        return new AlertResponse(savedAlert);
    }
    
    /**
     * Get alert statistics by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAlertStatsByType(LocalDateTime start, LocalDateTime end) {
        return alertRepository.getAlertStatsByType(start, end);
    }
    
    /**
     * Get alert statistics by severity
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAlertStatsBySeverity(LocalDateTime start, LocalDateTime end) {
        return alertRepository.getAlertStatsBySeverity(start, end);
    }
    
    /**
     * Count unacknowledged alerts by severity
     */
    @Transactional(readOnly = true)
    public long countUnacknowledgedBySeverity(Alert.AlertSeverity severity) {
        return alertRepository.countUnacknowledgedBySeverity(severity);
    }
    
    /**
     * Get recent alerts for specific sites
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getRecentAlertsForSites(List<Long> siteIds, LocalDateTime since) {
        return alertRepository.findRecentAlertsForSites(siteIds, since)
                .stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Clean up old acknowledged alerts
     */
    public int cleanupOldAlerts(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<Alert> alertsToDelete = alertRepository.findAlertsForCleanup(cutoffDate);
        
        if (!alertsToDelete.isEmpty()) {
            logger.info("Cleaning up {} old alerts older than {} days", alertsToDelete.size(), daysToKeep);
            alertRepository.deleteAll(alertsToDelete);
        }
        
        return alertsToDelete.size();
    }
    
    /**
     * Delete an alert (admin only)
     */
    public void deleteAlert(Long alertId) {
        logger.info("Deleting alert ID: {}", alertId);
        
        if (!alertRepository.existsById(alertId)) {
            throw new RuntimeException("Alert not found with ID: " + alertId);
        }
        
        alertRepository.deleteById(alertId);
        logger.info("Alert ID: {} deleted successfully", alertId);
    }
}