package com.ecogrid.ems.notification.controller;

import com.ecogrid.ems.notification.dto.AlertRequest;
import com.ecogrid.ems.notification.dto.AlertResponse;
import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.service.AlertService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AlertController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);
    
    @Autowired
    private AlertService alertService;
    
    /**
     * Create a new alert
     */
    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody AlertRequest request) {
        logger.info("Creating new alert: type={}, severity={}", request.getType(), request.getSeverity());
        
        try {
            AlertResponse response = alertService.createAlert(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating alert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get alert by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable Long id) {
        logger.debug("Retrieving alert with ID: {}", id);
        
        return alertService.getAlert(id)
                .map(alert -> ResponseEntity.ok(alert))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all alerts with pagination
     */
    @GetMapping
    public ResponseEntity<Page<AlertResponse>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Retrieving alerts - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AlertResponse> alerts = alertService.getAllAlerts(pageable);
        
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Get alerts by device
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<AlertResponse>> getAlertsByDevice(@PathVariable Long deviceId) {
        logger.debug("Retrieving alerts for device: {}", deviceId);
        
        List<AlertResponse> alerts = alertService.getAlertsByDevice(deviceId);
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Get alerts by site
     */
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<AlertResponse>> getAlertsBySite(@PathVariable Long siteId) {
        logger.debug("Retrieving alerts for site: {}", siteId);
        
        List<AlertResponse> alerts = alertService.getAlertsBySite(siteId);
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Get unacknowledged alerts
     */
    @GetMapping("/unacknowledged")
    public ResponseEntity<List<AlertResponse>> getUnacknowledgedAlerts() {
        logger.debug("Retrieving unacknowledged alerts");
        
        List<AlertResponse> alerts = alertService.getUnacknowledgedAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Get alerts by severity
     */
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<AlertResponse>> getAlertsBySeverity(@PathVariable Alert.AlertSeverity severity) {
        logger.debug("Retrieving alerts with severity: {}", severity);
        
        List<AlertResponse> alerts = alertService.getAlertsBySeverity(severity);
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Search alerts with filters
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AlertResponse>> searchAlerts(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Alert.AlertSeverity severity,
            @RequestParam(required = false) Boolean acknowledged,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Searching alerts with filters - deviceId: {}, siteId: {}, type: {}, severity: {}", 
                    deviceId, siteId, type, severity);
        
        // Set default date range if not provided (last 30 days)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AlertResponse> alerts = alertService.searchAlerts(
            deviceId, siteId, type, severity, acknowledged, startDate, endDate, pageable);
        
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Acknowledge an alert
     */
    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledgeAlert(
            @PathVariable Long id,
            @RequestParam Long userId) {
        
        logger.info("Acknowledging alert ID: {} by user: {}", id, userId);
        
        try {
            AlertResponse response = alertService.acknowledgeAlert(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error acknowledging alert ID: {}", id, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error acknowledging alert ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Resolve an alert
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable Long id,
            @RequestParam Long userId) {
        
        logger.info("Resolving alert ID: {} by user: {}", id, userId);
        
        try {
            AlertResponse response = alertService.resolveAlert(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error resolving alert ID: {}", id, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error resolving alert ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get alert statistics by type
     */
    @GetMapping("/statistics/by-type")
    public ResponseEntity<List<Map<String, Object>>> getAlertStatsByType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // Set default date range if not provided (last 7 days)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        logger.debug("Retrieving alert statistics by type from {} to {}", startDate, endDate);
        
        List<Object[]> stats = alertService.getAlertStatsByType(startDate, endDate);
        List<Map<String, Object>> result = stats.stream()
                .map(row -> Map.of("type", row[0], "count", row[1]))
                .toList();
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get alert statistics by severity
     */
    @GetMapping("/statistics/by-severity")
    public ResponseEntity<List<Map<String, Object>>> getAlertStatsBySeverity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // Set default date range if not provided (last 7 days)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        logger.debug("Retrieving alert statistics by severity from {} to {}", startDate, endDate);
        
        List<Object[]> stats = alertService.getAlertStatsBySeverity(startDate, endDate);
        List<Map<String, Object>> result = stats.stream()
                .map(row -> Map.of("severity", row[0], "count", row[1]))
                .toList();
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get dashboard alert summary
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        logger.debug("Retrieving dashboard alert summary");
        
        Map<String, Object> summary = Map.of(
            "totalCritical", alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.CRITICAL),
            "totalHigh", alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.HIGH),
            "totalMedium", alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.MEDIUM),
            "totalLow", alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.LOW),
            "totalUnacknowledged", alertService.getUnacknowledgedAlerts().size()
        );
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Delete an alert (admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        logger.info("Deleting alert ID: {}", id);
        
        try {
            alertService.deleteAlert(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting alert ID: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error deleting alert ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "healthy", "service", "notification-service"));
    }
}