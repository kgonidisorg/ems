package com.ecogrid.ems.notification.kafka;

import com.ecogrid.ems.notification.dto.AlertRequest;
import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeviceEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceEventConsumer.class);
    
    @Autowired
    private AlertService alertService;
    
    /**
     * Listen for device telemetry events and create alerts based on thresholds
     */
    @KafkaListener(topics = "${app.kafka.topics.device-telemetry}")
    public void handleDeviceTelemetry(
            @Payload Map<String, Object> telemetryData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.debug("Received device telemetry event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
        
        try {
            processTelemetryData(telemetryData);
        } catch (Exception e) {
            logger.error("Error processing telemetry data: {}", telemetryData, e);
        }
    }
    
    /**
     * Listen for device status events
     */
    @KafkaListener(topics = "${app.kafka.topics.device-status}")
    public void handleDeviceStatus(
            @Payload Map<String, Object> statusData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.debug("Received device status event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
        
        try {
            processStatusData(statusData);
        } catch (Exception e) {
            logger.error("Error processing status data: {}", statusData, e);
        }
    }
    
    /**
     * Process telemetry data and create alerts based on thresholds
     */
    private void processTelemetryData(Map<String, Object> telemetryData) {
        Long deviceId = getLongValue(telemetryData, "deviceId");
        Long siteId = getLongValue(telemetryData, "siteId");
        
        if (deviceId == null || siteId == null) {
            logger.warn("Missing deviceId or siteId in telemetry data: {}", telemetryData);
            return;
        }
        
        logger.debug("Processing telemetry data for device: {}, site: {}", deviceId, siteId);
        
        // Check for temperature alerts
        Double temperature = getDoubleValue(telemetryData, "temperature");
        if (temperature != null) {
            checkTemperatureAlerts(deviceId, siteId, temperature);
        }
        
        // Check for voltage alerts
        Double voltage = getDoubleValue(telemetryData, "voltage");
        if (voltage != null) {
            checkVoltageAlerts(deviceId, siteId, voltage);
        }
        
        // Check for power alerts
        Double power = getDoubleValue(telemetryData, "power");
        if (power != null) {
            checkPowerAlerts(deviceId, siteId, power);
        }
        
        // Check for battery level alerts
        Double batteryLevel = getDoubleValue(telemetryData, "batteryLevel");
        if (batteryLevel != null) {
            checkBatteryAlerts(deviceId, siteId, batteryLevel);
        }
        
        // Check for efficiency alerts
        Double efficiency = getDoubleValue(telemetryData, "efficiency");
        if (efficiency != null) {
            checkEfficiencyAlerts(deviceId, siteId, efficiency);
        }
    }
    
    /**
     * Process device status data and create alerts for offline devices
     */
    private void processStatusData(Map<String, Object> statusData) {
        Long deviceId = getLongValue(statusData, "deviceId");
        Long siteId = getLongValue(statusData, "siteId");
        String status = getStringValue(statusData, "status");
        
        if (deviceId == null || siteId == null || status == null) {
            logger.warn("Missing required fields in status data: {}", statusData);
            return;
        }
        
        logger.debug("Processing status data for device: {}, site: {}, status: {}", deviceId, siteId, status);
        
        // Create alert for device going offline
        if ("OFFLINE".equalsIgnoreCase(status)) {
            AlertRequest alertRequest = new AlertRequest(
                deviceId,
                siteId,
                "DEVICE_OFFLINE",
                Alert.AlertSeverity.HIGH,
                String.format("Device %d has gone offline", deviceId)
            );
            alertRequest.setDescription("Device is no longer responding to monitoring requests");
            
            alertService.createAlert(alertRequest);
            logger.info("Created DEVICE_OFFLINE alert for device: {}", deviceId);
        }
        
        // Create alert for device coming back online after being offline
        if ("ONLINE".equalsIgnoreCase(status)) {
            // This could be implemented as a recovery notification
            logger.info("Device {} is back online", deviceId);
        }
    }
    
    /**
     * Check temperature thresholds and create alerts
     */
    private void checkTemperatureAlerts(Long deviceId, Long siteId, Double temperature) {
        if (temperature > 80.0) {
            AlertRequest alertRequest = new AlertRequest(
                deviceId,
                siteId,
                "TEMPERATURE_HIGH",
                temperature > 90.0 ? Alert.AlertSeverity.CRITICAL : Alert.AlertSeverity.HIGH,
                String.format("Device temperature is %,.1f°C", temperature)
            );
            alertRequest.setDescription("Device is operating at elevated temperature which may affect performance and lifespan");
            alertRequest.setMetadata(String.format("{\"temperature\": %,.1f, \"threshold\": 80.0}", temperature));
            
            alertService.createAlert(alertRequest);
            logger.info("Created TEMPERATURE_HIGH alert for device: {} ({}°C)", deviceId, temperature);
        }
    }
    
    /**
     * Check voltage thresholds and create alerts
     */
    private void checkVoltageAlerts(Long deviceId, Long siteId, Double voltage) {
        if (voltage < 220.0 || voltage > 250.0) {
            Alert.AlertSeverity severity = (voltage < 200.0 || voltage > 260.0) ? 
                Alert.AlertSeverity.CRITICAL : Alert.AlertSeverity.MEDIUM;
            
            AlertRequest alertRequest = new AlertRequest(
                deviceId,
                siteId,
                voltage < 220.0 ? "VOLTAGE_LOW" : "VOLTAGE_HIGH",
                severity,
                String.format("Device voltage is %,.1fV", voltage)
            );
            alertRequest.setDescription("Device voltage is outside normal operating range (220-250V)");
            alertRequest.setMetadata(String.format("{\"voltage\": %,.1f, \"normalRange\": \"220-250V\"}", voltage));
            
            alertService.createAlert(alertRequest);
            logger.info("Created voltage alert for device: {} ({}V)", deviceId, voltage);
        }
    }
    
    /**
     * Check power thresholds and create alerts
     */
    private void checkPowerAlerts(Long deviceId, Long siteId, Double power) {
        // Negative power could indicate reverse power flow or measurement error
        if (power < 0) {
            AlertRequest alertRequest = new AlertRequest(
                deviceId,
                siteId,
                "POWER_NEGATIVE",
                Alert.AlertSeverity.MEDIUM,
                String.format("Device is reporting negative power: %,.1fW", power)
            );
            alertRequest.setDescription("Negative power reading may indicate reverse power flow or sensor malfunction");
            alertRequest.setMetadata(String.format("{\"power\": %,.1f}", power));
            
            alertService.createAlert(alertRequest);
            logger.info("Created POWER_NEGATIVE alert for device: {} ({}W)", deviceId, power);
        }
    }
    
    /**
     * Check battery level and create alerts
     */
    private void checkBatteryAlerts(Long deviceId, Long siteId, Double batteryLevel) {
        if (batteryLevel < 20.0) {
            Alert.AlertSeverity severity = batteryLevel < 10.0 ? 
                Alert.AlertSeverity.CRITICAL : Alert.AlertSeverity.HIGH;
            
            AlertRequest alertRequest = new AlertRequest(
                deviceId,
                siteId,
                "BATTERY_LOW",
                severity,
                String.format("Device battery level is %,.1f%%", batteryLevel)
            );
            alertRequest.setDescription("Device battery is running low and may need charging or replacement");
            alertRequest.setMetadata(String.format("{\"batteryLevel\": %,.1f, \"threshold\": 20.0}", batteryLevel));
            
            alertService.createAlert(alertRequest);
            logger.info("Created BATTERY_LOW alert for device: {} ({}%)", deviceId, batteryLevel);
        }
    }
    
    /**
     * Check efficiency and create alerts
     */
    private void checkEfficiencyAlerts(Long deviceId, Long siteId, Double efficiency) {
        if (efficiency < 80.0) {
            Alert.AlertSeverity severity = efficiency < 70.0 ? 
                Alert.AlertSeverity.HIGH : Alert.AlertSeverity.MEDIUM;
            
            AlertRequest alertRequest = new AlertRequest(
                deviceId,
                siteId,
                "EFFICIENCY_LOW",
                severity,
                String.format("Device efficiency is %,.1f%%", efficiency)
            );
            alertRequest.setDescription("Device efficiency is below optimal levels, maintenance may be required");
            alertRequest.setMetadata(String.format("{\"efficiency\": %,.1f, \"optimalThreshold\": 80.0}", efficiency));
            
            alertService.createAlert(alertRequest);
            logger.info("Created EFFICIENCY_LOW alert for device: {} ({}%)", deviceId, efficiency);
        }
    }
    
    // Utility methods for safe data extraction
    
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value for key {}: {}", key, value);
            }
        }
        return null;
    }
    
    private Double getDoubleValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value for key {}: {}", key, value);
            }
        }
        return null;
    }
    
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
}