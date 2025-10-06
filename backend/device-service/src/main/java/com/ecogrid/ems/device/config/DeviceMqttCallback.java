package com.ecogrid.ems.device.config;

import com.ecogrid.ems.device.service.DeviceTelemetryProcessor;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQTT callback handler for device telemetry messages
 */
@Component
public class DeviceMqttCallback implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(DeviceMqttCallback.class);

    private final DeviceTelemetryProcessor telemetryProcessor;

    @Autowired
    public DeviceMqttCallback(DeviceTelemetryProcessor telemetryProcessor) {
        this.telemetryProcessor = telemetryProcessor;
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("MQTT connection lost", cause);
        // TODO: Implement reconnection logic
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String payload = new String(message.getPayload());
            logger.info("🔔 MQTT MESSAGE RECEIVED! Topic: {} | Payload: {}", topic, payload);
            logger.info("🔍 Message details - QoS: {}, Retained: {}, Duplicate: {}", 
                message.getQos(), message.isRetained(), message.isDuplicate());
            
            // Process messages based on topic structure
            // Expected patterns: ecogrid/site{N}/{deviceType}/{deviceId}
            if (topic.startsWith("ecogrid/") && topic.split("/").length >= 4) {
                String[] topicParts = topic.split("/");
                String siteId = topicParts[1];  // site1, site2, etc.
                String deviceType = topicParts[2];  // bms, solar, ev
                String deviceId = topicParts[3];    // 001, 002, etc.
                
                logger.info("📊 Processing device telemetry - Site: {}, Type: {}, Device: {}", siteId, deviceType, deviceId);
                telemetryProcessor.processTelemetryMessage(topic, payload);
                logger.info("✅ Telemetry processing completed for topic: {}", topic);
            } else if (topic.contains("/telemetry/")) {
                // Legacy telemetry pattern support
                logger.info("📊 Processing legacy telemetry message for topic: {}", topic);
                telemetryProcessor.processTelemetryMessage(topic, payload);
                logger.info("✅ Legacy telemetry processing completed for topic: {}", topic);
            } else if (topic.contains("/alerts/")) {
                // Handle alert messages
                logger.info("🚨 Processing alert message for topic: {}", topic);
                processAlertMessage(topic, payload);
            } else if (topic.contains("/status")) {
                // Handle device status messages
                logger.info("📡 Processing status message for topic: {}", topic);
                processStatusMessage(topic, payload);
            } else {
                logger.info("❓ Unhandled MQTT topic: {}", topic);
            }
            
        } catch (Exception e) {
            logger.error("Error processing MQTT message from topic: " + topic, e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            logger.debug("MQTT message delivery complete for message ID: {}", token.getMessageId());
        } catch (Exception e) {
            logger.debug("MQTT message delivery complete");
        }
    }

    /**
     * Process alert messages from devices
     */
    private void processAlertMessage(String topic, String payload) {
        logger.info("Alert message received on topic: {} with payload: {}", topic, payload);
        // TODO: Implement alert message processing
        // This will parse the alert payload and create alerts directly
    }

    /**
     * Process device status messages
     */
    private void processStatusMessage(String topic, String payload) {
        logger.info("Status message received on topic: {} with payload: {}", topic, payload);
        // TODO: Implement device status processing
        // This will update device online/offline status
    }
}