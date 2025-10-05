package com.ecogrid.ems.device.dto.telemetry;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EV Charger Station telemetry data DTO
 */
public class EVChargerTelemetryDTO extends BaseTelemetryDTO {

    // Charging sessions
    @NotNull
    private Integer activeSessions;

    @NotNull
    private Integer totalSessions; // Daily count

    @NotNull
    private BigDecimal powerDelivered; // kW currently delivering

    @NotNull
    private BigDecimal energyDelivered; // kWh (daily)

    // Per-charger data
    private List<ChargerData> chargerData;

    // Financial metrics
    @NotNull
    private BigDecimal revenue; // $ (daily)

    @NotNull
    private BigDecimal avgSessionDuration; // minutes

    @NotNull
    @DecimalMin(value = "0.0", message = "Utilization rate must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Utilization rate must be between 0 and 100")
    private BigDecimal utilizationRate; // %

    // System status
    @NotNull
    private Boolean networkConnectivity;

    @NotNull
    private String paymentSystemStatus; // 'ONLINE', 'OFFLINE'

    @NotNull
    private Integer faults; // Active fault count

    @NotNull
    @DecimalMin(value = "0.0", message = "Uptime must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Uptime must be between 0 and 100")
    private BigDecimal uptime; // % (daily)

    // Constructors
    public EVChargerTelemetryDTO() {}

    public EVChargerTelemetryDTO(Long deviceId, LocalDateTime timestamp, Integer activeSessions,
                                Integer totalSessions, BigDecimal powerDelivered, BigDecimal energyDelivered,
                                BigDecimal revenue, BigDecimal avgSessionDuration, BigDecimal utilizationRate,
                                Boolean networkConnectivity, String paymentSystemStatus, 
                                Integer faults, BigDecimal uptime) {
        super(deviceId, timestamp);
        this.activeSessions = activeSessions;
        this.totalSessions = totalSessions;
        this.powerDelivered = powerDelivered;
        this.energyDelivered = energyDelivered;
        this.revenue = revenue;
        this.avgSessionDuration = avgSessionDuration;
        this.utilizationRate = utilizationRate;
        this.networkConnectivity = networkConnectivity;
        this.paymentSystemStatus = paymentSystemStatus;
        this.faults = faults;
        this.uptime = uptime;
    }

    // Getters and Setters
    public Integer getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(Integer activeSessions) {
        this.activeSessions = activeSessions;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
    }

    public BigDecimal getPowerDelivered() {
        return powerDelivered;
    }

    public void setPowerDelivered(BigDecimal powerDelivered) {
        this.powerDelivered = powerDelivered;
    }

    public BigDecimal getEnergyDelivered() {
        return energyDelivered;
    }

    public void setEnergyDelivered(BigDecimal energyDelivered) {
        this.energyDelivered = energyDelivered;
    }

    public List<ChargerData> getChargerData() {
        return chargerData;
    }

    public void setChargerData(List<ChargerData> chargerData) {
        this.chargerData = chargerData;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getAvgSessionDuration() {
        return avgSessionDuration;
    }

    public void setAvgSessionDuration(BigDecimal avgSessionDuration) {
        this.avgSessionDuration = avgSessionDuration;
    }

    public BigDecimal getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(BigDecimal utilizationRate) {
        this.utilizationRate = utilizationRate;
    }

    public Boolean getNetworkConnectivity() {
        return networkConnectivity;
    }

    public void setNetworkConnectivity(Boolean networkConnectivity) {
        this.networkConnectivity = networkConnectivity;
    }

    public String getPaymentSystemStatus() {
        return paymentSystemStatus;
    }

    public void setPaymentSystemStatus(String paymentSystemStatus) {
        this.paymentSystemStatus = paymentSystemStatus;
    }

    public Integer getFaults() {
        return faults;
    }

    public void setFaults(Integer faults) {
        this.faults = faults;
    }

    public BigDecimal getUptime() {
        return uptime;
    }

    public void setUptime(BigDecimal uptime) {
        this.uptime = uptime;
    }

    /**
     * Inner class for individual charger data
     */
    public static class ChargerData {
        private String chargerId;
        private String status; // 'AVAILABLE', 'OCCUPIED', 'CHARGING', 'FAULT'
        private String sessionId;
        private BigDecimal powerOutput; // kW
        private BigDecimal sessionDuration; // minutes
        private BigDecimal energyDelivered; // kWh (session)
        private String connectorType; // 'CCS', 'CHAdeMO', 'Type2'

        // Constructors
        public ChargerData() {}

        public ChargerData(String chargerId, String status, String sessionId, 
                          BigDecimal powerOutput, BigDecimal sessionDuration,
                          BigDecimal energyDelivered, String connectorType) {
            this.chargerId = chargerId;
            this.status = status;
            this.sessionId = sessionId;
            this.powerOutput = powerOutput;
            this.sessionDuration = sessionDuration;
            this.energyDelivered = energyDelivered;
            this.connectorType = connectorType;
        }

        // Getters and Setters
        public String getChargerId() {
            return chargerId;
        }

        public void setChargerId(String chargerId) {
            this.chargerId = chargerId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public BigDecimal getPowerOutput() {
            return powerOutput;
        }

        public void setPowerOutput(BigDecimal powerOutput) {
            this.powerOutput = powerOutput;
        }

        public BigDecimal getSessionDuration() {
            return sessionDuration;
        }

        public void setSessionDuration(BigDecimal sessionDuration) {
            this.sessionDuration = sessionDuration;
        }

        public BigDecimal getEnergyDelivered() {
            return energyDelivered;
        }

        public void setEnergyDelivered(BigDecimal energyDelivered) {
            this.energyDelivered = energyDelivered;
        }

        public String getConnectorType() {
            return connectorType;
        }

        public void setConnectorType(String connectorType) {
            this.connectorType = connectorType;
        }
    }
}