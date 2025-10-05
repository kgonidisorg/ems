package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for creating and managing device alerts
 * 
 * Note: This is a temporary implementation that publishes alerts to Kafka.
 * The actual Alert entity storage will be handled by the Notification Service.
 */
@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AlertService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Create an alert for a device
     */
    public void createAlert(Device device, String alertType, String severity, 
                           String message, Double thresholdValue, Double actualValue) {
        try {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("deviceId", device.getId());
            alertData.put("siteId", device.getSite().getId());
            alertData.put("alertType", alertType);
            alertData.put("severity", severity);
            alertData.put("message", message);
            alertData.put("thresholdValue", thresholdValue);
            alertData.put("actualValue", actualValue);
            alertData.put("deviceName", device.getName());
            alertData.put("deviceType", device.getDeviceType().getName());
            alertData.put("siteName", device.getSite().getName());
            alertData.put("timestamp", LocalDateTime.now());
            alertData.put("acknowledged", false);
            alertData.put("resolved", false);

            // Publish alert to Kafka for processing by Notification Service
            kafkaTemplate.send("device-alerts", device.getId().toString(), alertData);
            
            logger.info("Alert created for device {}: {} - {}", device.getId(), alertType, message);
            
        } catch (Exception e) {
            logger.error("Error creating alert for device: " + device.getId(), e);
        }
    }

    /**
     * Create a simple alert without threshold values
     */
    public void createAlert(Device device, String alertType, String severity, String message) {
        createAlert(device, alertType, severity, message, null, null);
    }
}