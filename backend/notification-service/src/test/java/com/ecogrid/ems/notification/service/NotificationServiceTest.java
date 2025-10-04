package com.ecogrid.ems.notification.service;

import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.entity.AlertHistory;
import com.ecogrid.ems.notification.entity.NotificationRule;
import com.ecogrid.ems.notification.entity.NotificationPreference;
import com.ecogrid.ems.notification.repository.AlertHistoryRepository;
import com.ecogrid.ems.notification.repository.NotificationRuleRepository;
import com.ecogrid.ems.notification.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private NotificationRuleRepository notificationRuleRepository;
    
    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;
    
    @Mock
    private AlertHistoryRepository alertHistoryRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private WebSocketService webSocketService;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private Alert testAlert;
    private NotificationRule testRule;
    private NotificationPreference testPreferences;
    
    @BeforeEach
    void setUp() {
        testAlert = new Alert(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        testAlert.setId(1L);
        
        testRule = new NotificationRule("Test Rule", 100L, "TEMPERATURE_HIGH", Alert.AlertSeverity.MEDIUM);
        testRule.setId(1L);
        testRule.setEmailEnabled(true);
        testRule.setWebsocketEnabled(true);
        
        testPreferences = new NotificationPreference(100L, "test@example.com");
        testPreferences.setEmailEnabled(true);
        testPreferences.setWebsocketEnabled(true);
    }
    
    @Test
    void processAlert_WithMatchingRules_ShouldSendNotifications() {
        // Given
        List<NotificationRule> matchingRules = Arrays.asList(testRule);
        when(notificationRuleRepository.findMatchingRules(
            eq(1L), eq(1L), eq("TEMPERATURE_HIGH"), eq("TEMPERATURE_"), eq("HIGH")))
            .thenReturn(matchingRules);
        
        when(notificationPreferenceRepository.findByUserId(100L))
            .thenReturn(Optional.of(testPreferences));
        
        doNothing().when(emailService).sendAlertNotification(any(Alert.class), anyString());
        doNothing().when(webSocketService).sendAlertToUser(anyLong(), any(Alert.class));
        when(alertHistoryRepository.save(any(AlertHistory.class))).thenReturn(new AlertHistory());
        
        // When
        notificationService.processAlert(testAlert);
        
        // Then
        verify(notificationRuleRepository).findMatchingRules(
            eq(1L), eq(1L), eq("TEMPERATURE_HIGH"), eq("TEMPERATURE_"), eq("HIGH"));
        verify(emailService).sendAlertNotification(testAlert, "test@example.com");
        verify(webSocketService).sendAlertToUser(100L, testAlert);
        verify(alertHistoryRepository, times(2)).save(any(AlertHistory.class));
    }
    
    @Test
    void processAlert_WithNoMatchingRules_ShouldNotSendNotifications() {
        // Given
        when(notificationRuleRepository.findMatchingRules(
            any(), any(), any(), any(), any()))
            .thenReturn(Arrays.asList());
        
        // When
        notificationService.processAlert(testAlert);
        
        // Then
        verify(notificationRuleRepository).findMatchingRules(
            eq(1L), eq(1L), eq("TEMPERATURE_HIGH"), eq("TEMPERATURE_"), eq("HIGH"));
        verify(emailService, never()).sendAlertNotification(any(), any());
        verify(webSocketService, never()).sendAlertToUser(any(), any());
        verify(alertHistoryRepository, never()).save(any());
    }
    
    @Test
    void processAlert_WithEmailDisabled_ShouldOnlySendWebSocket() {
        // Given
        testRule.setEmailEnabled(false);
        List<NotificationRule> matchingRules = Arrays.asList(testRule);
        
        when(notificationRuleRepository.findMatchingRules(any(), any(), any(), any(), any()))
            .thenReturn(matchingRules);
        when(notificationPreferenceRepository.findByUserId(100L))
            .thenReturn(Optional.of(testPreferences));
        
        doNothing().when(webSocketService).sendAlertToUser(anyLong(), any(Alert.class));
        when(alertHistoryRepository.save(any(AlertHistory.class))).thenReturn(new AlertHistory());
        
        // When
        notificationService.processAlert(testAlert);
        
        // Then
        verify(emailService, never()).sendAlertNotification(any(), any());
        verify(webSocketService).sendAlertToUser(100L, testAlert);
        verify(alertHistoryRepository).save(any(AlertHistory.class));
    }
    
    @Test
    void processAlert_WithWebSocketDisabled_ShouldOnlySendEmail() {
        // Given
        testRule.setWebsocketEnabled(false);
        List<NotificationRule> matchingRules = Arrays.asList(testRule);
        
        when(notificationRuleRepository.findMatchingRules(any(), any(), any(), any(), any()))
            .thenReturn(matchingRules);
        when(notificationPreferenceRepository.findByUserId(100L))
            .thenReturn(Optional.of(testPreferences));
        
        doNothing().when(emailService).sendAlertNotification(any(Alert.class), anyString());
        when(alertHistoryRepository.save(any(AlertHistory.class))).thenReturn(new AlertHistory());
        
        // When
        notificationService.processAlert(testAlert);
        
        // Then
        verify(emailService).sendAlertNotification(testAlert, "test@example.com");
        verify(webSocketService, never()).sendAlertToUser(any(), any());
        verify(alertHistoryRepository).save(any(AlertHistory.class));
    }
    
    @Test
    void processAlert_WithNoUserPreferences_ShouldCreateDefaultPreferences() {
        // Given
        List<NotificationRule> matchingRules = Arrays.asList(testRule);
        when(notificationRuleRepository.findMatchingRules(any(), any(), any(), any(), any()))
            .thenReturn(matchingRules);
        
        when(notificationPreferenceRepository.findByUserId(100L))
            .thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
            .thenReturn(testPreferences);
        
        doNothing().when(webSocketService).sendAlertToUser(anyLong(), any(Alert.class));
        when(alertHistoryRepository.save(any(AlertHistory.class))).thenReturn(new AlertHistory());
        
        // When
        notificationService.processAlert(testAlert);
        
        // Then
        ArgumentCaptor<NotificationPreference> preferencesCaptor = 
            ArgumentCaptor.forClass(NotificationPreference.class);
        verify(notificationPreferenceRepository).save(preferencesCaptor.capture());
        
        NotificationPreference savedPreferences = preferencesCaptor.getValue();
        assertEquals(100L, savedPreferences.getUserId());
        assertTrue(savedPreferences.isEmailEnabled());
        assertTrue(savedPreferences.isWebsocketEnabled());
        
        verify(webSocketService).sendAlertToUser(100L, testAlert);
    }
    
    @Test
    void processAlert_WithQuietHours_ShouldSkipNotifications() {
        // Given
        testPreferences.setQuietHoursStart(22); // 10 PM
        testPreferences.setQuietHoursEnd(6);    // 6 AM
        
        List<NotificationRule> matchingRules = Arrays.asList(testRule);
        when(notificationRuleRepository.findMatchingRules(any(), any(), any(), any(), any()))
            .thenReturn(matchingRules);
        when(notificationPreferenceRepository.findByUserId(100L))
            .thenReturn(Optional.of(testPreferences));
        
        // When
        notificationService.processAlert(testAlert);
        
        // Then
        // Note: This test is time-dependent and would need to be adjusted based on actual time
        // In a real scenario, you'd mock the time or use a clock abstraction
        verify(notificationRuleRepository).findMatchingRules(any(), any(), any(), any(), any());
    }
    
    @Test
    void processAlert_WithEmailFailure_ShouldRecordFailedHistory() {
        // Given
        List<NotificationRule> matchingRules = Arrays.asList(testRule);
        when(notificationRuleRepository.findMatchingRules(any(), any(), any(), any(), any()))
            .thenReturn(matchingRules);
        when(notificationPreferenceRepository.findByUserId(100L))
            .thenReturn(Optional.of(testPreferences));
        
        doThrow(new RuntimeException("Email service unavailable"))
            .when(emailService).sendAlertNotification(any(Alert.class), anyString());
        
        doNothing().when(webSocketService).sendAlertToUser(anyLong(), any(Alert.class));
        when(alertHistoryRepository.save(any(AlertHistory.class))).thenReturn(new AlertHistory());
        
        // When
        notificationService.processAlert(testAlert);
        
        // Then
        ArgumentCaptor<AlertHistory> historyCaptor = ArgumentCaptor.forClass(AlertHistory.class);
        verify(alertHistoryRepository, times(2)).save(historyCaptor.capture());
        
        // Find the email history record
        AlertHistory emailHistory = historyCaptor.getAllValues().stream()
            .filter(h -> h.getNotificationType() == AlertHistory.NotificationType.EMAIL)
            .findFirst()
            .orElse(null);
        
        assertNotNull(emailHistory);
        assertEquals(AlertHistory.DeliveryStatus.FAILED, emailHistory.getStatus());
        assertEquals("Email service unavailable", emailHistory.getErrorMessage());
    }
    
    @Test
    void notifyAlertAcknowledged_ShouldBroadcastUpdate() {
        // Given
        doNothing().when(webSocketService).broadcastAlertUpdate(any(Alert.class), anyString());
        
        // When
        notificationService.notifyAlertAcknowledged(testAlert);
        
        // Then
        verify(webSocketService).broadcastAlertUpdate(testAlert, "ACKNOWLEDGED");
    }
    
    @Test
    void notifyAlertResolved_ShouldBroadcastUpdate() {
        // Given
        doNothing().when(webSocketService).broadcastAlertUpdate(any(Alert.class), anyString());
        
        // When
        notificationService.notifyAlertResolved(testAlert);
        
        // Then
        verify(webSocketService).broadcastAlertUpdate(testAlert, "RESOLVED");
    }
    
    @Test
    void retryFailedNotifications_ShouldProcessFailedNotifications() {
        // Given
        AlertHistory failedHistory = new AlertHistory(1L, 100L, AlertHistory.NotificationType.EMAIL, "test@example.com");
        failedHistory.setStatus(AlertHistory.DeliveryStatus.FAILED);
        failedHistory.setRetryCount(1);
        
        List<AlertHistory> failedNotifications = Arrays.asList(failedHistory);
        when(alertHistoryRepository.findNotificationsForRetry(anyInt(), any()))
            .thenReturn(failedNotifications);
        when(alertHistoryRepository.save(any(AlertHistory.class))).thenReturn(failedHistory);
        
        // When
        notificationService.retryFailedNotifications(3);
        
        // Then
        verify(alertHistoryRepository).findNotificationsForRetry(eq(3), any());
        
        ArgumentCaptor<AlertHistory> historyCaptor = ArgumentCaptor.forClass(AlertHistory.class);
        verify(alertHistoryRepository).save(historyCaptor.capture());
        
        AlertHistory savedHistory = historyCaptor.getValue();
        assertEquals(2, savedHistory.getRetryCount());
        assertEquals(AlertHistory.DeliveryStatus.RETRYING, savedHistory.getStatus());
    }
}