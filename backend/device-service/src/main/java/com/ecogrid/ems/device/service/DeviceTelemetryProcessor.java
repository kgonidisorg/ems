package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.dto.telemetry.BMSTelemetryDTO;
import com.ecogrid.ems.device.dto.telemetry.BaseTelemetryDTO;
import com.ecogrid.ems.device.dto.telemetry.EVChargerTelemetryDTO;
import com.ecogrid.ems.device.dto.telemetry.SolarArrayTelemetryDTO;
import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.DeviceStatusCache;
import com.ecogrid.ems.device.entity.DeviceTelemetry;
import com.ecogrid.ems.device.entity.DeviceType;
import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.DeviceStatusCacheRepository;
import com.ecogrid.ems.device.repository.DeviceTelemetryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for processing device telemetry data from MQTT messages
 */
@Service
@Transactional
public class DeviceTelemetryProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeviceTelemetryProcessor.class);

    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryRepository telemetryRepository;
    private final DeviceStatusCacheRepository statusCacheRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AlertService alertService;

    @Autowired
    public DeviceTelemetryProcessor(DeviceRepository deviceRepository,
                                   DeviceTelemetryRepository telemetryRepository,
                                   DeviceStatusCacheRepository statusCacheRepository,
                                   KafkaTemplate<String, Object> kafkaTemplate,
                                   ObjectMapper objectMapper,
                                   AlertService alertService) {
        this.deviceRepository = deviceRepository;
        this.telemetryRepository = telemetryRepository;
        this.statusCacheRepository = statusCacheRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.alertService = alertService;
    }

    /**
     * Process telemetry message from MQTT
     * Topic format: ecogrid/sites/{siteId}/devices/{deviceId}/telemetry/{dataType}
     */
    public void processTelemetryMessage(String topic, String payload) {
        try {
            logger.debug("Processing telemetry message from topic: {}", topic);
            
            // Parse topic metadata
            TopicMetadata metadata = parseTopicMetadata(topic);
            if (metadata == null) {
                logger.warn("Invalid topic format: {}", topic);
                return;
            }

            // Find device
            Optional<Device> deviceOpt = deviceRepository.findById(metadata.getDeviceId());
            if (deviceOpt.isEmpty()) {
                logger.warn("Device not found: {}", metadata.getDeviceId());
                return;
            }

            Device device = deviceOpt.get();
            if (!isDeviceActive(device)) {
                logger.debug("Device {} is not active, skipping telemetry", device.getId());
                return;
            }

            // Parse telemetry based on device type
            BaseTelemetryDTO telemetryDTO = parseTelemetryByDeviceType(device.getDeviceType(), payload);
            if (telemetryDTO == null) {
                logger.warn("Failed to parse telemetry for device type: {}", device.getDeviceType().getName());
                return;
            }

            // Set device ID and timestamp if not provided
            telemetryDTO.setDeviceId(device.getId());
            if (telemetryDTO.getTimestamp() == null) {
                telemetryDTO.setTimestamp(LocalDateTime.now());
            }

            // Store raw telemetry
            DeviceTelemetry telemetry = createTelemetryEntity(device, telemetryDTO);
            telemetryRepository.save(telemetry);

            // Update device status cache
            updateDeviceStatusCache(device, telemetryDTO);

            // Publish to Kafka for real-time processing
            publishToKafka(device, telemetryDTO);

            // Check for alert conditions
            checkAlertConditions(device, telemetryDTO);

            logger.debug("Successfully processed telemetry for device: {}", device.getId());

        } catch (Exception e) {
            logger.error("Error processing telemetry message from topic: " + topic, e);
        }
    }

    /**
     * Parse MQTT topic to extract metadata
     */
    private TopicMetadata parseTopicMetadata(String topic) {
        try {
            // Topic format: ecogrid/sites/{siteId}/devices/{deviceId}/telemetry/{dataType}
            String[] parts = topic.split("/");
            if (parts.length < 6 || !"ecogrid".equals(parts[0]) || !"sites".equals(parts[1]) 
                || !"devices".equals(parts[3]) || !"telemetry".equals(parts[5])) {
                return null;
            }

            return new TopicMetadata(
                Long.parseLong(parts[2]), // siteId
                Long.parseLong(parts[4]), // deviceId
                parts.length > 6 ? parts[6] : "default" // dataType
            );
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.warn("Error parsing topic: {}", topic, e);
            return null;
        }
    }

    /**
     * Parse telemetry based on device type
     */
    private BaseTelemetryDTO parseTelemetryByDeviceType(DeviceType deviceType, String payload) {
        try {
            switch (deviceType.getName().toUpperCase()) {
                case "BMS":
                case "BATTERY_STORAGE":
                    return objectMapper.readValue(payload, BMSTelemetryDTO.class);
                    
                case "SOLAR_ARRAY":
                case "SOLAR_INVERTER":
                    return objectMapper.readValue(payload, SolarArrayTelemetryDTO.class);
                    
                case "EV_CHARGER":
                    return objectMapper.readValue(payload, EVChargerTelemetryDTO.class);
                    
                default:
                    logger.warn("Unknown device type for telemetry parsing: {}", deviceType.getName());
                    return null;
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing telemetry payload for device type: " + deviceType.getName(), e);
            return null;
        }
    }

    /**
     * Create DeviceTelemetry entity from DTO
     */
    private DeviceTelemetry createTelemetryEntity(Device device, BaseTelemetryDTO telemetryDTO) {
        Map<String, Object> data = convertTelemetryToMap(telemetryDTO);
        
        DeviceTelemetry telemetry = new DeviceTelemetry(device, telemetryDTO.getTimestamp(), data);
        telemetry.setQualityIndicators(telemetryDTO.getQualityIndicators());
        telemetry.setProcessedAt(LocalDateTime.now());
        
        return telemetry;
    }

    /**
     * Convert telemetry DTO to Map for JSON storage
     */
    private Map<String, Object> convertTelemetryToMap(BaseTelemetryDTO telemetryDTO) {
        try {
            String json = objectMapper.writeValueAsString(telemetryDTO);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(json, Map.class);
            return result;
        } catch (JsonProcessingException e) {
            logger.error("Error converting telemetry DTO to map", e);
            return new HashMap<>();
        }
    }

    /**
     * Update device status cache with latest telemetry
     */
    private void updateDeviceStatusCache(Device device, BaseTelemetryDTO telemetryDTO) {
        DeviceStatusCache statusCache = statusCacheRepository.findByDeviceId(device.getId())
            .orElse(new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE));

        statusCache.setLastSeen(telemetryDTO.getTimestamp());
        statusCache.setStatus(DeviceStatusCache.DeviceStatus.ONLINE);
        statusCache.setCurrentData(convertTelemetryToMap(telemetryDTO));
        statusCache.setUpdatedAt(LocalDateTime.now());

        statusCacheRepository.save(statusCache);
    }

    /**
     * Publish telemetry to Kafka for real-time processing
     */
    private void publishToKafka(Device device, BaseTelemetryDTO telemetryDTO) {
        try {
            Map<String, Object> kafkaMessage = new HashMap<>();
            kafkaMessage.put("deviceId", device.getId());
            kafkaMessage.put("siteId", device.getSite().getId());
            kafkaMessage.put("deviceType", device.getDeviceType().getName());
            kafkaMessage.put("telemetry", convertTelemetryToMap(telemetryDTO));
            kafkaMessage.put("timestamp", telemetryDTO.getTimestamp());

            kafkaTemplate.send("device-telemetry", device.getId().toString(), kafkaMessage);
        } catch (Exception e) {
            logger.error("Error publishing telemetry to Kafka for device: " + device.getId(), e);
        }
    }

    /**
     * Check for alert conditions based on device type and thresholds
     */
    private void checkAlertConditions(Device device, BaseTelemetryDTO telemetryDTO) {
        try {
            DeviceType deviceType = device.getDeviceType();
            Map<String, Object> alertThresholds = deviceType.getAlertThresholds();
            
            if (alertThresholds == null || alertThresholds.isEmpty()) {
                return;
            }

            // Check thresholds based on device type
            switch (deviceType.getName().toUpperCase()) {
                case "BMS":
                case "BATTERY_STORAGE":
                    checkBMSAlerts(device, (BMSTelemetryDTO) telemetryDTO, alertThresholds);
                    break;
                    
                case "SOLAR_ARRAY":
                case "SOLAR_INVERTER":
                    checkSolarArrayAlerts(device, (SolarArrayTelemetryDTO) telemetryDTO, alertThresholds);
                    break;
                    
                case "EV_CHARGER":
                    checkEVChargerAlerts(device, (EVChargerTelemetryDTO) telemetryDTO, alertThresholds);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error checking alert conditions for device: " + device.getId(), e);
        }
    }

    /**
     * Check BMS-specific alert conditions
     */
    @SuppressWarnings("unchecked")
    private void checkBMSAlerts(Device device, BMSTelemetryDTO telemetry, Map<String, Object> thresholds) {
        // High temperature alert
        if (thresholds.containsKey("HIGH_TEMPERATURE")) {
            Map<String, Object> threshold = (Map<String, Object>) thresholds.get("HIGH_TEMPERATURE");
            Double tempThreshold = ((Number) threshold.get("threshold")).doubleValue();
            if (telemetry.getTemperature().doubleValue() > tempThreshold) {
                alertService.createAlert(device, "HIGH_TEMPERATURE", (String) threshold.get("severity"),
                    "Battery temperature exceeded threshold: " + telemetry.getTemperature() + "°C",
                    tempThreshold, telemetry.getTemperature().doubleValue());
            }
        }

        // Low SOC alert
        if (thresholds.containsKey("LOW_SOC")) {
            Map<String, Object> threshold = (Map<String, Object>) thresholds.get("LOW_SOC");
            Double socThreshold = ((Number) threshold.get("threshold")).doubleValue();
            if (telemetry.getSoc().doubleValue() < socThreshold) {
                alertService.createAlert(device, "LOW_SOC", (String) threshold.get("severity"),
                    "Battery SOC below threshold: " + telemetry.getSoc() + "%",
                    socThreshold, telemetry.getSoc().doubleValue());
            }
        }
    }

    /**
     * Check Solar Array-specific alert conditions
     */
    @SuppressWarnings("unchecked")
    private void checkSolarArrayAlerts(Device device, SolarArrayTelemetryDTO telemetry, Map<String, Object> thresholds) {
        // High panel temperature alert
        if (thresholds.containsKey("HIGH_PANEL_TEMPERATURE")) {
            Map<String, Object> threshold = (Map<String, Object>) thresholds.get("HIGH_PANEL_TEMPERATURE");
            Double tempThreshold = ((Number) threshold.get("threshold")).doubleValue();
            if (telemetry.getPanelTemperature().doubleValue() > tempThreshold) {
                alertService.createAlert(device, "HIGH_PANEL_TEMPERATURE", (String) threshold.get("severity"),
                    "Panel temperature exceeded threshold: " + telemetry.getPanelTemperature() + "°C",
                    tempThreshold, telemetry.getPanelTemperature().doubleValue());
            }
        }

        // Inverter fault alert
        if ("FAULT".equals(telemetry.getInverterStatus()) && thresholds.containsKey("INVERTER_FAULT")) {
            Map<String, Object> threshold = (Map<String, Object>) thresholds.get("INVERTER_FAULT");
            alertService.createAlert(device, "INVERTER_FAULT", (String) threshold.get("severity"),
                "Inverter fault detected", null, null);
        }
    }

    /**
     * Check EV Charger-specific alert conditions
     */
    @SuppressWarnings("unchecked")
    private void checkEVChargerAlerts(Device device, EVChargerTelemetryDTO telemetry, Map<String, Object> thresholds) {
        // Low utilization alert
        if (thresholds.containsKey("LOW_UTILIZATION")) {
            Map<String, Object> threshold = (Map<String, Object>) thresholds.get("LOW_UTILIZATION");
            Double utilizationThreshold = ((Number) threshold.get("threshold")).doubleValue();
            if (telemetry.getUtilizationRate().doubleValue() < utilizationThreshold) {
                alertService.createAlert(device, "LOW_UTILIZATION", (String) threshold.get("severity"),
                    "Charger utilization below threshold: " + telemetry.getUtilizationRate() + "%",
                    utilizationThreshold, telemetry.getUtilizationRate().doubleValue());
            }
        }

        // Payment system fault
        if ("OFFLINE".equals(telemetry.getPaymentSystemStatus()) && thresholds.containsKey("PAYMENT_SYSTEM_FAULT")) {
            Map<String, Object> threshold = (Map<String, Object>) thresholds.get("PAYMENT_SYSTEM_FAULT");
            alertService.createAlert(device, "PAYMENT_SYSTEM_FAULT", (String) threshold.get("severity"),
                "Payment system offline", null, null);
        }
    }

    /**
     * Check if device is active and should process telemetry
     */
    private boolean isDeviceActive(Device device) {
        return device.getStatus() != Device.DeviceStatus.DECOMMISSIONED 
            && device.getStatus() != Device.DeviceStatus.ERROR;
    }

    /**
     * Inner class for topic metadata
     */
    private static class TopicMetadata {
        private final Long siteId;
        private final Long deviceId;
        private final String dataType;

        public TopicMetadata(Long siteId, Long deviceId, String dataType) {
            this.siteId = siteId;
            this.deviceId = deviceId;
            this.dataType = dataType;
        }

        public Long getSiteId() {
            return siteId;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public String getDataType() {
            return dataType;
        }
    }
}