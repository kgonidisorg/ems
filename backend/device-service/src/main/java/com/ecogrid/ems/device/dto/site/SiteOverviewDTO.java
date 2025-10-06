package com.ecogrid.ems.device.dto.site;

import com.ecogrid.ems.device.entity.Site.SiteStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a site overview with all devices and their telemetry
 */
public class SiteOverviewDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private BigDecimal capacityMw;
    private SiteStatus status;
    private String timezone;
    private String address;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdated;
    
    private List<SiteDeviceDTO> devices;
    private SiteSummaryDTO summary;

    // Constructors
    public SiteOverviewDTO() {}

    public SiteOverviewDTO(Long id, String name, String description, BigDecimal locationLat, 
                          BigDecimal locationLng, BigDecimal capacityMw, SiteStatus status, 
                          String timezone, String address, String contactPerson, String contactEmail, 
                          String contactPhone, LocalDateTime lastUpdated, 
                          List<SiteDeviceDTO> devices, SiteSummaryDTO summary) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.capacityMw = capacityMw;
        this.status = status;
        this.timezone = timezone;
        this.address = address;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.lastUpdated = lastUpdated;
        this.devices = devices;
        this.summary = summary;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(BigDecimal locationLat) {
        this.locationLat = locationLat;
    }

    public BigDecimal getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(BigDecimal locationLng) {
        this.locationLng = locationLng;
    }

    public BigDecimal getCapacityMw() {
        return capacityMw;
    }

    public void setCapacityMw(BigDecimal capacityMw) {
        this.capacityMw = capacityMw;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public void setStatus(SiteStatus status) {
        this.status = status;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<SiteDeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<SiteDeviceDTO> devices) {
        this.devices = devices;
    }

    public SiteSummaryDTO getSummary() {
        return summary;
    }

    public void setSummary(SiteSummaryDTO summary) {
        this.summary = summary;
    }

    /**
     * DTO for site summary statistics
     */
    public static class SiteSummaryDTO {
        
        private int totalDevices;
        private int onlineDevices;
        private int offlineDevices;
        private int alertingDevices;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        private LocalDateTime lastTelemetryUpdate;
        
        private EnergyMetricsDTO energyMetrics;

        // Constructors
        public SiteSummaryDTO() {}

        public SiteSummaryDTO(int totalDevices, int onlineDevices, int offlineDevices, 
                             int alertingDevices, LocalDateTime lastTelemetryUpdate, 
                             EnergyMetricsDTO energyMetrics) {
            this.totalDevices = totalDevices;
            this.onlineDevices = onlineDevices;
            this.offlineDevices = offlineDevices;
            this.alertingDevices = alertingDevices;
            this.lastTelemetryUpdate = lastTelemetryUpdate;
            this.energyMetrics = energyMetrics;
        }

        // Getters and Setters
        public int getTotalDevices() {
            return totalDevices;
        }

        public void setTotalDevices(int totalDevices) {
            this.totalDevices = totalDevices;
        }

        public int getOnlineDevices() {
            return onlineDevices;
        }

        public void setOnlineDevices(int onlineDevices) {
            this.onlineDevices = onlineDevices;
        }

        public int getOfflineDevices() {
            return offlineDevices;
        }

        public void setOfflineDevices(int offlineDevices) {
            this.offlineDevices = offlineDevices;
        }

        public int getAlertingDevices() {
            return alertingDevices;
        }

        public void setAlertingDevices(int alertingDevices) {
            this.alertingDevices = alertingDevices;
        }

        public LocalDateTime getLastTelemetryUpdate() {
            return lastTelemetryUpdate;
        }

        public void setLastTelemetryUpdate(LocalDateTime lastTelemetryUpdate) {
            this.lastTelemetryUpdate = lastTelemetryUpdate;
        }

        public EnergyMetricsDTO getEnergyMetrics() {
            return energyMetrics;
        }

        public void setEnergyMetrics(EnergyMetricsDTO energyMetrics) {
            this.energyMetrics = energyMetrics;
        }
    }

    /**
     * DTO for energy-related metrics
     */
    public static class EnergyMetricsDTO {
        
        private Double totalPowerKw;
        private Double totalEnergyKwh;
        private Double averageVoltage;
        private Double averageCurrent;
        
        // Constructors
        public EnergyMetricsDTO() {}

        public EnergyMetricsDTO(Double totalPowerKw, Double totalEnergyKwh, 
                               Double averageVoltage, Double averageCurrent) {
            this.totalPowerKw = totalPowerKw;
            this.totalEnergyKwh = totalEnergyKwh;
            this.averageVoltage = averageVoltage;
            this.averageCurrent = averageCurrent;
        }

        // Getters and Setters
        public Double getTotalPowerKw() {
            return totalPowerKw;
        }

        public void setTotalPowerKw(Double totalPowerKw) {
            this.totalPowerKw = totalPowerKw;
        }

        public Double getTotalEnergyKwh() {
            return totalEnergyKwh;
        }

        public void setTotalEnergyKwh(Double totalEnergyKwh) {
            this.totalEnergyKwh = totalEnergyKwh;
        }

        public Double getAverageVoltage() {
            return averageVoltage;
        }

        public void setAverageVoltage(Double averageVoltage) {
            this.averageVoltage = averageVoltage;
        }

        public Double getAverageCurrent() {
            return averageCurrent;
        }

        public void setAverageCurrent(Double averageCurrent) {
            this.averageCurrent = averageCurrent;
        }
    }
}