package com.ecogrid.ems.notification.service;

import com.ecogrid.ems.notification.dto.AlertRequest;
import com.ecogrid.ems.notification.dto.AlertResponse;
import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {
    
    @Mock
    private AlertRepository alertRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private AlertService alertService;
    
    private Alert testAlert;
    private AlertRequest testAlertRequest;
    
    @BeforeEach
    void setUp() {
        testAlert = new Alert(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        testAlert.setId(1L);
        testAlert.setCreatedAt(LocalDateTime.now());
        
        testAlertRequest = new AlertRequest(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
    }
    
    @Test
    void createAlert_ShouldCreateAlertAndTriggerNotifications() {
        // Given
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);
        doNothing().when(notificationService).processAlert(any(Alert.class));
        
        // When
        AlertResponse response = alertService.createAlert(testAlertRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(testAlert.getId(), response.getId());
        assertEquals(testAlert.getType(), response.getType());
        assertEquals(testAlert.getSeverity(), response.getSeverity());
        assertEquals(testAlert.getMessage(), response.getMessage());
        
        verify(alertRepository).save(any(Alert.class));
        verify(notificationService).processAlert(any(Alert.class));
    }
    
    @Test
    void getAlert_WhenAlertExists_ShouldReturnAlert() {
        // Given
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        
        // When
        Optional<AlertResponse> result = alertService.getAlert(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testAlert.getId(), result.get().getId());
        assertEquals(testAlert.getType(), result.get().getType());
        
        verify(alertRepository).findById(1L);
    }
    
    @Test
    void getAlert_WhenAlertDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(alertRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When
        Optional<AlertResponse> result = alertService.getAlert(1L);
        
        // Then
        assertFalse(result.isPresent());
        
        verify(alertRepository).findById(1L);
    }
    
    @Test
    void getAllAlerts_ShouldReturnPagedResults() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        Page<Alert> alertPage = new PageImpl<>(alerts);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(alertRepository.findAll(pageable)).thenReturn(alertPage);
        
        // When
        Page<AlertResponse> result = alertService.getAllAlerts(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAlert.getId(), result.getContent().get(0).getId());
        
        verify(alertRepository).findAll(pageable);
    }
    
    @Test
    void getAlertsByDevice_ShouldReturnDeviceAlerts() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        when(alertRepository.findByDeviceIdOrderByCreatedAtDesc(1L)).thenReturn(alerts);
        
        // When
        List<AlertResponse> result = alertService.getAlertsByDevice(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAlert.getId(), result.get(0).getId());
        
        verify(alertRepository).findByDeviceIdOrderByCreatedAtDesc(1L);
    }
    
    @Test
    void getAlertsBySite_ShouldReturnSiteAlerts() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        when(alertRepository.findBySiteIdOrderByCreatedAtDesc(1L)).thenReturn(alerts);
        
        // When
        List<AlertResponse> result = alertService.getAlertsBySite(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAlert.getId(), result.get(0).getId());
        
        verify(alertRepository).findBySiteIdOrderByCreatedAtDesc(1L);
    }
    
    @Test
    void getUnacknowledgedAlerts_ShouldReturnUnacknowledgedAlerts() {
        // Given
        List<Alert> alerts = Arrays.asList(testAlert);
        when(alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc()).thenReturn(alerts);
        
        // When
        List<AlertResponse> result = alertService.getUnacknowledgedAlerts();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAlert.getId(), result.get(0).getId());
        assertFalse(result.get(0).isAcknowledged());
        
        verify(alertRepository).findByAcknowledgedFalseOrderByCreatedAtDesc();
    }
    
    @Test
    void acknowledgeAlert_WhenAlertExists_ShouldAcknowledgeAlert() {
        // Given
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);
        doNothing().when(notificationService).notifyAlertAcknowledged(any(Alert.class));
        
        // When
        AlertResponse result = alertService.acknowledgeAlert(1L, 100L);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isAcknowledged());
        assertEquals(100L, result.getAcknowledgedBy());
        assertNotNull(result.getAcknowledgedAt());
        
        verify(alertRepository).findById(1L);
        verify(alertRepository).save(any(Alert.class));
        verify(notificationService).notifyAlertAcknowledged(any(Alert.class));
    }
    
    @Test
    void acknowledgeAlert_WhenAlertDoesNotExist_ShouldThrowException() {
        // Given
        when(alertRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> alertService.acknowledgeAlert(1L, 100L));
        
        assertEquals("Alert not found with ID: 1", exception.getMessage());
        
        verify(alertRepository).findById(1L);
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).notifyAlertAcknowledged(any(Alert.class));
    }
    
    @Test
    void acknowledgeAlert_WhenAlertAlreadyAcknowledged_ShouldThrowException() {
        // Given
        testAlert.setAcknowledged(true);
        testAlert.setAcknowledgedBy(200L);
        testAlert.setAcknowledgedAt(LocalDateTime.now());
        
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> alertService.acknowledgeAlert(1L, 100L));
        
        assertEquals("Alert is already acknowledged", exception.getMessage());
        
        verify(alertRepository).findById(1L);
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).notifyAlertAcknowledged(any(Alert.class));
    }
    
    @Test
    void resolveAlert_WhenAlertExists_ShouldResolveAlert() {
        // Given
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);
        doNothing().when(notificationService).notifyAlertResolved(any(Alert.class));
        
        // When
        AlertResponse result = alertService.resolveAlert(1L, 100L);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getResolvedAt());
        assertTrue(result.isAcknowledged()); // Should auto-acknowledge
        assertEquals(100L, result.getAcknowledgedBy());
        
        verify(alertRepository).findById(1L);
        verify(alertRepository).save(any(Alert.class));
        verify(notificationService).notifyAlertResolved(any(Alert.class));
    }
    
    @Test
    void resolveAlert_WhenAlertAlreadyResolved_ShouldThrowException() {
        // Given
        testAlert.setResolvedAt(LocalDateTime.now());
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> alertService.resolveAlert(1L, 100L));
        
        assertEquals("Alert is already resolved", exception.getMessage());
        
        verify(alertRepository).findById(1L);
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).notifyAlertResolved(any(Alert.class));
    }
    
    @Test
    void countUnacknowledgedBySeverity_ShouldReturnCount() {
        // Given
        when(alertRepository.countUnacknowledgedBySeverity(Alert.AlertSeverity.HIGH)).thenReturn(5L);
        
        // When
        long result = alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.HIGH);
        
        // Then
        assertEquals(5L, result);
        
        verify(alertRepository).countUnacknowledgedBySeverity(Alert.AlertSeverity.HIGH);
    }
    
    @Test
    void cleanupOldAlerts_ShouldDeleteOldAlerts() {
        // Given
        List<Alert> oldAlerts = Arrays.asList(testAlert);
        when(alertRepository.findAlertsForCleanup(any(LocalDateTime.class))).thenReturn(oldAlerts);
        doNothing().when(alertRepository).deleteAll(oldAlerts);
        
        // When
        int result = alertService.cleanupOldAlerts(90);
        
        // Then
        assertEquals(1, result);
        
        verify(alertRepository).findAlertsForCleanup(any(LocalDateTime.class));
        verify(alertRepository).deleteAll(oldAlerts);
    }
    
    @Test
    void deleteAlert_WhenAlertExists_ShouldDeleteAlert() {
        // Given
        when(alertRepository.existsById(1L)).thenReturn(true);
        doNothing().when(alertRepository).deleteById(1L);
        
        // When
        alertService.deleteAlert(1L);
        
        // Then
        verify(alertRepository).existsById(1L);
        verify(alertRepository).deleteById(1L);
    }
    
    @Test
    void deleteAlert_WhenAlertDoesNotExist_ShouldThrowException() {
        // Given
        when(alertRepository.existsById(1L)).thenReturn(false);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> alertService.deleteAlert(1L));
        
        assertEquals("Alert not found with ID: 1", exception.getMessage());
        
        verify(alertRepository).existsById(1L);
        verify(alertRepository, never()).deleteById(1L);
    }
}