package com.ecogrid.ems.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for Dashboard response
 */
public record DashboardResponse(
        @JsonProperty("totalEnergyConsumed")
        BigDecimal totalEnergyConsumed,

        @JsonProperty("totalEnergyGenerated")
        BigDecimal totalEnergyGenerated,

        @JsonProperty("carbonFootprintReduced")
        BigDecimal carbonFootprintReduced,

        @JsonProperty("costSavings")
        BigDecimal costSavings,

        @JsonProperty("activeSites")
        int activeSites,

        @JsonProperty("activeDevices")
        int activeDevices,

        @JsonProperty("averageEfficiency")
        BigDecimal averageEfficiency,

        @JsonProperty("timeSeriesData")
        List<TimeSeriesDataPoint> timeSeriesData,

        @JsonProperty("siteBreakdown")
        Map<String, BigDecimal> siteBreakdown,

        @JsonProperty("deviceTypeBreakdown")
        Map<String, BigDecimal> deviceTypeBreakdown,

        @JsonProperty("lastUpdated")
        LocalDateTime lastUpdated
) {
    /**
     * Time series data point for charts
     */
    public record TimeSeriesDataPoint(
            @JsonProperty("timestamp")
            LocalDateTime timestamp,

            @JsonProperty("energyConsumed")
            BigDecimal energyConsumed,

            @JsonProperty("energyGenerated")
            BigDecimal energyGenerated,

            @JsonProperty("carbonSaved")
            BigDecimal carbonSaved,

            @JsonProperty("costSavings")
            BigDecimal costSavings
    ) {}
}