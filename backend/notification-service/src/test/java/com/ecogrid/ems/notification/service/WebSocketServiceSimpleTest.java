package com.ecogrid.ems.notification.service;

import com.ecogrid.ems.notification.entity.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @InjectMocks
    private WebSocketService webSocketService;
    
    private Alert testAlert;
    
    @BeforeEach
    void setUp() {
        testAlert = new Alert(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        testAlert.setId(1L);
        
        // Enable WebSocket for testing by setting the field via reflection
        try {
            var field = WebSocketService.class.getDeclaredField("websocketEnabled");
            field.setAccessible(true);
            field.set(webSocketService, true);
            
            var topicPrefixField = WebSocketService.class.getDeclaredField("topicPrefix");
            topicPrefixField.setAccessible(true);
            topicPrefixField.set(webSocketService, "/topic");
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure WebSocket for testing", e);
        }
    }
    
    @Test
    void sendAlertToUser_ShouldSendNotificationToUser() {
        // Given
        Long userId = 100L;
        
        // When
        webSocketService.sendAlertToUser(userId, testAlert);
        
        // Then - verify that convertAndSend was called with correct destination
        verify(messagingTemplate).convertAndSend(eq("/topic/user/100/alerts"), any(Object.class));
    }
    
    @Test
    void broadcastAlertUpdate_ShouldSendUpdateToAllUsers() {
        // Given
        String action = "ACKNOWLEDGED";
        
        // When
        webSocketService.broadcastAlertUpdate(testAlert, action);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq("/topic/alerts/updates"), any(Object.class));
    }
    
    @Test
    void broadcastSystemNotification_ShouldSendSystemMessage() {
        // Given
        String message = "System maintenance scheduled";
        String type = "MAINTENANCE";
        
        // When
        webSocketService.broadcastSystemNotification(message, type);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq("/topic/system/notifications"), any(Object.class));
    }
    
    @Test
    void broadcastDashboardUpdate_ShouldSendDashboardData() {
        // Given
        Map<String, Object> dashboardData = Map.of(
            "totalAlerts", 10,
            "criticalAlerts", 2,
            "activeDevices", 50
        );
        
        // When
        webSocketService.broadcastDashboardUpdate(dashboardData);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq("/topic/dashboard/updates"), any(Object.class));
    }
    
    @Test
    void broadcastDeviceStatusUpdate_ShouldSendDeviceUpdate() {
        // Given
        Long deviceId = 1L;
        String status = "OFFLINE";
        Map<String, Object> telemetryData = Map.of(
            "temperature", 25.5,
            "voltage", 230.0,
            "power", 1500.0
        );
        
        // When
        webSocketService.broadcastDeviceStatusUpdate(deviceId, status, telemetryData);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq("/topic/devices/status"), any(Object.class));
    }
    
    @Test
    void registerUserSession_ShouldTrackUserSession() {
        // Given
        Long userId = 100L;
        String sessionId = "session-123";
        
        // When
        webSocketService.registerUserSession(userId, sessionId);
        
        // Then
        assertTrue(webSocketService.isUserConnected(userId));
        assertEquals(1, webSocketService.getActiveUserCount());
    }
    
    @Test
    void unregisterUserSession_ShouldRemoveUserSession() {
        // Given
        Long userId = 100L;
        String sessionId = "session-123";
        webSocketService.registerUserSession(userId, sessionId);
        
        // When
        webSocketService.unregisterUserSession(userId);
        
        // Then
        assertFalse(webSocketService.isUserConnected(userId));
        assertEquals(0, webSocketService.getActiveUserCount());
    }
    
    @Test
    void isUserConnected_WithNoSession_ShouldReturnFalse() {
        // Given
        Long userId = 100L;
        
        // When
        boolean connected = webSocketService.isUserConnected(userId);
        
        // Then
        assertFalse(connected);
    }
    
    @Test
    void getActiveUserCount_WithMultipleSessions_ShouldReturnCorrectCount() {
        // Given
        webSocketService.registerUserSession(100L, "session-1");
        webSocketService.registerUserSession(200L, "session-2");
        webSocketService.registerUserSession(300L, "session-3");
        
        // When
        int count = webSocketService.getActiveUserCount();
        
        // Then
        assertEquals(3, count);
    }
    
    @Test
    void testWebSocketConfiguration_ShouldReturnTrue() {
        // When
        boolean result = webSocketService.testWebSocketConfiguration();
        
        // Then
        assertTrue(result);
        verify(messagingTemplate).convertAndSend(eq("/topic/test"), any(Object.class));
    }
    
    @Test
    void testWebSocketConfiguration_WhenException_ShouldReturnFalse() {
        // Given
        doThrow(new RuntimeException("WebSocket error"))
            .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        
        // When
        boolean result = webSocketService.testWebSocketConfiguration();
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void multipleUserSessions_ShouldTrackCorrectly() {
        // Given
        webSocketService.registerUserSession(100L, "session-1");
        webSocketService.registerUserSession(200L, "session-2");
        
        // When & Then
        assertTrue(webSocketService.isUserConnected(100L));
        assertTrue(webSocketService.isUserConnected(200L));
        assertEquals(2, webSocketService.getActiveUserCount());
        
        // Unregister one user
        webSocketService.unregisterUserSession(100L);
        assertFalse(webSocketService.isUserConnected(100L));
        assertTrue(webSocketService.isUserConnected(200L));
        assertEquals(1, webSocketService.getActiveUserCount());
    }
}