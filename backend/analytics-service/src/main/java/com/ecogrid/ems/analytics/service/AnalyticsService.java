package com.ecogrid.ems.analytics.service;

import com.ecogrid.ems.analytics.dto.DashboardResponse;
import com.ecogrid.ems.analytics.dto.ReportRequest;
import com.ecogrid.ems.analytics.dto.ReportResponse;
import com.ecogrid.ems.analytics.entity.Report;
import com.ecogrid.ems.analytics.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Analytics operations
 */
@Service
@Transactional
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final ReportRepository reportRepository;

    public AnalyticsService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Get dashboard analytics data
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(int hoursBack, Long siteId) {
        logger.info("Generating dashboard data for {} hours back, siteId: {}", hoursBack, siteId);
        
        // Generate mock data for now - in real implementation this would query actual telemetry data
        LocalDateTime now = LocalDateTime.now();
        
        // Create time series data points
        List<DashboardResponse.TimeSeriesDataPoint> timeSeriesData = new ArrayList<>();
        for (int i = hoursBack; i >= 0; i--) {
            LocalDateTime timestamp = now.minusHours(i);
            timeSeriesData.add(new DashboardResponse.TimeSeriesDataPoint(
                timestamp,
                new BigDecimal("150.5").add(new BigDecimal(Math.random() * 100)), // energyConsumed
                new BigDecimal("180.2").add(new BigDecimal(Math.random() * 50)),  // energyGenerated
                new BigDecimal("25.3").add(new BigDecimal(Math.random() * 10)),   // carbonSaved
                new BigDecimal("45.75").add(new BigDecimal(Math.random() * 20))   // costSavings
            ));
        }

        // Site breakdown data
        Map<String, BigDecimal> siteBreakdown = Map.of(
            "Main Campus", new BigDecimal("1250.50"),
            "Production Facility", new BigDecimal("2100.75"),
            "Warehouse", new BigDecimal("850.25")
        );

        // Device type breakdown
        Map<String, BigDecimal> deviceTypeBreakdown = Map.of(
            "SOLAR_INVERTER", new BigDecimal("1800.30"),
            "BATTERY_STORAGE", new BigDecimal("1200.20"),
            "WIND_TURBINE", new BigDecimal("1200.00")
        );

        return new DashboardResponse(
            new BigDecimal("4200.50"), // totalEnergyConsumed
            new BigDecimal("4800.25"), // totalEnergyGenerated
            new BigDecimal("320.75"),  // carbonFootprintReduced
            new BigDecimal("1250.00"), // costSavings
            3, // activeSites
            15, // activeDevices
            new BigDecimal("87.5"), // averageEfficiency
            timeSeriesData,
            siteBreakdown,
            deviceTypeBreakdown,
            now // lastUpdated
        );
    }

    /**
     * Get energy consumption data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEnergyConsumption(LocalDateTime startDate, LocalDateTime endDate, 
                                                   Long siteId, String aggregation) {
        logger.info("Getting energy consumption data from {} to {} for site {}", startDate, endDate, siteId);
        
        // Mock implementation - replace with actual data querying
        Map<String, Object> result = new HashMap<>();
        result.put("totalConsumption", new BigDecimal("5250.75"));
        result.put("averageConsumption", new BigDecimal("218.78"));
        result.put("peakConsumption", new BigDecimal("425.50"));
        result.put("aggregation", aggregation);
        result.put("periodStart", startDate != null ? startDate : LocalDateTime.now().minusDays(7));
        result.put("periodEnd", endDate != null ? endDate : LocalDateTime.now());
        
        // Generate sample data points
        List<Map<String, Object>> dataPoints = new ArrayList<>();
        LocalDateTime current = (startDate != null ? startDate : LocalDateTime.now().minusDays(7));
        LocalDateTime end = (endDate != null ? endDate : LocalDateTime.now());
        
        while (current.isBefore(end)) {
            Map<String, Object> point = new HashMap<>();
            point.put("timestamp", current);
            point.put("consumption", new BigDecimal(200 + Math.random() * 100));
            dataPoints.add(point);
            current = current.plusHours(1);
        }
        
        result.put("dataPoints", dataPoints);
        return result;
    }

    /**
     * Get carbon footprint data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCarbonFootprint(LocalDateTime startDate, LocalDateTime endDate, Long siteId) {
        logger.info("Getting carbon footprint data from {} to {} for site {}", startDate, endDate, siteId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCarbonSaved", new BigDecimal("450.25")); // in kg CO2
        result.put("carbonIntensity", new BigDecimal("0.85")); // kg CO2 per kWh
        result.put("equivalentTrees", 22); // equivalent trees planted
        result.put("periodStart", startDate != null ? startDate : LocalDateTime.now().minusDays(30));
        result.put("periodEnd", endDate != null ? endDate : LocalDateTime.now());
        
        return result;
    }

    /**
     * Get financial metrics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFinancialMetrics(LocalDateTime startDate, LocalDateTime endDate, Long siteId) {
        logger.info("Getting financial metrics from {} to {} for site {}", startDate, endDate, siteId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalSavings", new BigDecimal("2450.75"));
        result.put("demandChargeReduction", new BigDecimal("850.00"));
        result.put("energyCostSavings", new BigDecimal("1200.50"));
        result.put("roi", new BigDecimal("15.75")); // percentage
        result.put("paybackPeriod", new BigDecimal("6.5")); // years
        result.put("periodStart", startDate != null ? startDate : LocalDateTime.now().minusDays(30));
        result.put("periodEnd", endDate != null ? endDate : LocalDateTime.now());
        
        return result;
    }

    /**
     * Create a new report
     */
    public ReportResponse createReport(ReportRequest request) {
        logger.info("Creating report: {}", request.name());
        
        Report report = new Report();
        report.setName(request.name());
        report.setDescription(request.description());
        report.setReportType(request.reportType());
        report.setStatus("PENDING");
        report.setFormat(request.format() != null ? request.format() : "PDF");
        report.setStartDate(request.startDate());
        report.setEndDate(request.endDate());
        report.setSiteId(request.siteId());
        report.setParameters(request.parameters());
        report.setScheduledFrequency(request.scheduledFrequency() != null ? request.scheduledFrequency() : "ONCE");
        report.setCreatedBy("system"); // In real implementation, get from security context
        
        Report savedReport = reportRepository.save(report);
        logger.info("Report created with ID: {}", savedReport.getId());
        
        return mapToReportResponse(savedReport);
    }

    /**
     * Get all reports with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(Pageable pageable, String sortBy, String sortDirection) {
        Page<Report> reportPage = reportRepository.findAll(pageable);
        List<ReportResponse> reportResponses = reportPage.getContent().stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reportResponses, pageable, reportPage.getTotalElements());
    }

    /**
     * Get report by ID
     */
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));
        
        return mapToReportResponse(report);
    }

    /**
     * Delete report
     */
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));
        
        reportRepository.delete(report);
        logger.info("Deleted report: {} (ID: {})", report.getName(), reportId);
    }

    /**
     * Map Report entity to ReportResponse DTO
     */
    private ReportResponse mapToReportResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getName(),
                report.getDescription(),
                report.getReportType(),
                report.getStatus(),
                report.getFormat(),
                report.getFilePath(),
                report.getFileSize(),
                report.getStartDate(),
                report.getEndDate(),
                report.getSiteId(),
                report.getSiteName(), // This would come from a join with Site entity
                report.getParameters(),
                report.getScheduledFrequency(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getCreatedBy()
        );
    }
}