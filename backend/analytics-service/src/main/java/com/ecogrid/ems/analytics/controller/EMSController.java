package com.ecogrid.ems.analytics.controller;

import com.ecogrid.ems.analytics.dto.SiteDashboardResponse;
import com.ecogrid.ems.analytics.dto.DeviceTypeMetricsResponse;
import com.ecogrid.ems.analytics.service.EMSAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for EMS-specific analytics endpoints
 */
@RestController
@RequestMapping("/api/v1/ems")
public class EMSController {

    private static final Logger logger = LoggerFactory.getLogger(EMSController.class);

    private final EMSAnalyticsService emsAnalyticsService;

    public EMSController(EMSAnalyticsService emsAnalyticsService) {
        this.emsAnalyticsService = emsAnalyticsService;
    }

    /**
     * Get all sites for dropdown/selector
     */
    @GetMapping("/sites")
    public ResponseEntity<?> getAllSites() {
        try {
            List<SiteDashboardResponse.SiteInfo> sites = emsAnalyticsService.getAllSites();
            logger.info("Retrieved {} sites", sites.size());
            return ResponseEntity.ok(sites);
        } catch (Exception e) {
            logger.error("Failed to retrieve sites", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve sites"));
        }
    }

    /**
     * Get comprehensive dashboard data for a specific site
     */
    @GetMapping("/sites/{siteId}/dashboard")
    public ResponseEntity<?> getSiteDashboard(@PathVariable String siteId) {
        try {
            SiteDashboardResponse dashboard = emsAnalyticsService.getSiteDashboard(siteId);
            logger.info("Site dashboard retrieved for site: {}", siteId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Failed to retrieve site dashboard for site: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve site dashboard", "siteId", siteId));
        }
    }

    /**
     * Get device type specific metrics with time-series data
     */
    @GetMapping("/sites/{siteId}/devices/{deviceType}/metrics")
    public ResponseEntity<?> getDeviceTypeMetrics(
            @PathVariable String siteId,
            @PathVariable String deviceType,
            @RequestParam(defaultValue = "1h") String interval,
            @RequestParam(defaultValue = "24") int hoursBack) {
        try {
            DeviceTypeMetricsResponse metrics = emsAnalyticsService.getDeviceTypeMetrics(
                    siteId, deviceType, interval, hoursBack);
            logger.info("Device metrics retrieved for site: {}, deviceType: {}", siteId, deviceType);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Failed to retrieve device metrics for site: {}, deviceType: {}", 
                    siteId, deviceType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to retrieve device metrics",
                            "siteId", siteId,
                            "deviceType", deviceType
                    ));
        }
    }

    /**
     * Get battery system specific metrics
     */
    @GetMapping("/sites/{siteId}/bms")
    public ResponseEntity<?> getBMSMetrics(
            @PathVariable String siteId,
            @RequestParam(defaultValue = "1h") String interval,
            @RequestParam(defaultValue = "24") int hoursBack) {
        return getDeviceTypeMetrics(siteId, "BMS", interval, hoursBack);
    }

    /**
     * Get solar array specific metrics
     */
    @GetMapping("/sites/{siteId}/solar")
    public ResponseEntity<?> getSolarMetrics(
            @PathVariable String siteId,
            @RequestParam(defaultValue = "1h") String interval,
            @RequestParam(defaultValue = "24") int hoursBack) {
        return getDeviceTypeMetrics(siteId, "SOLAR_ARRAY", interval, hoursBack);
    }

    /**
     * Get EV charger specific metrics
     */
    @GetMapping("/sites/{siteId}/chargers")
    public ResponseEntity<?> getChargerMetrics(
            @PathVariable String siteId,
            @RequestParam(defaultValue = "1h") String interval,
            @RequestParam(defaultValue = "24") int hoursBack) {
        return getDeviceTypeMetrics(siteId, "EV_CHARGER", interval, hoursBack);
    }

    /**
     * Get site alerts summary
     */
    @GetMapping("/sites/{siteId}/alerts")
    public ResponseEntity<?> getSiteAlerts(
            @PathVariable String siteId,
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "24") int hoursBack) {
        try {
            SiteDashboardResponse.AlertSummary alerts = emsAnalyticsService.getSiteAlerts(siteId, status, hoursBack);
            logger.info("Site alerts retrieved for site: {}", siteId);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Failed to retrieve site alerts for site: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve site alerts", "siteId", siteId));
        }
    }

    /**
     * Get site performance metrics
     */
    @GetMapping("/sites/{siteId}/performance")
    public ResponseEntity<?> getSitePerformance(
            @PathVariable String siteId,
            @RequestParam(defaultValue = "24") int hoursBack) {
        try {
            SiteDashboardResponse.PerformanceMetrics performance = 
                    emsAnalyticsService.getSitePerformance(siteId, hoursBack);
            logger.info("Site performance metrics retrieved for site: {}", siteId);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            logger.error("Failed to retrieve site performance for site: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve site performance", "siteId", siteId));
        }
    }

    /**
     * Get real-time site status (for health checks)
     */
    @GetMapping("/sites/{siteId}/status")
    public ResponseEntity<?> getSiteStatus(@PathVariable String siteId) {
        try {
            Map<String, Object> status = emsAnalyticsService.getSiteStatus(siteId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Failed to retrieve site status for site: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve site status", "siteId", siteId));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "EMS Analytics"
        ));
    }
}