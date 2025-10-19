package com.ecogrid.ems.device.controller;

import com.ecogrid.ems.device.dto.DeviceRequest;
import com.ecogrid.ems.device.dto.DeviceResponse;
import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.service.DeviceService;
import com.ecogrid.ems.device.service.MqttConnectionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Device management endpoints
 */
@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    private final DeviceService deviceService;
    private final MqttConnectionService mqttConnectionService;

    public DeviceController(DeviceService deviceService, MqttConnectionService mqttConnectionService) {
        this.deviceService = deviceService;
        this.mqttConnectionService = mqttConnectionService;
    }

    /**
     * Create a new device
     */
    @PostMapping
    public ResponseEntity<?> createDevice(@Valid @RequestBody DeviceRequest request) {
        try {
            DeviceResponse deviceResponse = deviceService.createDevice(request);
            logger.info("Device created successfully: {}", deviceResponse.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(deviceResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("Device creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Device creation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create device"));
        }
    }

    /**
     * Get device by ID
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getDeviceById(@PathVariable Long deviceId) {
        try {
            Optional<DeviceResponse> deviceResponse = deviceService.getDeviceById(deviceId);
            if (deviceResponse.isPresent()) {
                return ResponseEntity.ok(deviceResponse.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get device by ID: {}", deviceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve device"));
        }
    }

    /**
     * Get device by serial number
     */
    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<?> getDeviceBySerialNumber(@PathVariable String serialNumber) {
        try {
            Optional<DeviceResponse> deviceResponse = deviceService.getDeviceBySerialNumber(serialNumber);
            if (deviceResponse.isPresent()) {
                return ResponseEntity.ok(deviceResponse.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get device by serial number: {}", serialNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve device"));
        }
    }

    /**
     * Get all devices with pagination
     */
    @GetMapping
    public ResponseEntity<?> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<DeviceResponse> devicePage;
            if (search != null && !search.trim().isEmpty()) {
                devicePage = deviceService.searchDevicesByName(search.trim(), pageable);
            } else {
                devicePage = deviceService.getAllDevices(pageable);
            }

            return ResponseEntity.ok(Map.of(
                    "content", devicePage.getContent(),
                    "totalElements", devicePage.getTotalElements(),
                    "totalPages", devicePage.getTotalPages(),
                    "currentPage", devicePage.getNumber(),
                    "size", devicePage.getSize(),
                    "hasNext", devicePage.hasNext(),
                    "hasPrevious", devicePage.hasPrevious()
            ));
        } catch (Exception e) {
            logger.error("Failed to get devices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve devices"));
        }
    }

    /**
     * Get devices by site
     */
    @GetMapping("/site/{siteId}")
    public ResponseEntity<?> getDevicesBySite(@PathVariable Long siteId) {
        try {
            List<DeviceResponse> devices = deviceService.getDevicesBySite(siteId);
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            logger.error("Failed to get devices by site: {}", siteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve devices"));
        }
    }

    /**
     * Get devices by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getDevicesByStatus(@PathVariable String status) {
        try {
            Device.DeviceStatus deviceStatus = Device.DeviceStatus.valueOf(status.toUpperCase());
            List<DeviceResponse> devices = deviceService.getDevicesByStatus(deviceStatus);
            return ResponseEntity.ok(devices);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid device status: " + status));
        } catch (Exception e) {
            logger.error("Failed to get devices by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve devices"));
        }
    }

    /**
     * Get devices by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getDevicesByType(@PathVariable String type) {
        try {
            List<DeviceResponse> devices = deviceService.getDevicesByTypeName(type);
            return ResponseEntity.ok(devices);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid device type: " + type));
        } catch (Exception e) {
            logger.error("Failed to get devices by type: {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve devices"));
        }
    }

    /**
     * Update device
     */
    @PutMapping("/{deviceId}")
    public ResponseEntity<?> updateDevice(@PathVariable Long deviceId,
                                         @Valid @RequestBody DeviceRequest request) {
        try {
            DeviceResponse deviceResponse = deviceService.updateDevice(deviceId, request);
            logger.info("Device updated successfully: {}", deviceResponse.name());
            return ResponseEntity.ok(deviceResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("Device update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Device update failed for ID: {}", deviceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update device"));
        }
    }

    /**
     * Update device status
     */
    @PutMapping("/{deviceId}/status")
    public ResponseEntity<?> updateDeviceStatus(@PathVariable Long deviceId,
                                               @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Status is required"));
            }

            Device.DeviceStatus status = Device.DeviceStatus.valueOf(statusStr.toUpperCase());
            deviceService.updateDeviceStatus(deviceId, status);
            
            return ResponseEntity.ok(Map.of("message", "Device status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid device status: " + request.get("status")));
        } catch (Exception e) {
            logger.error("Device status update failed for ID: {}", deviceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update device status"));
        }
    }

    /**
     * Delete device
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long deviceId) {
        try {
            deviceService.deleteDevice(deviceId);
            logger.info("Device deleted successfully: {}", deviceId);
            return ResponseEntity.ok(Map.of("message", "Device deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Device deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Device deletion failed for ID: {}", deviceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete device"));
        }
    }

    /**
     * Get stale devices (haven't communicated recently)
     */
    @GetMapping("/stale")
    public ResponseEntity<?> getStaleDevices(@RequestParam(defaultValue = "24") int hoursThreshold) {
        try {
            List<DeviceResponse> staleDevices = deviceService.getStaleDevices(hoursThreshold);
            return ResponseEntity.ok(Map.of(
                    "devices", staleDevices,
                    "count", staleDevices.size(),
                    "hoursThreshold", hoursThreshold
            ));
        } catch (Exception e) {
            logger.error("Failed to get stale devices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve stale devices"));
        }
    }

    /**
     * Get devices due for maintenance
     */
    @GetMapping("/maintenance-due")
    public ResponseEntity<?> getDevicesDueForMaintenance(@RequestParam(defaultValue = "90") int daysThreshold) {
        try {
            List<DeviceResponse> maintenanceDevices = deviceService.getDevicesDueForMaintenance(daysThreshold);
            return ResponseEntity.ok(Map.of(
                    "devices", maintenanceDevices,
                    "count", maintenanceDevices.size(),
                    "daysThreshold", daysThreshold
            ));
        } catch (Exception e) {
            logger.error("Failed to get devices due for maintenance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve maintenance devices"));
        }
    }

    /**
     * Get device statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getDeviceStatistics() {
        try {
            DeviceService.DeviceStatistics statistics = deviceService.getDeviceStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get device statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve device statistics"));
        }
    }

    /**
     * Health check endpoint for devices endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "device-service-devices",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * MQTT connection status endpoint for debugging
     */
    @GetMapping("/mqtt/status")
    public ResponseEntity<?> mqttStatus() {
        return ResponseEntity.ok(Map.of(
                "connected", mqttConnectionService.isConnected(),
                "status", mqttConnectionService.getConnectionStatus(),
                "timestamp", System.currentTimeMillis()
        ));
    }
}