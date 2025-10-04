package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for handling MQTT messages from IoT devices
 */
@Service
@Transactional
public class MqttMessageService {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageService.class);

    private final DeviceRepository deviceRepository;

    public MqttMessageService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Process telemetry data received from device via MQTT
     */
    public void processTelemetryData(String topic, Map<String, Object> payload) {
        try {
            // Extract device serial number from topic: sites/{siteId}/devices/{serialNumber}/telemetry
            String serialNumber = extractSerialNumberFromTopic(topic);
            
            if (serialNumber != null) {
                // Update device last communication timestamp
                deviceRepository.findBySerialNumber(serialNumber)
                    .ifPresentOrElse(
                        device -> {
                            deviceRepository.updateLastCommunication(device.getId(), LocalDateTime.now());
                            
                            // Update device status to ONLINE if it was OFFLINE
                            if (device.getStatus() == Device.DeviceStatus.OFFLINE) {
                                deviceRepository.updateStatus(device.getId(), Device.DeviceStatus.ONLINE, LocalDateTime.now());
                                logger.info("Device {} is now ONLINE", serialNumber);
                            }
                            
                            logger.debug("Processed telemetry for device: {}", serialNumber);
                        },
                        () -> logger.warn("Received telemetry for unknown device: {}", serialNumber)
                    );
            }
        } catch (Exception e) {
            logger.error("Failed to process telemetry data from topic: {}", topic, e);
        }
    }

    /**
     * Process device status updates
     */
    public void processStatusUpdate(String topic, Map<String, Object> payload) {
        try {
            String serialNumber = extractSerialNumberFromTopic(topic);
            String status = (String) payload.get("status");
            
            if (serialNumber != null && status != null) {
                Device.DeviceStatus deviceStatus = Device.DeviceStatus.valueOf(status.toUpperCase());
                
                deviceRepository.findBySerialNumber(serialNumber)
                    .ifPresentOrElse(
                        device -> {
                            deviceRepository.updateStatus(device.getId(), deviceStatus, LocalDateTime.now());
                            logger.info("Updated status for device {} to {}", serialNumber, deviceStatus);
                        },
                        () -> logger.warn("Received status update for unknown device: {}", serialNumber)
                    );
            }
        } catch (Exception e) {
            logger.error("Failed to process status update from topic: {}", topic, e);
        }
    }

    /**
     * Send command to device via MQTT
     */
    public void sendCommand(String deviceSerialNumber, String command, Map<String, Object> parameters) {
        try {
            // This would integrate with actual MQTT client to send commands
            // For now, we'll just log the command
            logger.info("Sending command '{}' to device '{}' with parameters: {}", 
                command, deviceSerialNumber, parameters);
            
            // In a real implementation, this would:
            // 1. Construct MQTT topic: sites/{siteId}/devices/{serialNumber}/commands
            // 2. Create command payload
            // 3. Publish to MQTT broker
            // 4. Log command history
            
        } catch (Exception e) {
            logger.error("Failed to send command to device: {}", deviceSerialNumber, e);
        }
    }

    /**
     * Extract device serial number from MQTT topic
     */
    private String extractSerialNumberFromTopic(String topic) {
        try {
            // Expected format: sites/{siteId}/devices/{serialNumber}/telemetry or /status
            String[] parts = topic.split("/");
            if (parts.length >= 4 && "sites".equals(parts[0]) && "devices".equals(parts[2])) {
                return parts[3];
            }
        } catch (Exception e) {
            logger.warn("Failed to extract serial number from topic: {}", topic);
        }
        return null;
    }
}