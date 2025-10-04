package com.ecogrid.ems.notification.config;

import com.ecogrid.ems.notification.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
    @Autowired
    private WebSocketService webSocketService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("WebSocket connection established with session ID: {}", sessionId);
        
        // Extract user ID from headers if available
        String userIdHeader = headerAccessor.getFirstNativeHeader("X-User-ID");
        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                webSocketService.registerUserSession(userId, sessionId);
                logger.info("Registered WebSocket session for user: {} with session ID: {}", userId, sessionId);
            } catch (NumberFormatException e) {
                logger.warn("Invalid user ID in WebSocket header: {}", userIdHeader);
            }
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("WebSocket connection closed for session ID: {}", sessionId);
        
        // Extract user ID from headers if available
        String userIdHeader = headerAccessor.getFirstNativeHeader("X-User-ID");
        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                webSocketService.unregisterUserSession(userId);
                logger.info("Unregistered WebSocket session for user: {}", userId);
            } catch (NumberFormatException e) {
                logger.warn("Invalid user ID in WebSocket header during disconnect: {}", userIdHeader);
            }
        }
    }
}