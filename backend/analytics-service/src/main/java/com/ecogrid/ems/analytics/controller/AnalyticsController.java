package com.ecogrid.ems.analytics.controller;

import com.ecogrid.ems.analytics.dto.DashboardResponse;
import com.ecogrid.ems.analytics.dto.ReportRequest;
import com.ecogrid.ems.analytics.dto.ReportResponse;
import com.ecogrid.ems.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for Analytics endpoints
 */
@RestController
@RequestMapping("/api/v1/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get dashboard analytics data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestParam(defaultValue = "24") int hoursBack,
            @RequestParam(required = false) Long siteId) {
        try {
            DashboardResponse dashboard = analyticsService.getDashboardData(hoursBack, siteId);
            logger.info("Dashboard data retrieved successfully for {} hours", hoursBack);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Failed to retrieve dashboard data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve dashboard data"));
        }
    }

    /**
     * Get energy consumption analytics
     */
    @GetMapping("/energy/consumption")
    public ResponseEntity<?> getEnergyConsumption(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Long siteId,
            @RequestParam(defaultValue = "HOURLY") String aggregation) {
        try {
            var energyData = analyticsService.getEnergyConsumption(startDate, endDate, siteId, aggregation);
            logger.info("Energy consumption data retrieved for aggregation: {}", aggregation);
            return ResponseEntity.ok(energyData);
        } catch (Exception e) {
            logger.error("Failed to retrieve energy consumption data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve energy consumption data"));
        }
    }

    /**
     * Get carbon footprint analytics
     */
    @GetMapping("/carbon/footprint")
    public ResponseEntity<?> getCarbonFootprint(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Long siteId) {
        try {
            var carbonData = analyticsService.getCarbonFootprint(startDate, endDate, siteId);
            logger.info("Carbon footprint data retrieved successfully");
            return ResponseEntity.ok(carbonData);
        } catch (Exception e) {
            logger.error("Failed to retrieve carbon footprint data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve carbon footprint data"));
        }
    }

    /**
     * Get financial metrics
     */
    @GetMapping("/financial/metrics")
    public ResponseEntity<?> getFinancialMetrics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Long siteId) {
        try {
            var financialData = analyticsService.getFinancialMetrics(startDate, endDate, siteId);
            logger.info("Financial metrics retrieved successfully");
            return ResponseEntity.ok(financialData);
        } catch (Exception e) {
            logger.error("Failed to retrieve financial metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve financial metrics"));
        }
    }

    /**
     * Create a new report
     */
    @PostMapping("/reports")
    public ResponseEntity<?> createReport(@Valid @RequestBody ReportRequest request) {
        try {
            ReportResponse report = analyticsService.createReport(request);
            logger.info("Report created successfully: {}", report.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (IllegalArgumentException e) {
            logger.warn("Report creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Report creation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create report"));
        }
    }

    /**
     * Get all reports with pagination
     */
    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReportResponse> reports = analyticsService.getAllReports(pageable, sortBy, sortDirection);
            logger.info("Retrieved {} reports (page {} of {})", reports.getNumberOfElements(), 
                       page + 1, reports.getTotalPages());
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Failed to retrieve reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve reports"));
        }
    }

    /**
     * Get report by ID
     */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<?> getReportById(@PathVariable Long reportId) {
        try {
            ReportResponse report = analyticsService.getReportById(reportId);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            logger.warn("Report not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to retrieve report by ID: {}", reportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve report"));
        }
    }

    /**
     * Delete report
     */
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        try {
            analyticsService.deleteReport(reportId);
            logger.info("Report deleted successfully: {}", reportId);
            return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Report deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Report deletion failed for ID: {}", reportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete report"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "analytics-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}