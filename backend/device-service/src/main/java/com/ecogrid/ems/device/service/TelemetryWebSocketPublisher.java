package com.ecogrid.ems.device.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryWebSocketPublisher {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryWebSocketPublisher.class);
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TelemetryWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Listen to device-telemetry topic and publish to WebSocket clients
     */
    @KafkaListener(topics = "device-telemetry", groupId = "device-telemetry-ws")
    public void handleTelemetryMessage(Map<String, Object> message) {
        try {
            logger.info("[WS-PUBLISH] Publishing to WebSocket topic: /topic/telemetry");
            messagingTemplate.convertAndSend("/topic/telemetry", message);
        } catch (Exception e) {
            logger.error("[WS-PUBLISH] Error publishing to WebSocket topic /topic/telemetry: {}", e.getMessage(), e);
        }
    }
}
