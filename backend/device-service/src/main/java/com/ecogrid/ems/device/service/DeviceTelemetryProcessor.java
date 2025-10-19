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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for processing device telemetry data from MQTT messages
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DeviceTelemetryProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeviceTelemetryProcessor.class);

    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryRepository telemetryRepository;
    private final DeviceStatusCacheRepository statusCacheRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AlertService alertService;
    private final TransactionTemplate transactionTemplate;
    private final DeviceTelemetryCacheService telemetryCacheService;

    @Autowired
    public DeviceTelemetryProcessor(DeviceRepository deviceRepository,
                                   DeviceTelemetryRepository telemetryRepository,
                                   DeviceStatusCacheRepository statusCacheRepository,
                                   KafkaTemplate<String, Object> kafkaTemplate,
                                   ObjectMapper objectMapper,
                                   AlertService alertService,
                                   TransactionTemplate transactionTemplate,
                                   DeviceTelemetryCacheService telemetryCacheService) {
        this.deviceRepository = deviceRepository;
        this.telemetryRepository = telemetryRepository;
        this.statusCacheRepository = statusCacheRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.alertService = alertService;
        this.transactionTemplate = transactionTemplate;
        this.telemetryCacheService = telemetryCacheService;
    }

    /**
     * Process telemetry message from MQTT
     * Topic format: ecogrid/sites/{siteId}/devices/{deviceId}/telemetry/{dataType}
     */
    public void processTelemetryMessage(String topic, String payload) {
        try {
            logger.info("🔄 Processing telemetry message from topic: {}", topic);
            logger.info("📝 Payload: {}", payload);
            
            // Parse topic metadata
            TopicMetadata metadata = parseTopicMetadata(topic);
            if (metadata == null) {
                logger.warn("❌ Invalid topic format: {}", topic);
                return;
            }
            logger.info("✅ Parsed topic - Site: {}, Device Serial: {}, Data Type: {}", 
                metadata.getSiteId(), metadata.getDeviceSerial(), metadata.getDataType());

            // Find device by serial number
            Optional<Device> deviceOpt = deviceRepository.findBySerialNumber(metadata.getDeviceSerial());
            if (deviceOpt.isEmpty()) {
                logger.warn("❌ Device not found with serial number: {}", metadata.getDeviceSerial());
                return;
            }

            Device device = deviceOpt.get();
            logger.info("✅ Found device: {} (Type: {})", device.getSerialNumber(), device.getDeviceType().getName());
            
            if (!isDeviceActive(device)) {
                logger.warn("⚠️ Device {} is not active, skipping telemetry", device.getSerialNumber());
                return;
            }

            // Parse telemetry based on device type
            logger.info("🔍 Attempting to parse telemetry for device type: {}", device.getDeviceType().getName());
            BaseTelemetryDTO telemetryDTO = parseTelemetryByDeviceType(device.getDeviceType(), payload);
            if (telemetryDTO == null) {
                logger.warn("❌ Failed to parse telemetry for device type: {}", device.getDeviceType().getName());
                return;
            }
            logger.info("✅ Successfully parsed telemetry DTO");

            // Set device ID and timestamp if not provided
            // telemetryDTO.setDeviceId(device.getId());
            if (telemetryDTO.getTimestamp() == null) {
                telemetryDTO.setTimestamp(LocalDateTime.now());
            }

            // Store raw telemetry - use raw payload for generic telemetry to preserve sensor data
            logger.info("💾 Creating and saving telemetry entity...");
            DeviceTelemetry telemetry;
            
            // Check if this is a generic telemetry DTO (anonymous class) - if so, use raw payload
            if (telemetryDTO.getClass().getName().contains("$")) {
                logger.info("🔧 Using raw payload for generic telemetry to preserve sensor data");
                telemetry = createTelemetryEntityFromPayload(device, payload, telemetryDTO.getTimestamp());
            } else {
                logger.info("🎯 Using typed telemetry DTO");
                telemetry = createTelemetryEntity(device, telemetryDTO);
            }
            logger.info("📊 Telemetry entity created with device ID: {} and timestamp: {}", telemetry.getDevice().getId(), telemetry.getTimestamp());
            DeviceTelemetry savedTelemetry = telemetryRepository.save(telemetry);
            logger.info("✅ Saved telemetry with ID: {} for device: {} at timestamp: {}", 
                savedTelemetry.getId(), savedTelemetry.getDevice().getSerialNumber(), savedTelemetry.getTimestamp());
            logger.info("💾 Saved telemetry data: {}", savedTelemetry.getData());
            logger.info("💾 Saved telemetry voltage: {}, current: {}", 
                savedTelemetry.getData() != null ? savedTelemetry.getData().get("voltage") : "NULL_DATA",
                savedTelemetry.getData() != null ? savedTelemetry.getData().get("current") : "NULL_DATA");
            
            // Cache the latest telemetry data for quick access
            logger.info("🗂️ Caching latest telemetry for device: {}", device.getSerialNumber());
            telemetryCacheService.cacheLatestTelemetryFromEntity(device.getId(), savedTelemetry);
            
            // Verify telemetry was saved by retrieving records for this device  
            List<DeviceTelemetry> deviceTelemetryRecords = telemetryRepository.findByDeviceSerialNumberOrderByTimestampDesc(device.getSerialNumber());
            logger.info("📊 Total telemetry records for device {}: {}", device.getSerialNumber(), deviceTelemetryRecords.size());
            
            // Debug the retrieved records
            if (!deviceTelemetryRecords.isEmpty()) {
                DeviceTelemetry firstRecord = deviceTelemetryRecords.get(0);
                logger.info("🔍 First retrieved record ID: {}, data: {}", firstRecord.getId(), firstRecord.getData());
                if (firstRecord.getData() != null) {
                    logger.info("🔍 First record voltage: {}, current: {}", 
                        firstRecord.getData().get("voltage"), firstRecord.getData().get("current"));
                }
            }

            // Publish to Kafka for real-time processing (should not affect transaction)
            publishToKafka(device, telemetryDTO);

            // Update device status cache in separate transaction (fixed @MapsId issue)
            updateDeviceStatusCache(device, telemetryDTO);

            // Check for alert conditions in separate transaction
            checkAlertConditionsInSeparateTransaction(device, telemetryDTO);

            logger.info("🎉 Successfully processed telemetry for device: {}", device.getSerialNumber());

        } catch (Exception e) {
            logger.error("Error processing telemetry message from topic: " + topic, e);
        }
    }

    /**
     * Parse MQTT topic to extract metadata
     */
    private TopicMetadata parseTopicMetadata(String topic) {
        try {
            // Topic format: ecogrid/site{N}/{deviceType}/{deviceId}
            String[] parts = topic.split("/");
            if (parts.length < 4 || !"ecogrid".equals(parts[0])) {
                logger.warn("Invalid topic format: {}, expected: ecogrid/site{{N}}/{{deviceType}}/{{deviceId}}", topic);
                return null;
            }

            // Extract site number from "site1", "site2", etc.
            String siteStr = parts[1];
            if (!siteStr.startsWith("site")) {
                logger.warn("Invalid site format in topic: {}, expected: site{{N}}", siteStr);
                return null;
            }
            
            Long siteId = Long.parseLong(siteStr.substring(4)); // Remove "site" prefix
            String deviceType = parts[2]; // bms, solar, evcharger
            String deviceId = parts[3]; // 001, 002, etc.
            
            // Create device serial number based on pattern used in seeding: {TYPE}-SITE{N}-{ID}
            String deviceSerial = deviceType.toUpperCase() + "-SITE" + siteId + "-" + deviceId;
            
            return new TopicMetadata(
                siteId,
                deviceSerial,
                "telemetry" // default data type
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
            logger.info("🔍 Parsing telemetry for device type: {}", deviceType.getName());
            
            switch (deviceType.getName().toUpperCase()) {
                case "BMS":
                case "BATTERY_STORAGE":
                    try {
                        return objectMapper.readValue(payload, BMSTelemetryDTO.class);
                    } catch (JsonProcessingException e) {
                        logger.warn("⚠️ Failed to parse as BMSTelemetryDTO, trying generic telemetry: {}", e.getMessage());
                        return parseGenericTelemetry(payload);
                    }
                    
                case "SOLAR ARRAY":
                case "SOLAR_ARRAY":
                case "SOLAR_INVERTER":
                    try {
                        return objectMapper.readValue(payload, SolarArrayTelemetryDTO.class);
                    } catch (JsonProcessingException e) {
                        logger.warn("⚠️ Failed to parse as SolarArrayTelemetryDTO, trying generic telemetry: {}", e.getMessage());
                        return parseGenericTelemetry(payload);
                    }
                    
                case "EV CHARGER":
                case "EV_CHARGER":
                    try {
                        return objectMapper.readValue(payload, EVChargerTelemetryDTO.class);
                    } catch (JsonProcessingException e) {
                        logger.warn("⚠️ Failed to parse as EVChargerTelemetryDTO, trying generic telemetry: {}", e.getMessage());
                        return parseGenericTelemetry(payload);
                    }
                    
                default:
                    logger.info("ℹ️ Unknown device type, parsing as generic telemetry: {}", deviceType.getName());
                    return parseGenericTelemetry(payload);
            }
        } catch (JsonProcessingException e) {
            logger.error("❌ Error parsing telemetry payload for device type: " + deviceType.getName(), e);
            return null;
        }
    }

    /**
     * Parse generic telemetry data (fallback for test data or unknown formats)
     */
    private BaseTelemetryDTO parseGenericTelemetry(String payload) throws JsonProcessingException {
        logger.info("🔧 Parsing as generic telemetry...");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        
        // Create a basic telemetry DTO that preserves all sensor data
        BaseTelemetryDTO dto = new BaseTelemetryDTO() {
            private Long deviceId;
            private LocalDateTime timestamp;
            private Map<String, Object> qualityIndicators = new HashMap<>();
            
            @Override
            public Long getDeviceId() { return deviceId; }
            @Override
            public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
            @Override
            public LocalDateTime getTimestamp() { return timestamp; }
            @Override
            public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
            @Override
            public Map<String, Object> getQualityIndicators() { return qualityIndicators; }
            @Override
            public void setQualityIndicators(Map<String, Object> qualityIndicators) { this.qualityIndicators = qualityIndicators; }
        };
        
        // Try to extract timestamp if present
        if (data.containsKey("timestamp")) {
            try {
                String timestampStr = data.get("timestamp").toString();
                dto.setTimestamp(LocalDateTime.parse(timestampStr));
            } catch (Exception e) {
                logger.debug("Could not parse timestamp from payload, using current time");
                dto.setTimestamp(LocalDateTime.now());
            }
        } else {
            dto.setTimestamp(LocalDateTime.now());
        }
        
        logger.info("✅ Successfully created generic telemetry DTO with {} data fields", data.size());
        return dto;
    }

    /**
     * Create DeviceTelemetry entity from DTO
     */
    private DeviceTelemetry createTelemetryEntity(Device device, BaseTelemetryDTO telemetryDTO) {
        logger.info("📊 Creating telemetry entity from DTO: {}", telemetryDTO.getClass().getSimpleName());
        logger.info("📊 Telemetry DTO data: {}", telemetryDTO);
        
        Map<String, Object> data = convertTelemetryToMap(telemetryDTO);
        logger.info("📊 Converted telemetry data map: {}", data);
        logger.info("📊 Data map voltage: {}, current: {}", data.get("voltage"), data.get("current"));
        
        DeviceTelemetry telemetry = new DeviceTelemetry(device, telemetryDTO.getTimestamp(), data);
        telemetry.setQualityIndicators(telemetryDTO.getQualityIndicators());
        telemetry.setProcessedAt(LocalDateTime.now());
        
        logger.info("📊 Created telemetry entity with data: {}", telemetry.getData());
        return telemetry;
    }
    
    /**
     * Create DeviceTelemetry entity directly from raw payload (for generic telemetry)
     */
    private DeviceTelemetry createTelemetryEntityFromPayload(Device device, String payload, LocalDateTime timestamp) {
        try {
            logger.info("📊 Creating telemetry entity directly from payload");
            @SuppressWarnings("unchecked") 
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            logger.info("📊 Raw payload data map: {}", data);
            logger.info("📊 Raw data voltage: {}, current: {}, soc: {}", data.get("voltage"), data.get("current"), data.get("soc"));
            
            DeviceTelemetry telemetry = new DeviceTelemetry(device, timestamp, data);
            telemetry.setProcessedAt(LocalDateTime.now());
            
            logger.info("📊 Created telemetry entity with raw data: {}", telemetry.getData());
            return telemetry;
        } catch (JsonProcessingException e) {
            logger.error("❌ Error creating telemetry entity from payload", e);
            // Fallback to empty data
            return new DeviceTelemetry(device, timestamp, new HashMap<>());
        }
    }

    /**
     * Convert telemetry DTO to Map for JSON storage
     */
    private Map<String, Object> convertTelemetryToMap(BaseTelemetryDTO telemetryDTO) {
        try {
            logger.info("🔄 Converting telemetry DTO to map: {}", telemetryDTO);
            String json = objectMapper.writeValueAsString(telemetryDTO);
            logger.info("🔄 Serialized JSON string: {}", json);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(json, Map.class);
            logger.info("🔄 Deserialized map result: {}", result);
            logger.info("🔄 Map keys: {}", result.keySet());
            
            return result;
        } catch (JsonProcessingException e) {
            logger.error("❌ Error converting telemetry DTO to map", e);
            return new HashMap<>();
        }
    }

    /**
     * Update device status cache with latest telemetry
     */
    private void updateDeviceStatusCache(Device device, BaseTelemetryDTO telemetryDTO) {
        // Run status cache update in its own separate transaction to prevent rollback
        transactionTemplate.executeWithoutResult(status -> {
            try {
                DeviceStatusCache statusCache = statusCacheRepository.findByDeviceId(device.getId())
                    .orElseGet(() -> {
                        logger.info("Creating new DeviceStatusCache for device: {}", device.getId());
                        logger.info("Device ID when creating cache: {}", device.getId());
                        logger.info("Device ID is null: {}", (device.getId() == null));
                        DeviceStatusCache newCache = new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE);
                        logger.info("Created cache with deviceId: {}", newCache.getDeviceId());
                        return newCache;
                    });

                statusCache.setLastSeen(telemetryDTO.getTimestamp());
                statusCache.setStatus(DeviceStatusCache.DeviceStatus.ONLINE);
                statusCache.setCurrentData(convertTelemetryToMap(telemetryDTO));
                statusCache.setUpdatedAt(LocalDateTime.now());

                logger.info("Saving DeviceStatusCache for device: {} with ID: {}", device.getId(), statusCache.getDeviceId());
                statusCacheRepository.save(statusCache);
                logger.info("Successfully saved DeviceStatusCache");
            } catch (Exception e) {
                logger.error("Failed to update device status cache for device: {}", device.getId(), e);
                // Mark transaction for rollback, but this won't affect the main telemetry transaction
                status.setRollbackOnly();
            }
        });
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
    private void checkAlertConditionsInSeparateTransaction(Device device, BaseTelemetryDTO telemetryDTO) {
        // Run alert checking in its own separate transaction to prevent rollback
        transactionTemplate.executeWithoutResult(status -> {
            try {
                checkAlertConditions(device, telemetryDTO);
            } catch (Exception e) {
                logger.error("Failed to check alert conditions for device: {}", device.getId(), e);
                // Mark this transaction for rollback, but don't affect the main telemetry transaction
                status.setRollbackOnly();
            }
        });
    }

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
                    if (telemetryDTO instanceof BMSTelemetryDTO) {
                        checkBMSAlerts(device, (BMSTelemetryDTO) telemetryDTO, alertThresholds);
                    } else {
                        logger.warn("Expected BMSTelemetryDTO but got: {}", telemetryDTO.getClass().getSimpleName());
                    }
                    break;
                    
                case "SOLAR_ARRAY":
                case "SOLAR_INVERTER":
                    if (telemetryDTO instanceof SolarArrayTelemetryDTO) {
                        checkSolarArrayAlerts(device, (SolarArrayTelemetryDTO) telemetryDTO, alertThresholds);
                    } else {
                        logger.warn("Expected SolarArrayTelemetryDTO but got: {}", telemetryDTO.getClass().getSimpleName());
                    }
                    break;
                    
                case "EV_CHARGER":
                    if (telemetryDTO instanceof EVChargerTelemetryDTO) {
                        checkEVChargerAlerts(device, (EVChargerTelemetryDTO) telemetryDTO, alertThresholds);
                    } else {
                        logger.warn("Expected EVChargerTelemetryDTO but got: {}", telemetryDTO.getClass().getSimpleName());
                    }
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
        private final String deviceSerial;
        private final String dataType;

        public TopicMetadata(Long siteId, String deviceSerial, String dataType) {
            this.siteId = siteId;
            this.deviceSerial = deviceSerial;
            this.dataType = dataType;
        }

        public Long getSiteId() {
            return siteId;
        }

        public String getDeviceSerial() {
            return deviceSerial;
        }

        public String getDataType() {
            return dataType;
        }
    }
}