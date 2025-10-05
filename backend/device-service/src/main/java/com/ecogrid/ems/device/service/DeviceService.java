package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.dto.DeviceRequest;
import com.ecogrid.ems.device.dto.DeviceResponse;
import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.DeviceType;
import com.ecogrid.ems.device.entity.Site;
import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.DeviceTypeRepository;
import com.ecogrid.ems.device.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Device management operations
 */
@Service
@Transactional
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final SiteRepository siteRepository;
    private final DeviceEventService deviceEventService;

    public DeviceService(DeviceRepository deviceRepository, DeviceTypeRepository deviceTypeRepository, SiteRepository siteRepository, DeviceEventService deviceEventService) {
        this.deviceRepository = deviceRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.siteRepository = siteRepository;
        this.deviceEventService = deviceEventService;
    }

    /**
     * Create a new device
     */
    public DeviceResponse createDevice(DeviceRequest request) {
        // Check if device serial number already exists
        if (deviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new IllegalArgumentException("Device with serial number '" + request.serialNumber() + "' already exists");
        }

        // Verify site exists
        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + request.siteId()));

        Device device = new Device();
        device.setSerialNumber(request.serialNumber());
        device.setName(request.name());
        device.setDescription(request.description());
        device.setModel(request.model());
        device.setManufacturer(request.manufacturer());
        device.setFirmwareVersion(request.firmwareVersion());
        device.setRatedPowerKw(request.ratedPowerKw());
        device.setMqttTopic(request.mqttTopic());
        device.setIpAddress(request.ipAddress());
        device.setMacAddress(request.macAddress());
        device.setInstallationDate(request.installationDate());
        device.setSite(site);

        // Set device type
        if (request.deviceType() != null && !request.deviceType().isEmpty()) {
            DeviceType deviceType = deviceTypeRepository.findByName(request.deviceType().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid device type: " + request.deviceType()));
            device.setDeviceType(deviceType);
        }

        // Set status if provided
        if (request.status() != null && !request.status().isEmpty()) {
            try {
                device.setStatus(Device.DeviceStatus.valueOf(request.status().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid device status: " + request.status());
            }
        }

        // Set configuration and metadata
        if (request.configuration() != null) {
            device.setConfiguration(new HashMap<>(request.configuration()));
        }
        if (request.metadata() != null) {
            device.setMetadata(new HashMap<>(request.metadata()));
        }

        // Generate MQTT topic if not provided
        if (device.getMqttTopic() == null || device.getMqttTopic().isEmpty()) {
            device.setMqttTopic(generateMqttTopic(site.getId(), device.getSerialNumber()));
        }

        Device savedDevice = deviceRepository.save(device);
        logger.info("Created new device: {} (Serial: {})", savedDevice.getName(), savedDevice.getSerialNumber());

        return mapToDeviceResponse(savedDevice);
    }

    /**
     * Get device by ID
     */
    @Transactional(readOnly = true)
    public Optional<DeviceResponse> getDeviceById(Long deviceId) {
        return deviceRepository.findByIdWithSite(deviceId)
                .map(this::mapToDeviceResponse);
    }

    /**
     * Get device by serial number
     */
    @Transactional(readOnly = true)
    public Optional<DeviceResponse> getDeviceBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber)
                .map(this::mapToDeviceResponse);
    }

    /**
     * Get all devices with pagination
     */
    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAllDevices(Pageable pageable) {
        Page<Device> devicePage = deviceRepository.findAll(pageable);
        List<DeviceResponse> deviceResponses = devicePage.getContent().stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(deviceResponses, pageable, devicePage.getTotalElements());
    }

    /**
     * Get devices by site
     */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesBySite(Long siteId) {
        return deviceRepository.findBySiteId(siteId).stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get devices by status
     */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByStatus(Device.DeviceStatus status) {
        return deviceRepository.findByStatus(status).stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get devices by type
     */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByType(DeviceType deviceType) {
        return deviceRepository.findByDeviceType(deviceType).stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get devices by type name
     */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByTypeName(String typeName) {
        DeviceType deviceType = deviceTypeRepository.findByName(typeName.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Invalid device type: " + typeName));
        return getDevicesByType(deviceType);
    }

    /**
     * Search devices by name
     */
    @Transactional(readOnly = true)
    public Page<DeviceResponse> searchDevicesByName(String searchTerm, Pageable pageable) {
        Page<Device> devicePage = deviceRepository.searchByName(searchTerm, pageable);
        List<DeviceResponse> deviceResponses = devicePage.getContent().stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(deviceResponses, pageable, devicePage.getTotalElements());
    }

    /**
     * Update device
     */
    public DeviceResponse updateDevice(Long deviceId, DeviceRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));

        // Check if new serial number conflicts with existing device
        if (!device.getSerialNumber().equals(request.serialNumber()) && 
            deviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new IllegalArgumentException("Device with serial number '" + request.serialNumber() + "' already exists");
        }

        // Verify site exists if being changed
        if (!device.getSite().getId().equals(request.siteId())) {
            Site newSite = siteRepository.findById(request.siteId())
                    .orElseThrow(() -> new IllegalArgumentException("Site not found with ID: " + request.siteId()));
            device.setSite(newSite);
        }

        // Update device fields
        device.setSerialNumber(request.serialNumber());
        device.setName(request.name());
        device.setDescription(request.description());
        device.setModel(request.model());
        device.setManufacturer(request.manufacturer());
        device.setFirmwareVersion(request.firmwareVersion());
        device.setRatedPowerKw(request.ratedPowerKw());
        device.setMqttTopic(request.mqttTopic());
        device.setIpAddress(request.ipAddress());
        device.setMacAddress(request.macAddress());
        device.setInstallationDate(request.installationDate());

        // Update device type if provided
        if (request.deviceType() != null && !request.deviceType().isEmpty()) {
            DeviceType deviceType = deviceTypeRepository.findByName(request.deviceType().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid device type: " + request.deviceType()));
            device.setDeviceType(deviceType);
        }

        // Update status if provided
        if (request.status() != null && !request.status().isEmpty()) {
            try {
                device.setStatus(Device.DeviceStatus.valueOf(request.status().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid device status: " + request.status());
            }
        }

        // Update configuration and metadata
        if (request.configuration() != null) {
            device.setConfiguration(new HashMap<>(request.configuration()));
        }
        if (request.metadata() != null) {
            device.setMetadata(new HashMap<>(request.metadata()));
        }

        Device updatedDevice = deviceRepository.save(device);
        logger.info("Updated device: {} (Serial: {})", updatedDevice.getName(), updatedDevice.getSerialNumber());

        return mapToDeviceResponse(updatedDevice);
    }

    /**
     * Update device status
     */
    public void updateDeviceStatus(Long deviceId, Device.DeviceStatus status) {
        // Get device to capture previous status
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));
        
        String previousStatus = device.getStatus().name();
        String newStatus = status.name();
        
        deviceRepository.updateStatus(deviceId, status, LocalDateTime.now());
        logger.info("Updated device status for device ID {}: {}", deviceId, status);
        
        // Publish status change event
        deviceEventService.publishStatusChangeEvent(
            deviceId, 
            device.getSerialNumber(), 
            device.getSite().getId(), 
            previousStatus, 
            newStatus, 
            "Manual status update"
        );
    }

    /**
     * Update device last communication timestamp
     */
    public void updateLastCommunication(Long deviceId) {
        deviceRepository.updateLastCommunication(deviceId, LocalDateTime.now());
    }

    /**
     * Delete device
     */
    public void deleteDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));

        deviceRepository.delete(device);
        logger.info("Deleted device: {} (Serial: {})", device.getName(), device.getSerialNumber());
    }

    /**
     * Get devices that haven't communicated recently (stale devices)
     */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getStaleDevices(int hoursThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hoursThreshold);
        return deviceRepository.findStaleDevices(threshold).stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get devices due for maintenance
     */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesDueForMaintenance(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        return deviceRepository.findDevicesDueForMaintenance(threshold).stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get device statistics
     */
    @Transactional(readOnly = true)
    public DeviceStatistics getDeviceStatistics() {
        long totalDevices = deviceRepository.count();
        long onlineDevices = deviceRepository.countByStatus(Device.DeviceStatus.ONLINE);
        long offlineDevices = deviceRepository.countByStatus(Device.DeviceStatus.OFFLINE);
        long maintenanceDevices = deviceRepository.countByStatus(Device.DeviceStatus.MAINTENANCE);
        long errorDevices = deviceRepository.countByStatus(Device.DeviceStatus.ERROR);

        Map<String, Long> devicesByType = new HashMap<>();
        List<DeviceType> deviceTypes = deviceTypeRepository.findAll();
        for (DeviceType type : deviceTypes) {
            devicesByType.put(type.getName(), deviceRepository.countByDeviceType(type));
        }

        return new DeviceStatistics(totalDevices, onlineDevices, offlineDevices, maintenanceDevices, errorDevices, devicesByType);
    }

    /**
     * Generate MQTT topic for device
     */
    private String generateMqttTopic(Long siteId, String serialNumber) {
        return String.format("sites/%d/devices/%s/telemetry", siteId, serialNumber);
    }

    /**
     * Map Device entity to DeviceResponse DTO
     */
    private DeviceResponse mapToDeviceResponse(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getSerialNumber(),
                device.getName(),
                device.getDescription(),
                device.getDeviceType().getName(),
                device.getModel(),
                device.getManufacturer(),
                device.getFirmwareVersion(),
                device.getStatus().name(),
                device.getRatedPowerKw(),
                device.getMqttTopic(),
                device.getIpAddress(),
                device.getMacAddress(),
                device.getInstallationDate(),
                device.getLastCommunication(),
                device.getLastMaintenance(),
                device.getSite().getId(),
                device.getSite().getName(),
                new HashMap<>(device.getConfiguration()),
                new HashMap<>(device.getMetadata()),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    /**
     * Device statistics record
     */
    public record DeviceStatistics(
            long totalDevices,
            long onlineDevices,
            long offlineDevices,
            long maintenanceDevices,
            long errorDevices,
            Map<String, Long> devicesByType
    ) {}
}