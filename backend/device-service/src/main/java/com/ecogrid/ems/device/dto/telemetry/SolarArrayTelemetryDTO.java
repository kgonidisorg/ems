package com.ecogrid.ems.device.dto.telemetry;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Solar Array telemetry data DTO
 */
public class SolarArrayTelemetryDTO extends BaseTelemetryDTO {

    // Power generation
    @NotNull
    private BigDecimal currentOutput; // kW

    @NotNull
    private BigDecimal energyYield; // kWh (daily)

    private BigDecimal energyYieldTotal; // kWh (lifetime)

    // Environmental conditions
    @NotNull
    private BigDecimal panelTemperature; // °C

    @NotNull
    private BigDecimal irradiance; // W/m²

    private BigDecimal ambientTemperature; // °C
    private BigDecimal windSpeed; // m/s

    // System performance
    @NotNull
    @DecimalMin(value = "0.0", message = "Efficiency must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Efficiency must be between 0 and 100")
    private BigDecimal inverterEfficiency; // %

    @NotNull
    @DecimalMin(value = "0.0", message = "Efficiency must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Efficiency must be between 0 and 100")
    private BigDecimal systemEfficiency; // %

    @NotNull
    @DecimalMin(value = "0.0", message = "Performance ratio must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Performance ratio must be between 0 and 100")
    private BigDecimal performanceRatio; // %

    // String-level data
    private List<StringData> stringData;

    // Status and maintenance
    @NotNull
    private String inverterStatus; // 'ONLINE', 'OFFLINE', 'FAULT'

    private List<String> alarms;
    private LocalDateTime lastCleaning; // Last cleaning date

    // Constructors
    public SolarArrayTelemetryDTO() {}

    public SolarArrayTelemetryDTO(Long deviceId, LocalDateTime timestamp, BigDecimal currentOutput,
                                 BigDecimal energyYield, BigDecimal panelTemperature, BigDecimal irradiance,
                                 BigDecimal inverterEfficiency, BigDecimal systemEfficiency, 
                                 BigDecimal performanceRatio, String inverterStatus) {
        super(deviceId, timestamp);
        this.currentOutput = currentOutput;
        this.energyYield = energyYield;
        this.panelTemperature = panelTemperature;
        this.irradiance = irradiance;
        this.inverterEfficiency = inverterEfficiency;
        this.systemEfficiency = systemEfficiency;
        this.performanceRatio = performanceRatio;
        this.inverterStatus = inverterStatus;
    }

    // Getters and Setters
    public BigDecimal getCurrentOutput() {
        return currentOutput;
    }

    public void setCurrentOutput(BigDecimal currentOutput) {
        this.currentOutput = currentOutput;
    }

    public BigDecimal getEnergyYield() {
        return energyYield;
    }

    public void setEnergyYield(BigDecimal energyYield) {
        this.energyYield = energyYield;
    }

    public BigDecimal getEnergyYieldTotal() {
        return energyYieldTotal;
    }

    public void setEnergyYieldTotal(BigDecimal energyYieldTotal) {
        this.energyYieldTotal = energyYieldTotal;
    }

    public BigDecimal getPanelTemperature() {
        return panelTemperature;
    }

    public void setPanelTemperature(BigDecimal panelTemperature) {
        this.panelTemperature = panelTemperature;
    }

    public BigDecimal getIrradiance() {
        return irradiance;
    }

    public void setIrradiance(BigDecimal irradiance) {
        this.irradiance = irradiance;
    }

    public BigDecimal getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(BigDecimal ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public BigDecimal getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(BigDecimal windSpeed) {
        this.windSpeed = windSpeed;
    }

    public BigDecimal getInverterEfficiency() {
        return inverterEfficiency;
    }

    public void setInverterEfficiency(BigDecimal inverterEfficiency) {
        this.inverterEfficiency = inverterEfficiency;
    }

    public BigDecimal getSystemEfficiency() {
        return systemEfficiency;
    }

    public void setSystemEfficiency(BigDecimal systemEfficiency) {
        this.systemEfficiency = systemEfficiency;
    }

    public BigDecimal getPerformanceRatio() {
        return performanceRatio;
    }

    public void setPerformanceRatio(BigDecimal performanceRatio) {
        this.performanceRatio = performanceRatio;
    }

    public List<StringData> getStringData() {
        return stringData;
    }

    public void setStringData(List<StringData> stringData) {
        this.stringData = stringData;
    }

    public String getInverterStatus() {
        return inverterStatus;
    }

    public void setInverterStatus(String inverterStatus) {
        this.inverterStatus = inverterStatus;
    }

    public List<String> getAlarms() {
        return alarms;
    }

    public void setAlarms(List<String> alarms) {
        this.alarms = alarms;
    }

    public LocalDateTime getLastCleaning() {
        return lastCleaning;
    }

    public void setLastCleaning(LocalDateTime lastCleaning) {
        this.lastCleaning = lastCleaning;
    }

    /**
     * Inner class for string-level data
     */
    public static class StringData {
        private String stringId;
        private BigDecimal voltage; // V
        private BigDecimal current; // A
        private BigDecimal power; // kW
        private BigDecimal temperature; // °C

        // Constructors
        public StringData() {}

        public StringData(String stringId, BigDecimal voltage, BigDecimal current, 
                         BigDecimal power, BigDecimal temperature) {
            this.stringId = stringId;
            this.voltage = voltage;
            this.current = current;
            this.power = power;
            this.temperature = temperature;
        }

        // Getters and Setters
        public String getStringId() {
            return stringId;
        }

        public void setStringId(String stringId) {
            this.stringId = stringId;
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

        public BigDecimal getPower() {
            return power;
        }

        public void setPower(BigDecimal power) {
            this.power = power;
        }

        public BigDecimal getTemperature() {
            return temperature;
        }

        public void setTemperature(BigDecimal temperature) {
            this.temperature = temperature;
        }
    }
}