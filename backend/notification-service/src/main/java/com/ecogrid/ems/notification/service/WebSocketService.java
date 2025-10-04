package com.ecogrid.ems.notification.service;

import com.ecogrid.ems.notification.entity.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${app.websocket.enabled:true}")
    private boolean websocketEnabled;
    
    @Value("${app.websocket.topic-prefix:/topic}")
    private String topicPrefix;
    
    // Track active user sessions
    private final Map<Long, String> userSessions = new ConcurrentHashMap<>();
    
    /**
     * Send alert notification to a specific user
     */
    public void sendAlertToUser(Long userId, Alert alert) {
        if (!websocketEnabled) {
            logger.debug("WebSocket notifications are disabled");
            return;
        }
        
        logger.debug("Sending WebSocket alert notification to user: {} for alert ID: {}", userId, alert.getId());
        
        try {
            Map<String, Object> notification = createAlertNotification(alert, "NEW_ALERT");
            String destination = topicPrefix + "/user/" + userId + "/alerts";
            
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.debug("WebSocket alert notification sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Failed to send WebSocket alert notification to user: {}", userId, e);
            throw new RuntimeException("Failed to send WebSocket notification", e);
        }
    }
    
    /**
     * Broadcast alert update to all connected users
     */
    public void broadcastAlertUpdate(Alert alert, String action) {
        if (!websocketEnabled) {
            logger.debug("WebSocket notifications are disabled");
            return;
        }
        
        logger.debug("Broadcasting alert update for alert ID: {} with action: {}", alert.getId(), action);
        
        try {
            Map<String, Object> notification = createAlertNotification(alert, action);
            String destination = topicPrefix + "/alerts/updates";
            
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.debug("Alert update broadcast successfully for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            logger.error("Failed to broadcast alert update for alert ID: {}", alert.getId(), e);
            throw new RuntimeException("Failed to broadcast alert update", e);
        }
    }
    
    /**
     * Send system notification to all users
     */
    public void broadcastSystemNotification(String message, String type) {
        if (!websocketEnabled) {
            logger.debug("WebSocket notifications are disabled");
            return;
        }
        
        logger.info("Broadcasting system notification: {} (type: {})", message, type);
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SYSTEM_NOTIFICATION");
            notification.put("message", message);
            notification.put("notificationType", type);
            notification.put("timestamp", System.currentTimeMillis());
            
            String destination = topicPrefix + "/system/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("System notification broadcast successfully");
            
        } catch (Exception e) {
            logger.error("Failed to broadcast system notification", e);
        }
    }
    
    /**
     * Send dashboard update to all users
     */
    public void broadcastDashboardUpdate(Map<String, Object> data) {
        if (!websocketEnabled) {
            logger.debug("WebSocket notifications are disabled");
            return;
        }
        
        logger.debug("Broadcasting dashboard update");
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "DASHBOARD_UPDATE");
            notification.put("data", data);
            notification.put("timestamp", System.currentTimeMillis());
            
            String destination = topicPrefix + "/dashboard/updates";
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.debug("Dashboard update broadcast successfully");
            
        } catch (Exception e) {
            logger.error("Failed to broadcast dashboard update", e);
        }
    }
    
    /**
     * Send device status update
     */
    public void broadcastDeviceStatusUpdate(Long deviceId, String status, Map<String, Object> telemetryData) {
        if (!websocketEnabled) {
            logger.debug("WebSocket notifications are disabled");
            return;
        }
        
        logger.debug("Broadcasting device status update for device: {} with status: {}", deviceId, status);
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "DEVICE_STATUS_UPDATE");
            notification.put("deviceId", deviceId);
            notification.put("status", status);
            notification.put("telemetryData", telemetryData);
            notification.put("timestamp", System.currentTimeMillis());
            
            String destination = topicPrefix + "/devices/status";
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.debug("Device status update broadcast successfully for device: {}", deviceId);
            
        } catch (Exception e) {
            logger.error("Failed to broadcast device status update for device: {}", deviceId, e);
        }
    }
    
    /**
     * Register user session
     */
    public void registerUserSession(Long userId, String sessionId) {
        userSessions.put(userId, sessionId);
        logger.debug("Registered WebSocket session for user: {} with session ID: {}", userId, sessionId);
    }
    
    /**
     * Unregister user session
     */
    public void unregisterUserSession(Long userId) {
        String sessionId = userSessions.remove(userId);
        if (sessionId != null) {
            logger.debug("Unregistered WebSocket session for user: {} with session ID: {}", userId, sessionId);
        }
    }
    
    /**
     * Check if user has active session
     */
    public boolean isUserConnected(Long userId) {
        return userSessions.containsKey(userId);
    }
    
    /**
     * Get active user count
     */
    public int getActiveUserCount() {
        return userSessions.size();
    }
    
    /**
     * Create alert notification payload
     */
    private Map<String, Object> createAlertNotification(Alert alert, String action) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ALERT_NOTIFICATION");
        notification.put("action", action);
        notification.put("alert", convertAlertToMap(alert));
        notification.put("timestamp", System.currentTimeMillis());
        
        return notification;
    }
    
    /**
     * Convert Alert entity to Map for JSON serialization
     */
    private Map<String, Object> convertAlertToMap(Alert alert) {
        Map<String, Object> alertMap = new HashMap<>();
        alertMap.put("id", alert.getId());
        alertMap.put("deviceId", alert.getDeviceId());
        alertMap.put("siteId", alert.getSiteId());
        alertMap.put("type", alert.getType());
        alertMap.put("severity", alert.getSeverity().name());
        alertMap.put("message", alert.getMessage());
        alertMap.put("description", alert.getDescription());
        alertMap.put("acknowledged", alert.isAcknowledged());
        alertMap.put("acknowledgedBy", alert.getAcknowledgedBy());
        alertMap.put("acknowledgedAt", alert.getAcknowledgedAt());
        alertMap.put("createdAt", alert.getCreatedAt());
        alertMap.put("resolvedAt", alert.getResolvedAt());
        alertMap.put("metadata", alert.getMetadata());
        
        return alertMap;
    }
    
    /**
     * Test WebSocket configuration
     */
    public boolean testWebSocketConfiguration() {
        try {
            logger.info("Testing WebSocket configuration...");
            
            Map<String, Object> testMessage = new HashMap<>();
            testMessage.put("type", "TEST_MESSAGE");
            testMessage.put("message", "WebSocket configuration test");
            testMessage.put("timestamp", System.currentTimeMillis());
            
            String destination = topicPrefix + "/test";
            messagingTemplate.convertAndSend(destination, testMessage);
            
            logger.info("WebSocket configuration test completed successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("WebSocket configuration test failed", e);
            return false;
        }
    }
}