package com.ecogrid.ems.analytics.service;

import com.ecogrid.ems.analytics.dto.SiteDashboardResponse;
import com.ecogrid.ems.analytics.dto.DeviceTypeMetricsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Service for EMS-specific analytics operations
 * TODO: Replace mock data with actual database queries once shared module is integrated
 */
@Service
public class EMSAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(EMSAnalyticsService.class);

    public EMSAnalyticsService() {
        logger.info("EMSAnalyticsService initialized with mock data");
    }

    /**
     * Get all sites for dropdown/selector
     */
    public List<SiteDashboardResponse.SiteInfo> getAllSites() {
        List<SiteDashboardResponse.SiteInfo> sites = new ArrayList<>();
        
        // Mock Site 1
        SiteDashboardResponse.SiteInfo site1 = new SiteDashboardResponse.SiteInfo();
        site1.setId(1L);
        site1.setName("EcoGrid Main Campus");
        site1.setLocation("San Francisco, CA");
        site1.setCapacity(BigDecimal.valueOf(2.5)); // 2.5 MW
        site1.setStatus("ACTIVE");
        site1.setLastUpdated(LocalDateTime.now());
        site1.setCoordinates(new SiteDashboardResponse.SiteInfo.Coordinates(
                BigDecimal.valueOf(37.7749), BigDecimal.valueOf(-122.4194)));
        sites.add(site1);
        
        // Mock Site 2
        SiteDashboardResponse.SiteInfo site2 = new SiteDashboardResponse.SiteInfo();
        site2.setId(2L);
        site2.setName("EcoGrid Research Facility");
        site2.setLocation("Austin, TX");
        site2.setCapacity(BigDecimal.valueOf(1.8)); // 1.8 MW
        site2.setStatus("ACTIVE");
        site2.setLastUpdated(LocalDateTime.now());
        site2.setCoordinates(new SiteDashboardResponse.SiteInfo.Coordinates(
                BigDecimal.valueOf(30.2672), BigDecimal.valueOf(-97.7431)));
        sites.add(site2);
        
        return sites;
    }

    /**
     * Get comprehensive dashboard data for a specific site
     */
    public SiteDashboardResponse getSiteDashboard(String siteId) {
        SiteDashboardResponse response = new SiteDashboardResponse();
        
        // Get site info from mock data
        List<SiteDashboardResponse.SiteInfo> allSites = getAllSites();
        SiteDashboardResponse.SiteInfo siteInfo = allSites.stream()
                .filter(site -> site.getId().equals(Long.valueOf(siteId)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Site not found: " + siteId));
        
        response.setSiteInfo(siteInfo);
        response.setBatterySystem(getBatterySystemMetrics(siteId));
        response.setSolarArray(getSolarArrayMetrics(siteId));
        response.setEvChargers(getEVChargerMetrics(siteId));
        response.setAlerts(getSiteAlerts(siteId, "ACTIVE", 24));
        response.setPerformance(getSitePerformance(siteId, 24));

        return response;
    }

    /**
     * Get device type specific metrics with time-series data
     */
    public DeviceTypeMetricsResponse getDeviceTypeMetrics(String siteId, String deviceType, String interval, int hoursBack) {
        DeviceTypeMetricsResponse response = new DeviceTypeMetricsResponse();
        response.setDeviceType(deviceType);
        response.setSiteId(siteId);
        response.setLastUpdated(LocalDateTime.now());

        // Mock device counts based on device type
        switch (deviceType) {
            case "BMS":
                response.setTotalDevices(4);
                response.setActiveDevices(3);
                response.setFaultedDevices(1);
                break;
            case "SOLAR_ARRAY":
                response.setTotalDevices(8);
                response.setActiveDevices(7);
                response.setFaultedDevices(1);
                break;
            case "EV_CHARGER":
                response.setTotalDevices(12);
                response.setActiveDevices(10);
                response.setFaultedDevices(2);
                break;
            default:
                response.setTotalDevices(0);
                response.setActiveDevices(0);
                response.setFaultedDevices(0);
        }

        response.setTimeSeries(buildMockTimeSeriesData(deviceType, interval, hoursBack));
        response.setAggregated(buildMockAggregatedMetrics(deviceType));
        response.setDeviceStatuses(buildMockDeviceStatuses(deviceType, response.getTotalDevices()));

        return response;
    }

    /**
     * Get site alerts summary
     */
    public SiteDashboardResponse.AlertSummary getSiteAlerts(String siteId, String status, int hoursBack) {
        SiteDashboardResponse.AlertSummary alertSummary = new SiteDashboardResponse.AlertSummary();
        alertSummary.setCritical(2);
        alertSummary.setHigh(5);
        alertSummary.setMedium(12);
        alertSummary.setLow(8);
        alertSummary.setTotalActive(27);
        alertSummary.setResolved24h(15);
        return alertSummary;
    }

    /**
     * Get site performance metrics
     */
    public SiteDashboardResponse.PerformanceMetrics getSitePerformance(String siteId, int hoursBack) {
        SiteDashboardResponse.PerformanceMetrics performance = new SiteDashboardResponse.PerformanceMetrics();
        performance.setUptime(BigDecimal.valueOf(98.5));
        performance.setAvailability(BigDecimal.valueOf(97.2));
        performance.setTotalFaults(3);
        performance.setEfficiency(BigDecimal.valueOf(87.5));
        performance.setCarbonOffset(BigDecimal.valueOf(1250.75));
        return performance;
    }

    /**
     * Get real-time site status
     */
    public Map<String, Object> getSiteStatus(String siteId) {
        Map<String, Object> status = new HashMap<>();
        status.put("siteId", siteId);
        status.put("siteName", "EcoGrid Main Campus");
        status.put("status", "ACTIVE");
        status.put("totalDevices", 24);
        status.put("activeDevices", 20);
        status.put("lastUpdated", LocalDateTime.now());
        return status;
    }

    // Private helper methods for mock data

    private SiteDashboardResponse.BatterySystemMetrics getBatterySystemMetrics(String siteId) {
        SiteDashboardResponse.BatterySystemMetrics metrics = new SiteDashboardResponse.BatterySystemMetrics();
        metrics.setTotalCapacity(BigDecimal.valueOf(500.0));
        metrics.setAvgSOC(BigDecimal.valueOf(78.5));
        metrics.setTotalChargeRate(BigDecimal.valueOf(125.3));
        metrics.setAvgTemperature(BigDecimal.valueOf(24.8));
        metrics.setHealthStatus("GOOD");
        metrics.setEfficiency(BigDecimal.valueOf(92.5));
        metrics.setActiveDevices(3);
        metrics.setOfflineDevices(1);
        return metrics;
    }

    private SiteDashboardResponse.SolarArrayMetrics getSolarArrayMetrics(String siteId) {
        SiteDashboardResponse.SolarArrayMetrics metrics = new SiteDashboardResponse.SolarArrayMetrics();
        metrics.setTotalOutput(BigDecimal.valueOf(450.8));
        metrics.setDailyYield(BigDecimal.valueOf(1250.5));
        metrics.setAvgEfficiency(BigDecimal.valueOf(18.5));
        metrics.setAvgPanelTemp(BigDecimal.valueOf(32.1));
        metrics.setIrradiance(BigDecimal.valueOf(850.3));
        metrics.setPerformanceRatio(BigDecimal.valueOf(88.5));
        metrics.setActiveStrings(7);
        metrics.setFaultedStrings(1);
        return metrics;
    }

    private SiteDashboardResponse.EVChargerMetrics getEVChargerMetrics(String siteId) {
        SiteDashboardResponse.EVChargerMetrics metrics = new SiteDashboardResponse.EVChargerMetrics();
        metrics.setTotalChargers(12);
        metrics.setActiveChargers(10);
        metrics.setActiveSessions(6);
        metrics.setTotalPowerDelivery(BigDecimal.valueOf(180.5));
        metrics.setDailyRevenue(BigDecimal.valueOf(875.25));
        metrics.setAvgUtilization(BigDecimal.valueOf(65.5));
        metrics.setDailyEnergy(BigDecimal.valueOf(2150.75));
        metrics.setAvgSessionDuration(BigDecimal.valueOf(45.5));
        return metrics;
    }

    private DeviceTypeMetricsResponse.TimeSeriesData buildMockTimeSeriesData(String deviceType, String interval, int hoursBack) {
        DeviceTypeMetricsResponse.TimeSeriesData timeSeriesData = new DeviceTypeMetricsResponse.TimeSeriesData();
        timeSeriesData.setInterval(interval);
        timeSeriesData.setStartTime(LocalDateTime.now().minusHours(hoursBack));
        timeSeriesData.setEndTime(LocalDateTime.now());
        
        List<DeviceTypeMetricsResponse.TimeSeriesData.DataPoint> dataPoints = new ArrayList<>();
        
        // Generate mock data points
        for (int i = 0; i < Math.min(hoursBack, 24); i++) {
            DeviceTypeMetricsResponse.TimeSeriesData.MetricValues values = 
                    new DeviceTypeMetricsResponse.TimeSeriesData.MetricValues();
            
            // Mock values based on device type
            switch (deviceType) {
                case "BMS":
                    values.setPower(BigDecimal.valueOf(120 + (Math.random() * 20)));
                    values.setSoc(BigDecimal.valueOf(75 + (Math.random() * 20)));
                    values.setTemperature(BigDecimal.valueOf(23 + (Math.random() * 5)));
                    break;
                case "SOLAR_ARRAY":
                    values.setPower(BigDecimal.valueOf(400 + (Math.random() * 100)));
                    values.setIrradiance(BigDecimal.valueOf(800 + (Math.random() * 200)));
                    values.setEfficiency(BigDecimal.valueOf(17 + (Math.random() * 3)));
                    break;
                case "EV_CHARGER":
                    values.setPower(BigDecimal.valueOf(150 + (Math.random() * 50)));
                    values.setActiveSessions((int) (Math.random() * 8));
                    values.setUtilization(BigDecimal.valueOf(60 + (Math.random() * 30)));
                    break;
            }
            
            DeviceTypeMetricsResponse.TimeSeriesData.DataPoint dataPoint = 
                    new DeviceTypeMetricsResponse.TimeSeriesData.DataPoint(
                            LocalDateTime.now().minusHours(hoursBack - i), values);
            dataPoints.add(dataPoint);
        }
        
        timeSeriesData.setDataPoints(dataPoints);
        return timeSeriesData;
    }

    private DeviceTypeMetricsResponse.AggregatedMetrics buildMockAggregatedMetrics(String deviceType) {
        DeviceTypeMetricsResponse.AggregatedMetrics aggregated = new DeviceTypeMetricsResponse.AggregatedMetrics();
        
        switch (deviceType) {
            case "BMS":
                aggregated.setTotalPower(BigDecimal.valueOf(480.5));
                aggregated.setTotalEnergy(BigDecimal.valueOf(11532.0));
                aggregated.setAvgSOC(BigDecimal.valueOf(78.5));
                aggregated.setTotalCapacity(BigDecimal.valueOf(500.0));
                break;
            case "SOLAR_ARRAY":
                aggregated.setTotalPower(BigDecimal.valueOf(450.8));
                aggregated.setTotalEnergy(BigDecimal.valueOf(10819.2));
                aggregated.setAvgIrradiance(BigDecimal.valueOf(850.3));
                aggregated.setPerformanceRatio(BigDecimal.valueOf(88.5));
                break;
            case "EV_CHARGER":
                aggregated.setTotalPower(BigDecimal.valueOf(180.5));
                aggregated.setTotalSessions(48);
                aggregated.setTotalRevenue(BigDecimal.valueOf(875.25));
                break;
        }
        
        aggregated.setAvgEfficiency(BigDecimal.valueOf(87.5));
        aggregated.setAvgTemperature(BigDecimal.valueOf(24.8));
        
        return aggregated;
    }

    private List<DeviceTypeMetricsResponse.DeviceStatus> buildMockDeviceStatuses(String deviceType, int totalDevices) {
        List<DeviceTypeMetricsResponse.DeviceStatus> deviceStatuses = new ArrayList<>();
        
        for (int i = 1; i <= totalDevices; i++) {
            DeviceTypeMetricsResponse.DeviceStatus status = new DeviceTypeMetricsResponse.DeviceStatus();
            status.setDeviceId((long) i);
            status.setDeviceName(deviceType + "-" + String.format("%02d", i));
            status.setStatus(i <= (totalDevices - 1) ? "ACTIVE" : "FAULT");
            status.setLastSeen(LocalDateTime.now().minusMinutes((long) (Math.random() * 30)));
            
            switch (deviceType) {
                case "BMS":
                    status.setCurrentValue(BigDecimal.valueOf(120 + (Math.random() * 20)));
                    status.setUnit("kW");
                    break;
                case "SOLAR_ARRAY":
                    status.setCurrentValue(BigDecimal.valueOf(400 + (Math.random() * 100)));
                    status.setUnit("kW");
                    break;
                case "EV_CHARGER":
                    status.setCurrentValue(BigDecimal.valueOf(150 + (Math.random() * 50)));
                    status.setUnit("kW");
                    break;
            }
            
            deviceStatuses.add(status);
        }
        
        return deviceStatuses;
    }
}