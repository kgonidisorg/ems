package com.ecogrid.ems.device.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Device entity representing an IoT device in the energy management system
 */
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_serial", columnList = "serialNumber"),
    @Index(name = "idx_device_type", columnList = "deviceType"),
    @Index(name = "idx_device_status", columnList = "status"),
    @Index(name = "idx_device_site", columnList = "site_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true)
    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @Column(nullable = false)
    @NotBlank(message = "Device name is required")
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    @Column(nullable = false)
    @NotBlank(message = "Model is required")
    private String model;

    @Column(nullable = false)
    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @Column(name = "rated_power_kw", precision = 10, scale = 3)
    private BigDecimal ratedPowerKw;

    @Column(name = "mqtt_topic")
    private String mqttTopic;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @Column(name = "last_communication")
    private LocalDateTime lastCommunication;

    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;

    @ElementCollection
    @CollectionTable(name = "device_configuration", joinColumns = @JoinColumn(name = "device_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> configuration = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "device_metadata", joinColumns = @JoinColumn(name = "device_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    @NotNull(message = "Site is required")
    private Site site;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Device() {}

    public Device(String serialNumber, String name, DeviceType deviceType, String model, String manufacturer, Site site) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.deviceType = deviceType;
        this.model = model;
        this.manufacturer = manufacturer;
        this.site = site;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
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

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public BigDecimal getRatedPowerKw() {
        return ratedPowerKw;
    }

    public void setRatedPowerKw(BigDecimal ratedPowerKw) {
        this.ratedPowerKw = ratedPowerKw;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public LocalDateTime getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDateTime installationDate) {
        this.installationDate = installationDate;
    }

    public LocalDateTime getLastCommunication() {
        return lastCommunication;
    }

    public void setLastCommunication(LocalDateTime lastCommunication) {
        this.lastCommunication = lastCommunication;
    }

    public LocalDateTime getLastMaintenance() {
        return lastMaintenance;
    }

    public void setLastMaintenance(LocalDateTime lastMaintenance) {
        this.lastMaintenance = lastMaintenance;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum DeviceType {
        SOLAR_INVERTER,
        BATTERY_STORAGE,
        EV_CHARGER,
        WIND_TURBINE,
        SMART_METER,
        WEATHER_STATION,
        GRID_INTERCONNECT,
        LOAD_CONTROLLER
    }

    public enum DeviceStatus {
        ONLINE,
        OFFLINE,
        MAINTENANCE,
        ERROR,
        COMMISSIONING,
        DECOMMISSIONED
    }
}