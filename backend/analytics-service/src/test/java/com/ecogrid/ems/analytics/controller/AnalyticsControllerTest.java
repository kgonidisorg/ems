package com.ecogrid.ems.analytics.controller;

import com.ecogrid.ems.analytics.dto.DashboardResponse;
import com.ecogrid.ems.analytics.dto.ReportRequest;
import com.ecogrid.ems.analytics.dto.ReportResponse;
import com.ecogrid.ems.analytics.service.AnalyticsService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AnalyticsController
 */
@WebMvcTest(AnalyticsController.class)
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Autowired
    private ObjectMapper objectMapper;

    private DashboardResponse dashboardResponse;
    private ReportRequest reportRequest;
    private ReportResponse reportResponse;

    @BeforeEach
    void setUp() {
        // Setup mock dashboard response
        dashboardResponse = new DashboardResponse(
                new BigDecimal("4200.50"), // totalEnergyConsumed
                new BigDecimal("4800.25"), // totalEnergyGenerated
                new BigDecimal("320.75"),  // carbonFootprintReduced
                new BigDecimal("1250.00"), // costSavings
                3, // activeSites
                15, // activeDevices
                new BigDecimal("87.5"), // averageEfficiency
                Arrays.asList(
                    new DashboardResponse.TimeSeriesDataPoint(
                        LocalDateTime.now(),
                        new BigDecimal("150.5"),
                        new BigDecimal("180.2"),
                        new BigDecimal("25.3"),
                        new BigDecimal("45.75")
                    )
                ),
                Map.of("Main Campus", new BigDecimal("1250.50")),
                Map.of("SOLAR_INVERTER", new BigDecimal("1800.30")),
                LocalDateTime.now()
        );

        // Setup mock report request
        reportRequest = new ReportRequest(
                "Monthly Energy Report",
                "Comprehensive energy consumption report",
                "ENERGY_CONSUMPTION",
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now(),
                1L,
                "PDF",
                Map.of("includeCharts", "true"),
                "MONTHLY"
        );

        // Setup mock report response
        reportResponse = new ReportResponse(
                1L,
                "Monthly Energy Report",
                "Comprehensive energy consumption report",
                "ENERGY_CONSUMPTION",
                "COMPLETED",
                "PDF",
                "/reports/monthly_energy_report.pdf",
                1024L,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now(),
                1L,
                "Main Site",
                Map.of("includeCharts", "true"),
                "MONTHLY",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "system"
        );
    }

    @Test
    void getDashboard_ValidRequest_ShouldReturnDashboardData() throws Exception {
        // Arrange
        when(analyticsService.getDashboardData(24, null)).thenReturn(dashboardResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard")
                        .param("hoursBack", "24"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalEnergyConsumed").value(4200.50))
                .andExpect(jsonPath("$.totalEnergyGenerated").value(4800.25))
                .andExpect(jsonPath("$.activeSites").value(3))
                .andExpect(jsonPath("$.activeDevices").value(15));

        verify(analyticsService).getDashboardData(24, null);
    }

    @Test
    void getDashboard_WithSiteId_ShouldReturnFilteredData() throws Exception {
        // Arrange
        when(analyticsService.getDashboardData(12, 1L)).thenReturn(dashboardResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard")
                        .param("hoursBack", "12")
                        .param("siteId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalEnergyConsumed").value(4200.50));

        verify(analyticsService).getDashboardData(12, 1L);
    }

    @Test
    void getDashboard_ServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(analyticsService.getDashboardData(anyInt(), any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Failed to retrieve dashboard data"));

        verify(analyticsService).getDashboardData(24, null);
    }

    @Test
    void getEnergyConsumption_ValidRequest_ShouldReturnEnergyData() throws Exception {
        // Arrange
        Map<String, Object> energyData = new HashMap<>();
        energyData.put("totalConsumption", new BigDecimal("5250.75"));
        energyData.put("averageConsumption", new BigDecimal("218.78"));
        energyData.put("aggregation", "HOURLY");
        
        when(analyticsService.getEnergyConsumption(any(), any(), any(), eq("HOURLY")))
                .thenReturn(energyData);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/energy/consumption")
                        .param("aggregation", "HOURLY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalConsumption").value(5250.75))
                .andExpect(jsonPath("$.aggregation").value("HOURLY"));

        verify(analyticsService).getEnergyConsumption(any(), any(), any(), eq("HOURLY"));
    }

    @Test
    void getCarbonFootprint_ValidRequest_ShouldReturnCarbonData() throws Exception {
        // Arrange
        Map<String, Object> carbonData = new HashMap<>();
        carbonData.put("totalCarbonSaved", new BigDecimal("450.25"));
        carbonData.put("equivalentTrees", 22);
        
        when(analyticsService.getCarbonFootprint(any(), any(), any()))
                .thenReturn(carbonData);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/carbon/footprint"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCarbonSaved").value(450.25))
                .andExpect(jsonPath("$.equivalentTrees").value(22));

        verify(analyticsService).getCarbonFootprint(any(), any(), any());
    }

    @Test
    void getFinancialMetrics_ValidRequest_ShouldReturnFinancialData() throws Exception {
        // Arrange
        Map<String, Object> financialData = new HashMap<>();
        financialData.put("totalSavings", new BigDecimal("2450.75"));
        financialData.put("roi", new BigDecimal("15.75"));
        
        when(analyticsService.getFinancialMetrics(any(), any(), any()))
                .thenReturn(financialData);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/financial/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalSavings").value(2450.75))
                .andExpect(jsonPath("$.roi").value(15.75));

        verify(analyticsService).getFinancialMetrics(any(), any(), any());
    }

    @Test
    void createReport_ValidRequest_ShouldReturnCreatedReport() throws Exception {
        // Arrange
        when(analyticsService.createReport(any(ReportRequest.class))).thenReturn(reportResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/analytics/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Energy Report"))
                .andExpect(jsonPath("$.reportType").value("ENERGY_CONSUMPTION"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(analyticsService).createReport(any(ReportRequest.class));
    }

    @Test
    void createReport_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(analyticsService.createReport(any(ReportRequest.class)))
                .thenThrow(new IllegalArgumentException("Report name is required"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/analytics/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Report name is required"));

        verify(analyticsService).createReport(any(ReportRequest.class));
    }

    @Test
    void createReport_ServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(analyticsService.createReport(any(ReportRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/analytics/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Failed to create report"));

        verify(analyticsService).createReport(any(ReportRequest.class));
    }

    @Test
    void getAllReports_ValidRequest_ShouldReturnReportPage() throws Exception {
        // Arrange
        List<ReportResponse> reports = Arrays.asList(reportResponse);
        Page<ReportResponse> reportPage = new PageImpl<>(reports, PageRequest.of(0, 10), 1);
        
        when(analyticsService.getAllReports(any(), eq("createdAt"), eq("desc")))
                .thenReturn(reportPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/reports")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(analyticsService).getAllReports(any(), eq("createdAt"), eq("desc"));
    }

    @Test
    void getReportById_ExistingReport_ShouldReturnReport() throws Exception {
        // Arrange
        when(analyticsService.getReportById(1L)).thenReturn(reportResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/reports/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Energy Report"));

        verify(analyticsService).getReportById(1L);
    }

    @Test
    void getReportById_NonExistingReport_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(analyticsService.getReportById(999L))
                .thenThrow(new IllegalArgumentException("Report not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/reports/999"))
                .andExpect(status().isNotFound());

        verify(analyticsService).getReportById(999L);
    }

    @Test
    void deleteReport_ExistingReport_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(analyticsService).deleteReport(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/analytics/reports/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Report deleted successfully"));

        verify(analyticsService).deleteReport(1L);
    }

    @Test
    void deleteReport_NonExistingReport_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Report not found"))
                .when(analyticsService).deleteReport(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/analytics/reports/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Report not found"));

        verify(analyticsService).deleteReport(999L);
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("analytics-service"));
    }
}