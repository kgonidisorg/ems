package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service to manage MQTT connection health and retry logic
 */
@Service
public class MqttConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(MqttConnectionService.class);

    private final MqttClient mqttClient;
    private final MqttConfig mqttConfig;
    private boolean initialConnectionAttempted = false;

    @Autowired
    public MqttConnectionService(MqttClient mqttClient, MqttConfig mqttConfig) {
        this.mqttClient = mqttClient;
        this.mqttConfig = mqttConfig;
    }

    /**
     * Attempt initial MQTT connection after application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("🚀 Application ready - attempting initial MQTT connection...");
        attemptConnection();
    }

    /**
     * Periodically check and retry MQTT connection if needed
     * Runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    public void checkMqttConnection() {
        if (!initialConnectionAttempted) {
            initialConnectionAttempted = true;
            attemptConnection();
        } else if (!mqttClient.isConnected()) {
            logger.warn("⚠️  MQTT client is disconnected - attempting reconnection...");
            attemptConnection();
        } else {
            logger.debug("✅ MQTT connection is healthy");
        }
    }

    /**
     * Get MQTT connection status
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Get MQTT connection details for health checks
     */
    public String getConnectionStatus() {
        if (mqttClient == null) {
            return "MQTT client not initialized";
        }
        
        if (mqttClient.isConnected()) {
            return String.format("Connected to %s (Client ID: %s)", 
                               mqttClient.getServerURI(), 
                               mqttClient.getClientId());
        } else {
            return String.format("Disconnected from %s (Client ID: %s)", 
                               mqttClient.getServerURI(), 
                               mqttClient.getClientId());
        }
    }

    private void attemptConnection() {
        try {
            boolean success = mqttConfig.attemptMqttConnection(mqttClient);
            if (success) {
                logger.info("🎉 MQTT connection established successfully");
            } else {
                logger.warn("⚠️  MQTT connection attempt failed - will retry in 30 seconds");
            }
        } catch (Exception e) {
            logger.error("❌ Error during MQTT connection attempt", e);
        }
    }
}