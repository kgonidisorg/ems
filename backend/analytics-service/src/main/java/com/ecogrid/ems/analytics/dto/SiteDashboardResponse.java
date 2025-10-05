package com.ecogrid.ems.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for site dashboard response with EMS-specific data
 */
public class SiteDashboardResponse {

    private SiteInfo siteInfo;
    private BatterySystemMetrics batterySystem;
    private SolarArrayMetrics solarArray;
    private EVChargerMetrics evChargers;
    private AlertSummary alerts;
    private PerformanceMetrics performance;

    // Constructors
    public SiteDashboardResponse() {}

    // Getters and Setters
    public SiteInfo getSiteInfo() {
        return siteInfo;
    }

    public void setSiteInfo(SiteInfo siteInfo) {
        this.siteInfo = siteInfo;
    }

    public BatterySystemMetrics getBatterySystem() {
        return batterySystem;
    }

    public void setBatterySystem(BatterySystemMetrics batterySystem) {
        this.batterySystem = batterySystem;
    }

    public SolarArrayMetrics getSolarArray() {
        return solarArray;
    }

    public void setSolarArray(SolarArrayMetrics solarArray) {
        this.solarArray = solarArray;
    }

    public EVChargerMetrics getEvChargers() {
        return evChargers;
    }

    public void setEvChargers(EVChargerMetrics evChargers) {
        this.evChargers = evChargers;
    }

    public AlertSummary getAlerts() {
        return alerts;
    }

    public void setAlerts(AlertSummary alerts) {
        this.alerts = alerts;
    }

    public PerformanceMetrics getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceMetrics performance) {
        this.performance = performance;
    }

    // Inner classes for nested data structures
    
    public static class SiteInfo {
        private Long id;
        private String name;
        private String location;
        private Coordinates coordinates;
        private BigDecimal capacity; // MW
        private String status;
        private LocalDateTime lastUpdated;

        // Constructors, getters, and setters
        public SiteInfo() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Coordinates getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
        }

        public BigDecimal getCapacity() {
            return capacity;
        }

        public void setCapacity(BigDecimal capacity) {
            this.capacity = capacity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public static class Coordinates {
            private BigDecimal lat;
            private BigDecimal lng;

            public Coordinates() {}

            public Coordinates(BigDecimal lat, BigDecimal lng) {
                this.lat = lat;
                this.lng = lng;
            }

            public BigDecimal getLat() {
                return lat;
            }

            public void setLat(BigDecimal lat) {
                this.lat = lat;
            }

            public BigDecimal getLng() {
                return lng;
            }

            public void setLng(BigDecimal lng) {
                this.lng = lng;
            }
        }
    }

    public static class BatterySystemMetrics {
        private BigDecimal totalCapacity; // kWh
        private BigDecimal avgSOC; // %
        private BigDecimal totalChargeRate; // kW
        private BigDecimal avgTemperature; // °C
        private String healthStatus;
        private BigDecimal efficiency; // %
        private Integer activeDevices;
        private Integer offlineDevices;

        // Constructors, getters, and setters
        public BatterySystemMetrics() {}

        public BigDecimal getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(BigDecimal totalCapacity) {
            this.totalCapacity = totalCapacity;
        }

        public BigDecimal getAvgSOC() {
            return avgSOC;
        }

        public void setAvgSOC(BigDecimal avgSOC) {
            this.avgSOC = avgSOC;
        }

        public BigDecimal getTotalChargeRate() {
            return totalChargeRate;
        }

        public void setTotalChargeRate(BigDecimal totalChargeRate) {
            this.totalChargeRate = totalChargeRate;
        }

        public BigDecimal getAvgTemperature() {
            return avgTemperature;
        }

        public void setAvgTemperature(BigDecimal avgTemperature) {
            this.avgTemperature = avgTemperature;
        }

        public String getHealthStatus() {
            return healthStatus;
        }

        public void setHealthStatus(String healthStatus) {
            this.healthStatus = healthStatus;
        }

        public BigDecimal getEfficiency() {
            return efficiency;
        }

        public void setEfficiency(BigDecimal efficiency) {
            this.efficiency = efficiency;
        }

        public Integer getActiveDevices() {
            return activeDevices;
        }

        public void setActiveDevices(Integer activeDevices) {
            this.activeDevices = activeDevices;
        }

        public Integer getOfflineDevices() {
            return offlineDevices;
        }

        public void setOfflineDevices(Integer offlineDevices) {
            this.offlineDevices = offlineDevices;
        }
    }

    public static class SolarArrayMetrics {
        private BigDecimal totalOutput; // kW
        private BigDecimal dailyYield; // kWh
        private BigDecimal avgEfficiency; // %
        private BigDecimal avgPanelTemp; // °C
        private BigDecimal irradiance; // W/m²
        private BigDecimal performanceRatio; // %
        private Integer activeStrings;
        private Integer faultedStrings;

        // Constructors, getters, and setters
        public SolarArrayMetrics() {}

        public BigDecimal getTotalOutput() {
            return totalOutput;
        }

        public void setTotalOutput(BigDecimal totalOutput) {
            this.totalOutput = totalOutput;
        }

        public BigDecimal getDailyYield() {
            return dailyYield;
        }

        public void setDailyYield(BigDecimal dailyYield) {
            this.dailyYield = dailyYield;
        }

        public BigDecimal getAvgEfficiency() {
            return avgEfficiency;
        }

        public void setAvgEfficiency(BigDecimal avgEfficiency) {
            this.avgEfficiency = avgEfficiency;
        }

        public BigDecimal getAvgPanelTemp() {
            return avgPanelTemp;
        }

        public void setAvgPanelTemp(BigDecimal avgPanelTemp) {
            this.avgPanelTemp = avgPanelTemp;
        }

        public BigDecimal getIrradiance() {
            return irradiance;
        }

        public void setIrradiance(BigDecimal irradiance) {
            this.irradiance = irradiance;
        }

        public BigDecimal getPerformanceRatio() {
            return performanceRatio;
        }

        public void setPerformanceRatio(BigDecimal performanceRatio) {
            this.performanceRatio = performanceRatio;
        }

        public Integer getActiveStrings() {
            return activeStrings;
        }

        public void setActiveStrings(Integer activeStrings) {
            this.activeStrings = activeStrings;
        }

        public Integer getFaultedStrings() {
            return faultedStrings;
        }

        public void setFaultedStrings(Integer faultedStrings) {
            this.faultedStrings = faultedStrings;
        }
    }

    public static class EVChargerMetrics {
        private Integer totalChargers;
        private Integer activeChargers;  
        private Integer activeSessions;
        private BigDecimal totalPowerDelivery; // kW
        private BigDecimal dailyRevenue; // $
        private BigDecimal avgUtilization; // %
        private BigDecimal dailyEnergy; // kWh
        private BigDecimal avgSessionDuration; // minutes

        // Constructors, getters, and setters
        public EVChargerMetrics() {}

        public Integer getTotalChargers() {
            return totalChargers;
        }

        public void setTotalChargers(Integer totalChargers) {
            this.totalChargers = totalChargers;
        }

        public Integer getActiveChargers() {
            return activeChargers;
        }

        public void setActiveChargers(Integer activeChargers) {
            this.activeChargers = activeChargers;
        }

        public Integer getActiveSessions() {
            return activeSessions;
        }

        public void setActiveSessions(Integer activeSessions) {
            this.activeSessions = activeSessions;
        }

        public BigDecimal getTotalPowerDelivery() {
            return totalPowerDelivery;
        }

        public void setTotalPowerDelivery(BigDecimal totalPowerDelivery) {
            this.totalPowerDelivery = totalPowerDelivery;
        }

        public BigDecimal getDailyRevenue() {
            return dailyRevenue;
        }

        public void setDailyRevenue(BigDecimal dailyRevenue) {
            this.dailyRevenue = dailyRevenue;
        }

        public BigDecimal getAvgUtilization() {
            return avgUtilization;
        }

        public void setAvgUtilization(BigDecimal avgUtilization) {
            this.avgUtilization = avgUtilization;
        }

        public BigDecimal getDailyEnergy() {
            return dailyEnergy;
        }

        public void setDailyEnergy(BigDecimal dailyEnergy) {
            this.dailyEnergy = dailyEnergy;
        }

        public BigDecimal getAvgSessionDuration() {
            return avgSessionDuration;
        }

        public void setAvgSessionDuration(BigDecimal avgSessionDuration) {
            this.avgSessionDuration = avgSessionDuration;
        }
    }

    public static class AlertSummary {
        private Integer critical;
        private Integer high;
        private Integer medium;
        private Integer low;
        private Integer totalActive;
        private Integer resolved24h;

        // Constructors, getters, and setters
        public AlertSummary() {}

        public Integer getCritical() {
            return critical;
        }

        public void setCritical(Integer critical) {
            this.critical = critical;
        }

        public Integer getHigh() {
            return high;
        }

        public void setHigh(Integer high) {
            this.high = high;
        }

        public Integer getMedium() {
            return medium;
        }

        public void setMedium(Integer medium) {
            this.medium = medium;
        }

        public Integer getLow() {
            return low;
        }

        public void setLow(Integer low) {
            this.low = low;
        }

        public Integer getTotalActive() {
            return totalActive;
        }

        public void setTotalActive(Integer totalActive) {
            this.totalActive = totalActive;
        }

        public Integer getResolved24h() {
            return resolved24h;
        }

        public void setResolved24h(Integer resolved24h) {
            this.resolved24h = resolved24h;
        }
    }

    public static class PerformanceMetrics {
        private BigDecimal uptime; // % (24h)
        private BigDecimal availability; // % (24h)
        private Integer totalFaults; // (24h)
        private BigDecimal efficiency; // % (site-wide)
        private BigDecimal carbonOffset; // kg CO2 saved (daily)

        // Constructors, getters, and setters
        public PerformanceMetrics() {}

        public BigDecimal getUptime() {
            return uptime;
        }

        public void setUptime(BigDecimal uptime) {
            this.uptime = uptime;
        }

        public BigDecimal getAvailability() {
            return availability;
        }

        public void setAvailability(BigDecimal availability) {
            this.availability = availability;
        }

        public Integer getTotalFaults() {
            return totalFaults;
        }

        public void setTotalFaults(Integer totalFaults) {
            this.totalFaults = totalFaults;
        }

        public BigDecimal getEfficiency() {
            return efficiency;
        }

        public void setEfficiency(BigDecimal efficiency) {
            this.efficiency = efficiency;
        }

        public BigDecimal getCarbonOffset() {
            return carbonOffset;
        }

        public void setCarbonOffset(BigDecimal carbonOffset) {
            this.carbonOffset = carbonOffset;
        }
    }
}