package com.ecogrid.ems.device.dto.site;

import com.ecogrid.ems.device.entity.Device.DeviceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO representing a device with its current status and latest telemetry data
 */
public class SiteDeviceDTO {

    private Long id;
    private String serialNumber;
    private String name;
    private String deviceType;
    private String model;
    private String manufacturer;
    private DeviceStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastCommunication;
    
    private LatestTelemetryDTO latestTelemetry;

    // Constructors
    public SiteDeviceDTO() {}

    public SiteDeviceDTO(Long id, String serialNumber, String name, String deviceType, 
                        String model, String manufacturer, DeviceStatus status, 
                        LocalDateTime lastCommunication, LatestTelemetryDTO latestTelemetry) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.name = name;
        this.deviceType = deviceType;
        this.model = model;
        this.manufacturer = manufacturer;
        this.status = status;
        this.lastCommunication = lastCommunication;
        this.latestTelemetry = latestTelemetry;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastCommunication() {
        return lastCommunication;
    }

    public void setLastCommunication(LocalDateTime lastCommunication) {
        this.lastCommunication = lastCommunication;
    }

    public LatestTelemetryDTO getLatestTelemetry() {
        return latestTelemetry;
    }

    public void setLatestTelemetry(LatestTelemetryDTO latestTelemetry) {
        this.latestTelemetry = latestTelemetry;
    }

    /**
     * DTO for latest telemetry data
     */
    public static class LatestTelemetryDTO {
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        private LocalDateTime timestamp;
        
        private String telemetryType;
        private Map<String, Object> data;

        // Constructors
        public LatestTelemetryDTO() {}

        public LatestTelemetryDTO(LocalDateTime timestamp, String telemetryType, Map<String, Object> data) {
            this.timestamp = timestamp;
            this.telemetryType = telemetryType;
            this.data = data;
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getTelemetryType() {
            return telemetryType;
        }

        public void setTelemetryType(String telemetryType) {
            this.telemetryType = telemetryType;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}