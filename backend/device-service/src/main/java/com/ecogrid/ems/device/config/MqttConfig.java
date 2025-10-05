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

/**
 * MQTT configuration for device telemetry
 */
@Configuration
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client.id:device-service}")
    private String clientId;

    @Value("${mqtt.username:#{null}}")
    private String username;

    @Value("${mqtt.password:#{null}}")
    private String password;

    @Value("${mqtt.topic.pattern:ecogrid/sites/+/devices/+/+/+}")
    private String topicPattern;

    @Autowired
    private DeviceMqttCallback mqttCallback;

    @Bean
    public MqttClient mqttClient() {
        try {
            MqttClient client = new MqttClient(brokerUrl, clientId + "-" + System.currentTimeMillis());
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(30);
            options.setAutomaticReconnect(true);
            
            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
            }
            
            if (password != null && !password.isEmpty()) {
                options.setPassword(password.toCharArray());
            }

            // Set callback before connecting
            client.setCallback(mqttCallback);
            
            // Connect to MQTT broker
            logger.info("Connecting to MQTT broker: {}", brokerUrl);
            client.connect(options);
            
            // Subscribe to telemetry topics
            client.subscribe(topicPattern, 1);
            logger.info("Subscribed to MQTT topic pattern: {}", topicPattern);
            
            // Subscribe to specific topic patterns
            client.subscribe("ecogrid/sites/+/devices/+/telemetry/+", 1);
            client.subscribe("ecogrid/sites/+/devices/+/alerts/+", 1);
            client.subscribe("ecogrid/sites/+/devices/+/status", 1);
            
            logger.info("MQTT client connected and subscribed successfully");
            return client;
            
        } catch (MqttException e) {
            logger.error("Failed to create MQTT client", e);
            throw new RuntimeException("Failed to initialize MQTT client", e);
        }
    }
}