package com.ecogrid.ems.device.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * MQTT configuration for device telemetry
 */
@Configuration
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client-id:device-service}")
    private String clientId;

    @Value("${mqtt.username:#{null}}")
    private String username;

    @Value("${mqtt.password:#{null}}")
    private String password;

    @Value("${mqtt.topic-pattern:ecogrid/+/+/+}")
    private String topicPattern;

    @Autowired
    private DeviceMqttCallback mqttCallback;

    @Bean
    @Lazy
    public MqttClient mqttClient() {
        try {
            logger.info("Initializing MQTT client for broker: {}", brokerUrl);
            MqttClient client = new MqttClient(brokerUrl, clientId + "-" + System.currentTimeMillis());
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10); // Reduced timeout for faster failure
            options.setKeepAliveInterval(30);
            options.setAutomaticReconnect(true);
            
            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
                logger.debug("MQTT authentication configured for user: {}", username);
            }
            
            if (password != null && !password.isEmpty()) {
                options.setPassword(password.toCharArray());
            }

            // Set callback before connecting
            client.setCallback(mqttCallback);
            
            // Connect to MQTT broker
            logger.info("Attempting to connect to MQTT broker: {} with client ID: {}", brokerUrl, client.getClientId());
            client.connect(options);
            
            // Subscribe to telemetry topics
            client.subscribe(topicPattern, 1);
            logger.info("✅ Subscribed to MQTT topic pattern: {}", topicPattern);
            
            // Subscribe to specific device topic patterns matching our database structure
            // Pattern matches: ecogrid/site1/bms/001, ecogrid/site2/solar/001, etc.
            logger.info("✅ Subscribed to device topic pattern for telemetry data");
            
            logger.info("🎉 MQTT client connected and subscribed successfully to broker: {}", brokerUrl);
            return client;
            
        } catch (MqttException e) {
            logger.error("❌ Failed to connect to MQTT broker: {} - Error: {} (Code: {})", 
                        brokerUrl, e.getMessage(), e.getReasonCode());
            logger.error("MQTT connection failure details:", e);
            
            // Create a disconnected client that can be used later for retry attempts
            try {
                MqttClient disconnectedClient = new MqttClient(brokerUrl, clientId + "-disconnected-" + System.currentTimeMillis());
                disconnectedClient.setCallback(mqttCallback);
                logger.warn("⚠️  Created disconnected MQTT client - MQTT features will be unavailable until connection is restored");
                return disconnectedClient;
            } catch (MqttException ex) {
                logger.error("Failed to create even a disconnected MQTT client", ex);
                throw new RuntimeException("Failed to initialize MQTT client", ex);
            }
        }
    }
    
    /**
     * Attempt to connect/reconnect to MQTT broker
     * This method can be called to retry MQTT connection
     */
    public boolean attemptMqttConnection(MqttClient client) {
        if (client == null) {
            logger.warn("Cannot attempt MQTT connection - client is null");
            return false;
        }
        
        if (client.isConnected()) {
            logger.debug("MQTT client is already connected");
            return true;
        }
        
        try {
            logger.info("🔄 Attempting to reconnect to MQTT broker: {}", brokerUrl);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            options.setAutomaticReconnect(true);
            
            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
            }
            
            if (password != null && !password.isEmpty()) {
                options.setPassword(password.toCharArray());
            }
            
            client.connect(options);
            
            // Re-subscribe to topics
            client.subscribe(topicPattern, 1);
            
            logger.info("✅ MQTT reconnection successful - subscribed to all topics");
            return true;
            
        } catch (MqttException e) {
            logger.error("❌ MQTT reconnection failed: {} (Code: {})", e.getMessage(), e.getReasonCode());
            return false;
        }
    }
}