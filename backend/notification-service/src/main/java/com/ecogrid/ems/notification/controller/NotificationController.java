package com.ecogrid.ems.notification.controller;

import com.ecogrid.ems.notification.dto.NotificationRuleRequest;
import com.ecogrid.ems.notification.entity.NotificationRule;
import com.ecogrid.ems.notification.entity.NotificationPreference;
import com.ecogrid.ems.notification.entity.AlertHistory;
import com.ecogrid.ems.notification.repository.NotificationRuleRepository;
import com.ecogrid.ems.notification.repository.NotificationPreferenceRepository;
import com.ecogrid.ems.notification.repository.AlertHistoryRepository;
import com.ecogrid.ems.notification.service.EmailService;
import com.ecogrid.ems.notification.service.WebSocketService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationRuleRepository notificationRuleRepository;
    
    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;
    
    @Autowired
    private AlertHistoryRepository alertHistoryRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    // === HEALTH ENDPOINT ===
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.debug("Health check requested");
        
        Map<String, Object> healthResponse = Map.of(
            "status", "UP",
            "service", "notification-service",
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(healthResponse);
    }
    
    // === NOTIFICATION RULES ENDPOINTS ===
    
    /**
     * Create a new notification rule
     */
    @PostMapping("/rules")
    public ResponseEntity<NotificationRule> createNotificationRule(@Valid @RequestBody NotificationRuleRequest request) {
        logger.info("Creating notification rule: {} for user: {}", request.getName(), request.getUserId());
        
        try {
            // Check if rule with same name already exists for user
            Optional<NotificationRule> existingRule = notificationRuleRepository
                .findByUserIdAndName(request.getUserId(), request.getName());
            
            if (existingRule.isPresent()) {
                logger.warn("Notification rule with name '{}' already exists for user: {}", 
                           request.getName(), request.getUserId());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            NotificationRule rule = new NotificationRule(
                request.getName(),
                request.getUserId(),
                request.getAlertType(),
                request.getMinSeverity()
            );
            
            rule.setDescription(request.getDescription());
            rule.setSiteId(request.getSiteId());
            rule.setDeviceId(request.getDeviceId());
            rule.setEmailEnabled(request.isEmailEnabled());
            rule.setWebsocketEnabled(request.isWebsocketEnabled());
            rule.setActive(request.isActive());
            
            NotificationRule savedRule = notificationRuleRepository.save(rule);
            
            logger.info("Notification rule created with ID: {}", savedRule.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
            
        } catch (Exception e) {
            logger.error("Error creating notification rule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notification rules for a user
     */
    @GetMapping("/rules/user/{userId}")
    public ResponseEntity<List<NotificationRule>> getUserNotificationRules(@PathVariable Long userId) {
        logger.debug("Retrieving notification rules for user: {}", userId);
        
        List<NotificationRule> rules = notificationRuleRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(rules);
    }
    
    /**
     * Get active notification rules for a user
     */
    @GetMapping("/rules/user/{userId}/active")
    public ResponseEntity<List<NotificationRule>> getActiveUserNotificationRules(@PathVariable Long userId) {
        logger.debug("Retrieving active notification rules for user: {}", userId);
        
        List<NotificationRule> rules = notificationRuleRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(rules);
    }
    
    /**
     * Get notification rule by ID
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<NotificationRule> getNotificationRule(@PathVariable Long id) {
        logger.debug("Retrieving notification rule with ID: {}", id);
        
        return notificationRuleRepository.findById(id)
                .map(rule -> ResponseEntity.ok(rule))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update notification rule
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<NotificationRule> updateNotificationRule(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRuleRequest request) {
        
        logger.info("Updating notification rule ID: {}", id);
        
        try {
            Optional<NotificationRule> optionalRule = notificationRuleRepository.findById(id);
            if (optionalRule.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            NotificationRule rule = optionalRule.get();
            
            // Check if new name conflicts with another rule for the same user
            if (!rule.getName().equals(request.getName())) {
                Optional<NotificationRule> existingRule = notificationRuleRepository
                    .findByUserIdAndName(request.getUserId(), request.getName());
                
                if (existingRule.isPresent() && !existingRule.get().getId().equals(id)) {
                    logger.warn("Notification rule with name '{}' already exists for user: {}", 
                               request.getName(), request.getUserId());
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }
            
            rule.setName(request.getName());
            rule.setDescription(request.getDescription());
            rule.setSiteId(request.getSiteId());
            rule.setDeviceId(request.getDeviceId());
            rule.setAlertType(request.getAlertType());
            rule.setMinSeverity(request.getMinSeverity());
            rule.setEmailEnabled(request.isEmailEnabled());
            rule.setWebsocketEnabled(request.isWebsocketEnabled());
            rule.setActive(request.isActive());
            
            NotificationRule savedRule = notificationRuleRepository.save(rule);
            
            logger.info("Notification rule ID: {} updated successfully", id);
            return ResponseEntity.ok(savedRule);
            
        } catch (Exception e) {
            logger.error("Error updating notification rule ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete notification rule
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteNotificationRule(@PathVariable Long id) {
        logger.info("Deleting notification rule ID: {}", id);
        
        try {
            if (!notificationRuleRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            notificationRuleRepository.deleteById(id);
            
            logger.info("Notification rule ID: {} deleted successfully", id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            logger.error("Error deleting notification rule ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // === NOTIFICATION PREFERENCES ENDPOINTS ===
    
    /**
     * Get notification preferences for a user
     */
    @GetMapping("/preferences/user/{userId}")
    public ResponseEntity<NotificationPreference> getUserPreferences(@PathVariable Long userId) {
        logger.debug("Retrieving notification preferences for user: {}", userId);
        
        return notificationPreferenceRepository.findByUserId(userId)
                .map(preferences -> ResponseEntity.ok(preferences))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create or update notification preferences
     */
    @PutMapping("/preferences/user/{userId}")
    public ResponseEntity<NotificationPreference> updateUserPreferences(
            @PathVariable Long userId,
            @Valid @RequestBody NotificationPreference preferences) {
        
        logger.info("Updating notification preferences for user: {}", userId);
        
        try {
            Optional<NotificationPreference> existingPreferences = 
                notificationPreferenceRepository.findByUserId(userId);
            
            NotificationPreference preferencesToSave;
            if (existingPreferences.isPresent()) {
                preferencesToSave = existingPreferences.get();
            } else {
                preferencesToSave = new NotificationPreference();
                preferencesToSave.setUserId(userId);
            }
            
            preferencesToSave.setEmail(preferences.getEmail());
            preferencesToSave.setEmailEnabled(preferences.isEmailEnabled());
            preferencesToSave.setWebsocketEnabled(preferences.isWebsocketEnabled());
            preferencesToSave.setDigestEnabled(preferences.isDigestEnabled());
            preferencesToSave.setDigestFrequency(preferences.getDigestFrequency());
            preferencesToSave.setQuietHoursStart(preferences.getQuietHoursStart());
            preferencesToSave.setQuietHoursEnd(preferences.getQuietHoursEnd());
            preferencesToSave.setTimezone(preferences.getTimezone());
            
            NotificationPreference savedPreferences = notificationPreferenceRepository.save(preferencesToSave);
            
            logger.info("Notification preferences updated for user: {}", userId);
            return ResponseEntity.ok(savedPreferences);
            
        } catch (Exception e) {
            logger.error("Error updating notification preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // === NOTIFICATION HISTORY ENDPOINTS ===
    
    /**
     * Get notification history for a user
     */
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<Page<AlertHistory>> getUserNotificationHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Retrieving notification history for user: {} - page: {}, size: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AlertHistory> history = alertHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get notification history for an alert
     */
    @GetMapping("/history/alert/{alertId}")
    public ResponseEntity<List<AlertHistory>> getAlertNotificationHistory(@PathVariable Long alertId) {
        logger.debug("Retrieving notification history for alert: {}", alertId);
        
        List<AlertHistory> history = alertHistoryRepository.findByAlertIdOrderByCreatedAtDesc(alertId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get notification delivery statistics
     */
    @GetMapping("/statistics/delivery")
    public ResponseEntity<List<Map<String, Object>>> getDeliveryStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // Set default date range if not provided (last 7 days)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        logger.debug("Retrieving delivery statistics from {} to {}", startDate, endDate);
        
        List<Object[]> stats = alertHistoryRepository.getDeliveryStats(startDate, endDate);
        List<Map<String, Object>> result = stats.stream()
                .map(row -> Map.of("status", row[0], "count", row[1]))
                .toList();
        
        return ResponseEntity.ok(result);
    }
    
    // === SERVICE TESTING ENDPOINTS ===
    
    /**
     * Test email configuration
     */
    @PostMapping("/test/email")
    public ResponseEntity<Map<String, Object>> testEmailConfiguration() {
        logger.info("Testing email configuration");
        
        try {
            boolean success = emailService.testEmailConfiguration();
            
            Map<String, Object> result = Map.of(
                "success", success,
                "message", success ? "Email configuration test successful" : "Email configuration test failed"
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Email configuration test failed", e);
            
            Map<String, Object> result = Map.of(
                "success", false,
                "message", "Email configuration test failed: " + e.getMessage()
            );
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Test WebSocket configuration
     */
    @PostMapping("/test/websocket")
    public ResponseEntity<Map<String, Object>> testWebSocketConfiguration() {
        logger.info("Testing WebSocket configuration");
        
        try {
            boolean success = webSocketService.testWebSocketConfiguration();
            
            Map<String, Object> result = Map.of(
                "success", success,
                "message", success ? "WebSocket configuration test successful" : "WebSocket configuration test failed",
                "activeConnections", webSocketService.getActiveUserCount()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("WebSocket configuration test failed", e);
            
            Map<String, Object> result = Map.of(
                "success", false,
                "message", "WebSocket configuration test failed: " + e.getMessage()
            );
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Get notification service status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        logger.debug("Retrieving notification service status");
        
        Map<String, Object> status = Map.of(
            "service", "notification-service",
            "status", "healthy",
            "activeWebSocketConnections", webSocketService.getActiveUserCount(),
            "totalNotificationRules", notificationRuleRepository.count(),
            "totalNotificationPreferences", notificationPreferenceRepository.count(),
            "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(status);
    }
}