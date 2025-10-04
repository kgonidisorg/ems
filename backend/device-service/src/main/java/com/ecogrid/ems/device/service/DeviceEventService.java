package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.event.DeviceStatusEvent;
import com.ecogrid.ems.device.event.DeviceTelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for publishing device-related events
 * This can be extended to publish to Kafka topics
 */
@Service
public class DeviceEventService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceEventService.class);

    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeviceEventService(ApplicationEventPublisher eventPublisher, KafkaTemplate<String, Object> kafkaTemplate) {
        this.eventPublisher = eventPublisher;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish device telemetry event
     */
    public void publishTelemetryEvent(Long deviceId, String serialNumber, Long siteId, 
                                    String deviceType, Map<String, Object> telemetryData) {
        try {
            DeviceTelemetryEvent event = DeviceTelemetryEvent.create(
                deviceId, serialNumber, siteId, deviceType, telemetryData
            );
            
            eventPublisher.publishEvent(event);
            logger.debug("Published telemetry event for device: {}", serialNumber);
            
            // Publish to Kafka topic for real-time analytics
            kafkaTemplate.send("device-telemetry", serialNumber, event);
            
        } catch (Exception e) {
            logger.error("Failed to publish telemetry event for device: {}", serialNumber, e);
        }
    }

    /**
     * Publish device status change event
     */
    public void publishStatusChangeEvent(Long deviceId, String serialNumber, Long siteId, 
                                       String previousStatus, String newStatus, String reason) {
        try {
            DeviceStatusEvent event = DeviceStatusEvent.create(
                deviceId, serialNumber, siteId, previousStatus, newStatus, reason
            );
            
            eventPublisher.publishEvent(event);
            logger.info("Published status change event for device {}: {} -> {}", 
                serialNumber, previousStatus, newStatus);
            
            // Publish to Kafka topic for notifications and analytics
            kafkaTemplate.send("device-status", serialNumber, event);
            
        } catch (Exception e) {
            logger.error("Failed to publish status change event for device: {}", serialNumber, e);
        }
    }
}