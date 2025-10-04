package com.ecogrid.ems.notification.service;

import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.entity.AlertHistory;
import com.ecogrid.ems.notification.entity.NotificationRule;
import com.ecogrid.ems.notification.entity.NotificationPreference;
import com.ecogrid.ems.notification.repository.AlertHistoryRepository;
import com.ecogrid.ems.notification.repository.NotificationRuleRepository;
import com.ecogrid.ems.notification.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
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
    
    /**
     * Process a new alert and send notifications based on rules
     */
    @Async
    public void processAlert(Alert alert) {
        logger.info("Processing alert ID: {} for notifications", alert.getId());
        
        try {
            // Find matching notification rules
            List<NotificationRule> matchingRules = findMatchingRules(alert);
            
            if (matchingRules.isEmpty()) {
                logger.debug("No notification rules match alert ID: {}", alert.getId());
                return;
            }
            
            logger.info("Found {} matching notification rules for alert ID: {}", 
                       matchingRules.size(), alert.getId());
            
            // Process each matching rule
            for (NotificationRule rule : matchingRules) {
                processNotificationRule(alert, rule);
            }
            
        } catch (Exception e) {
            logger.error("Error processing alert ID: {} for notifications", alert.getId(), e);
        }
    }
    
    /**
     * Find notification rules that match the alert criteria
     */
    private List<NotificationRule> findMatchingRules(Alert alert) {
        String alertTypePrefix = extractAlertTypePrefix(alert.getType());
        
        return notificationRuleRepository.findMatchingRules(
            alert.getSiteId(),
            alert.getDeviceId(),
            alert.getType(),
            alertTypePrefix,
            alert.getSeverity()
        );
    }
    
    /**
     * Extract alert type prefix for wildcard matching (e.g., "BATTERY_LOW" -> "BATTERY_")
     */
    private String extractAlertTypePrefix(String alertType) {
        if (alertType.contains("_")) {
            return alertType.substring(0, alertType.indexOf("_") + 1);
        }
        return alertType;
    }
    
    /**
     * Process a notification rule for an alert
     */
    private void processNotificationRule(Alert alert, NotificationRule rule) {
        logger.debug("Processing notification rule ID: {} for alert ID: {}", rule.getId(), alert.getId());
        
        // Get user notification preferences
        NotificationPreference preferences = notificationPreferenceRepository
            .findByUserId(rule.getUserId())
            .orElse(createDefaultPreferences(rule.getUserId()));
        
        // Check if we're in quiet hours
        if (isInQuietHours(preferences)) {
            logger.debug("Skipping notification due to quiet hours for user: {}", rule.getUserId());
            return;
        }
        
        // Send email notification if enabled
        if (rule.isEmailEnabled() && preferences.isEmailEnabled() && preferences.getEmail() != null) {
            sendEmailNotification(alert, rule, preferences);
        }
        
        // Send WebSocket notification if enabled
        if (rule.isWebsocketEnabled() && preferences.isWebsocketEnabled()) {
            sendWebSocketNotification(alert, rule);
        }
    }
    
    /**
     * Check if current time is within user's quiet hours
     */
    private boolean isInQuietHours(NotificationPreference preferences) {
        if (preferences.getQuietHoursStart() == null || preferences.getQuietHoursEnd() == null) {
            return false;
        }
        
        // This is a simplified implementation - in production, you'd want to handle
        // timezone conversion and wrap-around hours (e.g., 22:00 to 06:00)
        int currentHour = LocalDateTime.now().getHour();
        int start = preferences.getQuietHoursStart();
        int end = preferences.getQuietHoursEnd();
        
        if (start <= end) {
            return currentHour >= start && currentHour < end;
        } else {
            // Wrap-around case (e.g., 22:00 to 06:00)
            return currentHour >= start || currentHour < end;
        }
    }
    
    /**
     * Create default notification preferences for a user
     */
    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference preferences = new NotificationPreference();
        preferences.setUserId(userId);
        preferences.setEmailEnabled(true);
        preferences.setWebsocketEnabled(true);
        return notificationPreferenceRepository.save(preferences);
    }
    
    /**
     * Send email notification
     */
    private void sendEmailNotification(Alert alert, NotificationRule rule, NotificationPreference preferences) {
        logger.debug("Sending email notification for alert ID: {} to user: {}", alert.getId(), rule.getUserId());
        
        AlertHistory history = new AlertHistory(
            alert.getId(),
            rule.getUserId(),
            AlertHistory.NotificationType.EMAIL,
            preferences.getEmail()
        );
        
        try {
            emailService.sendAlertNotification(alert, preferences.getEmail());
            
            history.setStatus(AlertHistory.DeliveryStatus.DELIVERED);
            history.setDeliveredAt(LocalDateTime.now());
            
            logger.debug("Email notification sent successfully for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send email notification for alert ID: {}", alert.getId(), e);
            
            history.setStatus(AlertHistory.DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
        }
        
        alertHistoryRepository.save(history);
    }
    
    /**
     * Send WebSocket notification
     */
    private void sendWebSocketNotification(Alert alert, NotificationRule rule) {
        logger.debug("Sending WebSocket notification for alert ID: {} to user: {}", alert.getId(), rule.getUserId());
        
        AlertHistory history = new AlertHistory(
            alert.getId(),
            rule.getUserId(),
            AlertHistory.NotificationType.WEBSOCKET,
            "user-" + rule.getUserId()
        );
        
        try {
            webSocketService.sendAlertToUser(rule.getUserId(), alert);
            
            history.setStatus(AlertHistory.DeliveryStatus.DELIVERED);
            history.setDeliveredAt(LocalDateTime.now());
            
            logger.debug("WebSocket notification sent successfully for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification for alert ID: {}", alert.getId(), e);
            
            history.setStatus(AlertHistory.DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
        }
        
        alertHistoryRepository.save(history);
    }
    
    /**
     * Notify about alert acknowledgment
     */
    @Async
    public void notifyAlertAcknowledged(Alert alert) {
        logger.info("Sending acknowledgment notifications for alert ID: {}", alert.getId());
        
        try {
            // Send WebSocket update to all connected users
            webSocketService.broadcastAlertUpdate(alert, "ACKNOWLEDGED");
        } catch (Exception e) {
            logger.error("Error sending acknowledgment notifications for alert ID: {}", alert.getId(), e);
        }
    }
    
    /**
     * Notify about alert resolution
     */
    @Async
    public void notifyAlertResolved(Alert alert) {
        logger.info("Sending resolution notifications for alert ID: {}", alert.getId());
        
        try {
            // Send WebSocket update to all connected users
            webSocketService.broadcastAlertUpdate(alert, "RESOLVED");
        } catch (Exception e) {
            logger.error("Error sending resolution notifications for alert ID: {}", alert.getId(), e);
        }
    }
    
    /**
     * Retry failed notifications
     */
    @Async
    public void retryFailedNotifications(int maxRetries) {
        logger.info("Retrying failed notifications with max retries: {}", maxRetries);
        
        try {
            LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(5); // Wait 5 minutes before retry
            List<AlertHistory> failedNotifications = alertHistoryRepository
                .findNotificationsForRetry(maxRetries, retryAfter);
            
            logger.info("Found {} failed notifications to retry", failedNotifications.size());
            
            for (AlertHistory history : failedNotifications) {
                retryNotification(history);
            }
            
        } catch (Exception e) {
            logger.error("Error during notification retry process", e);
        }
    }
    
    /**
     * Retry a specific notification
     */
    private void retryNotification(AlertHistory history) {
        logger.debug("Retrying notification ID: {}, attempt: {}", 
                    history.getId(), history.getRetryCount() + 1);
        
        history.setRetryCount(history.getRetryCount() + 1);
        history.setStatus(AlertHistory.DeliveryStatus.RETRYING);
        
        // Implementation would depend on notification type
        // This is a placeholder for the retry logic
        
        alertHistoryRepository.save(history);
    }
}