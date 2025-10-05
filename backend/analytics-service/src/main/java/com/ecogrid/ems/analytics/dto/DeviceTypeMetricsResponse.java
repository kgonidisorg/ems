package com.ecogrid.ems.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for device type specific metrics with time-series data
 */
public class DeviceTypeMetricsResponse {

    private String deviceType;
    private String siteId;
    private Integer totalDevices;
    private Integer activeDevices;
    private Integer faultedDevices;
    private LocalDateTime lastUpdated;
    private TimeSeriesData timeSeries;
    private AggregatedMetrics aggregated;
    private List<DeviceStatus> deviceStatuses;

    // Constructors
    public DeviceTypeMetricsResponse() {}

    // Getters and Setters
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Integer getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(Integer totalDevices) {
        this.totalDevices = totalDevices;
    }

    public Integer getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(Integer activeDevices) {
        this.activeDevices = activeDevices;
    }

    public Integer getFaultedDevices() {
        return faultedDevices;
    }

    public void setFaultedDevices(Integer faultedDevices) {
        this.faultedDevices = faultedDevices;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public TimeSeriesData getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(TimeSeriesData timeSeries) {
        this.timeSeries = timeSeries;
    }

    public AggregatedMetrics getAggregated() {
        return aggregated;
    }

    public void setAggregated(AggregatedMetrics aggregated) {
        this.aggregated = aggregated;
    }

    public List<DeviceStatus> getDeviceStatuses() {
        return deviceStatuses;
    }

    public void setDeviceStatuses(List<DeviceStatus> deviceStatuses) {
        this.deviceStatuses = deviceStatuses;
    }

    // Inner classes
    
    public static class TimeSeriesData {
        private List<DataPoint> dataPoints;
        private String interval; // "5m", "15m", "1h", "1d"
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        // Constructors, getters, and setters
        public TimeSeriesData() {}

        public List<DataPoint> getDataPoints() {
            return dataPoints;
        }

        public void setDataPoints(List<DataPoint> dataPoints) {
            this.dataPoints = dataPoints;
        }

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public static class DataPoint {
            private LocalDateTime timestamp;
            private MetricValues values;

            public DataPoint() {}

            public DataPoint(LocalDateTime timestamp, MetricValues values) {
                this.timestamp = timestamp;
                this.values = values;
            }

            public LocalDateTime getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
            }

            public MetricValues getValues() {
                return values;
            }

            public void setValues(MetricValues values) {
                this.values = values;
            }
        }

        public static class MetricValues {
            // Common metrics for all device types
            private BigDecimal power; // kW
            private BigDecimal energy; // kWh
            private BigDecimal efficiency; // %
            private BigDecimal temperature; // °C
            
            // BMS specific
            private BigDecimal soc; // %
            private BigDecimal voltage; // V
            private BigDecimal current; // A
            
            // Solar specific
            private BigDecimal irradiance; // W/m²
            private BigDecimal performanceRatio; // %
            
            // EV Charger specific
            private Integer activeSessions;
            private BigDecimal utilization; // %
            private BigDecimal revenue; // $

            // Constructors, getters, and setters
            public MetricValues() {}

            public BigDecimal getPower() {
                return power;
            }

            public void setPower(BigDecimal power) {
                this.power = power;
            }

            public BigDecimal getEnergy() {
                return energy;
            }

            public void setEnergy(BigDecimal energy) {
                this.energy = energy;
            }

            public BigDecimal getEfficiency() {
                return efficiency;
            }

            public void setEfficiency(BigDecimal efficiency) {
                this.efficiency = efficiency;
            }

            public BigDecimal getTemperature() {
                return temperature;
            }

            public void setTemperature(BigDecimal temperature) {
                this.temperature = temperature;
            }

            public BigDecimal getSoc() {
                return soc;
            }

            public void setSoc(BigDecimal soc) {
                this.soc = soc;
            }

            public BigDecimal getVoltage() {
                return voltage;
            }

            public void setVoltage(BigDecimal voltage) {
                this.voltage = voltage;
            }

            public BigDecimal getCurrent() {
                return current;
            }

            public void setCurrent(BigDecimal current) {
                this.current = current;
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

            public Integer getActiveSessions() {
                return activeSessions;
            }

            public void setActiveSessions(Integer activeSessions) {
                this.activeSessions = activeSessions;
            }

            public BigDecimal getUtilization() {
                return utilization;
            }

            public void setUtilization(BigDecimal utilization) {
                this.utilization = utilization;
            }

            public BigDecimal getRevenue() {
                return revenue;
            }

            public void setRevenue(BigDecimal revenue) {
                this.revenue = revenue;
            }
        }
    }

    public static class AggregatedMetrics {
        private BigDecimal totalPower; // kW
        private BigDecimal totalEnergy; // kWh (daily)
        private BigDecimal avgEfficiency; // %
        private BigDecimal avgTemperature; // °C
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private BigDecimal stdDeviation;
        
        // Device type specific aggregations
        private BigDecimal avgSOC; // BMS
        private BigDecimal totalCapacity; // BMS - kWh
        private BigDecimal avgIrradiance; // Solar - W/m²
        private BigDecimal performanceRatio; // Solar - %
        private Integer totalSessions; // EV - count
        private BigDecimal totalRevenue; // EV - $

        // Constructors, getters, and setters
        public AggregatedMetrics() {}

        public BigDecimal getTotalPower() {
            return totalPower;
        }

        public void setTotalPower(BigDecimal totalPower) {
            this.totalPower = totalPower;
        }

        public BigDecimal getTotalEnergy() {
            return totalEnergy;
        }

        public void setTotalEnergy(BigDecimal totalEnergy) {
            this.totalEnergy = totalEnergy;
        }

        public BigDecimal getAvgEfficiency() {
            return avgEfficiency;
        }

        public void setAvgEfficiency(BigDecimal avgEfficiency) {
            this.avgEfficiency = avgEfficiency;
        }

        public BigDecimal getAvgTemperature() {
            return avgTemperature;
        }

        public void setAvgTemperature(BigDecimal avgTemperature) {
            this.avgTemperature = avgTemperature;
        }

        public BigDecimal getMinValue() {
            return minValue;
        }

        public void setMinValue(BigDecimal minValue) {
            this.minValue = minValue;
        }

        public BigDecimal getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(BigDecimal maxValue) {
            this.maxValue = maxValue;
        }

        public BigDecimal getStdDeviation() {
            return stdDeviation;
        }

        public void setStdDeviation(BigDecimal stdDeviation) {
            this.stdDeviation = stdDeviation;
        }

        public BigDecimal getAvgSOC() {
            return avgSOC;
        }

        public void setAvgSOC(BigDecimal avgSOC) {
            this.avgSOC = avgSOC;
        }

        public BigDecimal getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(BigDecimal totalCapacity) {
            this.totalCapacity = totalCapacity;
        }

        public BigDecimal getAvgIrradiance() {
            return avgIrradiance;
        }

        public void setAvgIrradiance(BigDecimal avgIrradiance) {
            this.avgIrradiance = avgIrradiance;
        }

        public BigDecimal getPerformanceRatio() {
            return performanceRatio;
        }

        public void setPerformanceRatio(BigDecimal performanceRatio) {
            this.performanceRatio = performanceRatio;
        }

        public Integer getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(Integer totalSessions) {
            this.totalSessions = totalSessions;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
    }

    public static class DeviceStatus {
        private Long deviceId;
        private String deviceName;
        private String status; // "ACTIVE", "OFFLINE", "FAULT", "MAINTENANCE"
        private LocalDateTime lastSeen;
        private String lastError;
        private BigDecimal currentValue;
        private String unit;

        // Constructors, getters, and setters
        public DeviceStatus() {}

        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getLastSeen() {
            return lastSeen;
        }

        public void setLastSeen(LocalDateTime lastSeen) {
            this.lastSeen = lastSeen;
        }

        public String getLastError() {
            return lastError;
        }

        public void setLastError(String lastError) {
            this.lastError = lastError;
        }

        public BigDecimal getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(BigDecimal currentValue) {
            this.currentValue = currentValue;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}