package com.ecogrid.ems.notification.controller;

import com.ecogrid.ems.notification.dto.AlertRequest;
import com.ecogrid.ems.notification.dto.AlertResponse;
import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.service.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AlertService alertService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private AlertRequest testAlertRequest;
    private AlertResponse testAlertResponse;
    
    @BeforeEach
    void setUp() {
        testAlertRequest = new AlertRequest(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        
        testAlertResponse = new AlertResponse();
        testAlertResponse.setId(1L);
        testAlertResponse.setDeviceId(1L);
        testAlertResponse.setSiteId(1L);
        testAlertResponse.setType("TEMPERATURE_HIGH");
        testAlertResponse.setSeverity(Alert.AlertSeverity.HIGH);
        testAlertResponse.setMessage("Temperature alert");
        testAlertResponse.setAcknowledged(false);
        testAlertResponse.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    void createAlert_WithValidRequest_ShouldReturnCreatedAlert() throws Exception {
        // Given
        when(alertService.createAlert(any(AlertRequest.class))).thenReturn(testAlertResponse);
        
        // When & Then
        mockMvc.perform(post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAlertRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("TEMPERATURE_HIGH"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.message").value("Temperature alert"))
                .andExpect(jsonPath("$.acknowledged").value(false));
        
        verify(alertService).createAlert(any(AlertRequest.class));
    }
    
    @Test
    void createAlert_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - request with missing required fields
        AlertRequest invalidRequest = new AlertRequest();
        
        // When & Then
        mockMvc.perform(post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(alertService, never()).createAlert(any(AlertRequest.class));
    }
    
    @Test
    void getAlert_WhenAlertExists_ShouldReturnAlert() throws Exception {
        // Given
        when(alertService.getAlert(1L)).thenReturn(Optional.of(testAlertResponse));
        
        // When & Then
        mockMvc.perform(get("/api/alerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("TEMPERATURE_HIGH"));
        
        verify(alertService).getAlert(1L);
    }
    
    @Test
    void getAlert_WhenAlertDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(alertService.getAlert(1L)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/alerts/1"))
                .andExpect(status().isNotFound());
        
        verify(alertService).getAlert(1L);
    }
    
    @Test
    void getAllAlerts_ShouldReturnPagedResults() throws Exception {
        // Given
        List<AlertResponse> alerts = Arrays.asList(testAlertResponse);
        Page<AlertResponse> alertPage = new PageImpl<>(alerts, PageRequest.of(0, 20), 1);
        
        when(alertService.getAllAlerts(any())).thenReturn(alertPage);
        
        // When & Then
        mockMvc.perform(get("/api/alerts")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(20));
        
        verify(alertService).getAllAlerts(any());
    }
    
    @Test
    void getAlertsByDevice_ShouldReturnDeviceAlerts() throws Exception {
        // Given
        List<AlertResponse> alerts = Arrays.asList(testAlertResponse);
        when(alertService.getAlertsByDevice(1L)).thenReturn(alerts);
        
        // When & Then
        mockMvc.perform(get("/api/alerts/device/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].deviceId").value(1L));
        
        verify(alertService).getAlertsByDevice(1L);
    }
    
    @Test
    void getAlertsBySite_ShouldReturnSiteAlerts() throws Exception {
        // Given
        List<AlertResponse> alerts = Arrays.asList(testAlertResponse);
        when(alertService.getAlertsBySite(1L)).thenReturn(alerts);
        
        // When & Then
        mockMvc.perform(get("/api/alerts/site/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].siteId").value(1L));
        
        verify(alertService).getAlertsBySite(1L);
    }
    
    @Test
    void getUnacknowledgedAlerts_ShouldReturnUnacknowledgedAlerts() throws Exception {
        // Given
        List<AlertResponse> alerts = Arrays.asList(testAlertResponse);
        when(alertService.getUnacknowledgedAlerts()).thenReturn(alerts);
        
        // When & Then
        mockMvc.perform(get("/api/alerts/unacknowledged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].acknowledged").value(false));
        
        verify(alertService).getUnacknowledgedAlerts();
    }
    
    @Test
    void acknowledgeAlert_WithValidRequest_ShouldAcknowledgeAlert() throws Exception {
        // Given
        testAlertResponse.setAcknowledged(true);
        testAlertResponse.setAcknowledgedBy(100L);
        testAlertResponse.setAcknowledgedAt(LocalDateTime.now());
        
        when(alertService.acknowledgeAlert(1L, 100L)).thenReturn(testAlertResponse);
        
        // When & Then
        mockMvc.perform(put("/api/alerts/1/acknowledge")
                .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.acknowledged").value(true))
                .andExpect(jsonPath("$.acknowledgedBy").value(100L));
        
        verify(alertService).acknowledgeAlert(1L, 100L);
    }
    
    @Test
    void acknowledgeAlert_WithMissingUserId_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/alerts/1/acknowledge"))
                .andExpect(status().isBadRequest());
        
        verify(alertService, never()).acknowledgeAlert(anyLong(), anyLong());
    }
    
    @Test
    void acknowledgeAlert_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(alertService.acknowledgeAlert(1L, 100L))
            .thenThrow(new RuntimeException("Alert is already acknowledged"));
        
        // When & Then
        mockMvc.perform(put("/api/alerts/1/acknowledge")
                .param("userId", "100"))
                .andExpect(status().isBadRequest());
        
        verify(alertService).acknowledgeAlert(1L, 100L);
    }
    
    @Test
    void resolveAlert_WithValidRequest_ShouldResolveAlert() throws Exception {
        // Given
        testAlertResponse.setResolvedAt(LocalDateTime.now());
        testAlertResponse.setAcknowledged(true);
        testAlertResponse.setAcknowledgedBy(100L);
        
        when(alertService.resolveAlert(1L, 100L)).thenReturn(testAlertResponse);
        
        // When & Then
        mockMvc.perform(put("/api/alerts/1/resolve")
                .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.resolvedAt").exists())
                .andExpect(jsonPath("$.acknowledged").value(true));
        
        verify(alertService).resolveAlert(1L, 100L);
    }
    
    @Test
    void searchAlerts_WithFilters_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<AlertResponse> alerts = Arrays.asList(testAlertResponse);
        Page<AlertResponse> alertPage = new PageImpl<>(alerts, PageRequest.of(0, 20), 1);
        
        when(alertService.searchAlerts(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(alertPage);
        
        // When & Then
        mockMvc.perform(get("/api/alerts/search")
                .param("deviceId", "1")
                .param("siteId", "1")
                .param("type", "TEMPERATURE")
                .param("severity", "HIGH")
                .param("acknowledged", "false")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
        
        verify(alertService).searchAlerts(any(), any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    void getDashboardSummary_ShouldReturnAlertCounts() throws Exception {
        // Given
        when(alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.CRITICAL)).thenReturn(2L);
        when(alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.HIGH)).thenReturn(5L);
        when(alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.MEDIUM)).thenReturn(3L);
        when(alertService.countUnacknowledgedBySeverity(Alert.AlertSeverity.LOW)).thenReturn(1L);
        when(alertService.getUnacknowledgedAlerts()).thenReturn(Arrays.asList(testAlertResponse));
        
        // When & Then
        mockMvc.perform(get("/api/alerts/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCritical").value(2))
                .andExpect(jsonPath("$.totalHigh").value(5))
                .andExpect(jsonPath("$.totalMedium").value(3))
                .andExpect(jsonPath("$.totalLow").value(1))
                .andExpect(jsonPath("$.totalUnacknowledged").value(1));
        
        verify(alertService, times(4)).countUnacknowledgedBySeverity(any());
        verify(alertService).getUnacknowledgedAlerts();
    }
    
    @Test
    void deleteAlert_WhenAlertExists_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(alertService).deleteAlert(1L);
        
        // When & Then
        mockMvc.perform(delete("/api/alerts/1"))
                .andExpect(status().isNoContent());
        
        verify(alertService).deleteAlert(1L);
    }
    
    @Test
    void deleteAlert_WhenAlertDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("Alert not found")).when(alertService).deleteAlert(1L);
        
        // When & Then
        mockMvc.perform(delete("/api/alerts/1"))
                .andExpect(status().isNotFound());
        
        verify(alertService).deleteAlert(1L);
    }
    
    @Test
    void healthCheck_ShouldReturnHealthyStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/alerts/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("notification-service"));
    }
}