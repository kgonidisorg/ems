package com.ecogrid.ems.device.dto.telemetry;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BMS (Battery Management System) telemetry data DTO
 */
public class BMSTelemetryDTO extends BaseTelemetryDTO {

    // State of Charge metrics
    @NotNull
    @DecimalMin(value = "0.0", message = "SOC must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "SOC must be between 0 and 100")
    private BigDecimal soc; // State of charge (%)

    @NotNull
    private BigDecimal remainingCapacity; // kWh

    @NotNull
    private BigDecimal nominalCapacity; // kWh

    // Power metrics
    @NotNull
    private BigDecimal chargeRate; // kW (+charging, -discharging)

    @NotNull
    private BigDecimal voltage; // V

    @NotNull
    private BigDecimal current; // A

    // Thermal management
    @NotNull
    private BigDecimal temperature; // °C (average)

    private List<BigDecimal> moduleTemperatures; // °C per module

    // Health and efficiency
    @NotNull
    private String healthStatus; // 'EXCELLENT', 'GOOD', 'FAIR', 'POOR'

    @NotNull
    @DecimalMin(value = "0.0", message = "Efficiency must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Efficiency must be between 0 and 100")
    private BigDecimal efficiency; // % round-trip efficiency

    private Long cycleCount; // Total charge cycles

    // Safety and status
    private List<String> alarms; // Active alarms
    private List<String> warnings; // Active warnings
    private LocalDateTime lastMaintenance; // Last maintenance date

    // Constructors
    public BMSTelemetryDTO() {}

    public BMSTelemetryDTO(Long deviceId, LocalDateTime timestamp, BigDecimal soc, 
                          BigDecimal remainingCapacity, BigDecimal nominalCapacity,
                          BigDecimal chargeRate, BigDecimal voltage, BigDecimal current,
                          BigDecimal temperature, String healthStatus, BigDecimal efficiency) {
        super(deviceId, timestamp);
        this.soc = soc;
        this.remainingCapacity = remainingCapacity;
        this.nominalCapacity = nominalCapacity;
        this.chargeRate = chargeRate;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.healthStatus = healthStatus;
        this.efficiency = efficiency;
    }

    // Getters and Setters
    public BigDecimal getSoc() {
        return soc;
    }

    public void setSoc(BigDecimal soc) {
        this.soc = soc;
    }

    public BigDecimal getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(BigDecimal remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public BigDecimal getNominalCapacity() {
        return nominalCapacity;
    }

    public void setNominalCapacity(BigDecimal nominalCapacity) {
        this.nominalCapacity = nominalCapacity;
    }

    public BigDecimal getChargeRate() {
        return chargeRate;
    }

    public void setChargeRate(BigDecimal chargeRate) {
        this.chargeRate = chargeRate;
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

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public List<BigDecimal> getModuleTemperatures() {
        return moduleTemperatures;
    }

    public void setModuleTemperatures(List<BigDecimal> moduleTemperatures) {
        this.moduleTemperatures = moduleTemperatures;
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

    public Long getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(Long cycleCount) {
        this.cycleCount = cycleCount;
    }

    public List<String> getAlarms() {
        return alarms;
    }

    public void setAlarms(List<String> alarms) {
        this.alarms = alarms;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public LocalDateTime getLastMaintenance() {
        return lastMaintenance;
    }

    public void setLastMaintenance(LocalDateTime lastMaintenance) {
        this.lastMaintenance = lastMaintenance;
    }
}