package com.ecogrid.ems.device.config;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Properties;

/**
 * Test configuration for MQTT broker
 * This starts an embedded MQTT broker for integration tests
 */
@TestConfiguration
@Profile("test")
public class TestMqttConfig {

    private Server mqttBroker;

    @PostConstruct
    public void startEmbeddedBroker() throws IOException {
        // Configure embedded MQTT broker
        Properties brokerProperties = new Properties();
        brokerProperties.setProperty("port", "1883");
        brokerProperties.setProperty("host", "localhost");
        brokerProperties.setProperty("allow_anonymous", "true");
        brokerProperties.setProperty("allow_zero_byte_client_id", "true");
        brokerProperties.setProperty("store_file", "");
        
        IConfig brokerConfig = new MemoryConfig(brokerProperties);
        
        // Start broker
        mqttBroker = new Server();
        mqttBroker.startServer(brokerConfig);
        
        System.out.println("🚀 Embedded MQTT broker started on port 1883");
    }

    @PreDestroy
    public void stopEmbeddedBroker() {
        if (mqttBroker != null) {
            mqttBroker.stopServer();
            System.out.println("🛑 Embedded MQTT broker stopped");
        }
    }
}