package com.ecogrid.ems.notification.integration;

import com.ecogrid.ems.notification.dto.AlertRequest;
import com.ecogrid.ems.notification.dto.AlertResponse;
import com.ecogrid.ems.notification.entity.Alert;
import com.ecogrid.ems.notification.entity.NotificationRule;
import com.ecogrid.ems.notification.entity.NotificationPreference;
import com.ecogrid.ems.notification.repository.AlertRepository;
import com.ecogrid.ems.notification.repository.NotificationRuleRepository;
import com.ecogrid.ems.notification.repository.NotificationPreferenceRepository;
import com.ecogrid.ems.notification.repository.AlertHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = {NotificationServiceIntegrationTest.Initializer.class})
class NotificationServiceIntegrationTest {
    
    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ems_test")
            .withUsername("test")
            .withPassword("test");
    
    @SuppressWarnings("resource")
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withEmbeddedZookeeper();
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private NotificationRuleRepository notificationRuleRepository;
    
    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;
    
    @Autowired
    private AlertHistoryRepository alertHistoryRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private NotificationRule testRule;
    private NotificationPreference testPreferences;
    
    @BeforeEach
    void setUp() {
        // Clean up repositories
        alertHistoryRepository.deleteAll();
        alertRepository.deleteAll();
        notificationRuleRepository.deleteAll();
        notificationPreferenceRepository.deleteAll();
        
        // Create test notification rule
        testRule = new NotificationRule("Test Rule", 100L, "TEMPERATURE_HIGH", Alert.AlertSeverity.MEDIUM);
        testRule.setEmailEnabled(true);
        testRule.setWebsocketEnabled(true);
        testRule = notificationRuleRepository.save(testRule);
        
        // Create test notification preferences
        testPreferences = new NotificationPreference(100L, "test@example.com");
        testPreferences.setEmailEnabled(true);
        testPreferences.setWebsocketEnabled(true);
        testPreferences = notificationPreferenceRepository.save(testPreferences);
        
        System.out.println("DEBUG: Rules created in setup: " + notificationRuleRepository.count());
    }
    
    @Test
    void createAlert_ShouldSaveAlertAndTriggerNotifications() throws Exception {
        // Given
        AlertRequest request = new AlertRequest(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        
        // When
        String response = mockMvc.perform(post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TEMPERATURE_HIGH"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.message").value("Temperature alert"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        AlertResponse alertResponse = objectMapper.readValue(response, AlertResponse.class);
        
        // Then
        // Verify alert was saved to database
        Alert savedAlert = alertRepository.findById(alertResponse.getId()).orElse(null);
        assertNotNull(savedAlert);
        assertEquals("TEMPERATURE_HIGH", savedAlert.getType());
        assertEquals(Alert.AlertSeverity.HIGH, savedAlert.getSeverity());
        assertEquals("Temperature alert", savedAlert.getMessage());
        assertEquals(1L, savedAlert.getDeviceId());
        assertEquals(1L, savedAlert.getSiteId());
        assertFalse(savedAlert.isAcknowledged());
        assertNotNull(savedAlert.getCreatedAt());
        
        // Verify notification history was created (async, may need a small delay)
        Thread.sleep(1000); // Wait for async processing
        
        long historyCount = alertHistoryRepository.findByAlertIdOrderByCreatedAtDesc(savedAlert.getId()).size();
        assertTrue(historyCount > 0, "Alert history should be created for notifications");
    }
    
    @Test
    void acknowledgeAlert_ShouldUpdateAlertStatus() throws Exception {
        // Given
        Alert alert = new Alert(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        alert = alertRepository.save(alert);
        
        // When
        mockMvc.perform(put("/api/alerts/" + alert.getId() + "/acknowledge")
                .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true))
                .andExpect(jsonPath("$.acknowledgedBy").value(100L))
                .andExpect(jsonPath("$.acknowledgedAt").exists());
        
        // Then
        Alert updatedAlert = alertRepository.findById(alert.getId()).orElse(null);
        assertNotNull(updatedAlert);
        assertTrue(updatedAlert.isAcknowledged());
        assertEquals(100L, updatedAlert.getAcknowledgedBy());
        assertNotNull(updatedAlert.getAcknowledgedAt());
    }
    
    @Test
    void resolveAlert_ShouldUpdateAlertStatusAndAutoAcknowledge() throws Exception {
        // Given
        Alert alert = new Alert(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        alert = alertRepository.save(alert);
        
        // When
        mockMvc.perform(put("/api/alerts/" + alert.getId() + "/resolve")
                .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedAt").exists())
                .andExpect(jsonPath("$.acknowledged").value(true))
                .andExpect(jsonPath("$.acknowledgedBy").value(100L));
        
        // Then
        Alert updatedAlert = alertRepository.findById(alert.getId()).orElse(null);
        assertNotNull(updatedAlert);
        assertNotNull(updatedAlert.getResolvedAt());
        assertTrue(updatedAlert.isAcknowledged());
        assertEquals(100L, updatedAlert.getAcknowledgedBy());
        assertNotNull(updatedAlert.getAcknowledgedAt());
    }
    
    @Test
    void searchAlerts_WithFilters_ShouldReturnFilteredResults() throws Exception {
        // Given
        Alert alert1 = new Alert(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "High temperature");
        Alert alert2 = new Alert(2L, 1L, "BATTERY_LOW", Alert.AlertSeverity.MEDIUM, "Low battery");
        Alert alert3 = new Alert(1L, 2L, "TEMPERATURE_HIGH", Alert.AlertSeverity.CRITICAL, "Critical temperature");
        
        alertRepository.saveAll(java.util.Arrays.asList(alert1, alert2, alert3));
        
        // When & Then - Filter by device ID
        mockMvc.perform(get("/api/alerts/search")
                .param("deviceId", "1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
        
        // When & Then - Filter by severity
        mockMvc.perform(get("/api/alerts/search")
                .param("severity", "HIGH")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
        
        // When & Then - Filter by type
        mockMvc.perform(get("/api/alerts/search")
                .param("type", "TEMPERATURE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
    
    @Test
    void createNotificationRule_ShouldSaveRule() throws Exception {
        // Given
        var ruleRequest = new com.ecogrid.ems.notification.dto.NotificationRuleRequest(
            "Temperature Alert Rule", 200L, "TEMPERATURE_*", Alert.AlertSeverity.MEDIUM);
        ruleRequest.setDescription("Alert for all temperature events");
        ruleRequest.setSiteId(1L);
        
        // When
        String response = mockMvc.perform(post("/api/notifications/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Temperature Alert Rule"))
                .andExpect(jsonPath("$.userId").value(200L))
                .andExpect(jsonPath("$.alertType").value("TEMPERATURE_*"))
                .andExpect(jsonPath("$.minSeverity").value("MEDIUM"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        NotificationRule savedRule = objectMapper.readValue(response, NotificationRule.class);
        
        // Then
        NotificationRule dbRule = notificationRuleRepository.findById(savedRule.getId()).orElse(null);
        assertNotNull(dbRule);
        assertEquals("Temperature Alert Rule", dbRule.getName());
        assertEquals(200L, dbRule.getUserId());
        assertEquals("TEMPERATURE_*", dbRule.getAlertType());
        assertEquals(Alert.AlertSeverity.MEDIUM, dbRule.getMinSeverity());
        assertEquals(1L, dbRule.getSiteId());
        assertTrue(dbRule.isActive());
    }
    
    @Test
    void updateNotificationPreferences_ShouldSavePreferences() throws Exception {
        // Given
        NotificationPreference updatedPreferences = new NotificationPreference();
        updatedPreferences.setUserId(100L);
        updatedPreferences.setEmail("updated@example.com");
        updatedPreferences.setEmailEnabled(false);
        updatedPreferences.setWebsocketEnabled(true);
        updatedPreferences.setDigestEnabled(true);
        updatedPreferences.setDigestFrequency(NotificationPreference.DigestFrequency.WEEKLY);
        updatedPreferences.setQuietHoursStart(22);
        updatedPreferences.setQuietHoursEnd(6);
        updatedPreferences.setTimezone("America/New_York");
        updatedPreferences.setCreatedAt(LocalDateTime.now());
        updatedPreferences.setUpdatedAt(LocalDateTime.now());
        
        // When
        mockMvc.perform(put("/api/notifications/preferences/user/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPreferences)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.emailEnabled").value(false))
                .andExpect(jsonPath("$.websocketEnabled").value(true))
                .andExpect(jsonPath("$.digestEnabled").value(true))
                .andExpect(jsonPath("$.digestFrequency").value("WEEKLY"))
                .andExpect(jsonPath("$.quietHoursStart").value(22))
                .andExpect(jsonPath("$.quietHoursEnd").value(6))
                .andExpect(jsonPath("$.timezone").value("America/New_York"));
        
        // Then
        NotificationPreference dbPreferences = notificationPreferenceRepository
            .findByUserId(100L).orElse(null);
        assertNotNull(dbPreferences);
        assertEquals("updated@example.com", dbPreferences.getEmail());
        assertFalse(dbPreferences.isEmailEnabled());
        assertTrue(dbPreferences.isWebsocketEnabled());
        assertTrue(dbPreferences.isDigestEnabled());
        assertEquals(NotificationPreference.DigestFrequency.WEEKLY, dbPreferences.getDigestFrequency());
        assertEquals(22, dbPreferences.getQuietHoursStart());
        assertEquals(6, dbPreferences.getQuietHoursEnd());
        assertEquals("America/New_York", dbPreferences.getTimezone());
    }
    
    @Test
    void getNotificationServiceStatus_ShouldReturnServiceHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("notification-service"))
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.totalNotificationRules").isNumber())
                .andExpect(jsonPath("$.totalNotificationPreferences").isNumber())
                .andExpect(jsonPath("$.activeWebSocketConnections").isNumber())
                .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void alertWorkflow_CompleteLifecycle_ShouldWorkEndToEnd() throws Exception {
        // Step 1: Create alert
        AlertRequest request = new AlertRequest(1L, 1L, "TEMPERATURE_HIGH", Alert.AlertSeverity.HIGH, "Temperature alert");
        
        String createResponse = mockMvc.perform(post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        AlertResponse alert = objectMapper.readValue(createResponse, AlertResponse.class);
        Long alertId = alert.getId();
        
        // Step 2: Verify alert is unacknowledged
        mockMvc.perform(get("/api/alerts/" + alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(false))
                .andExpect(jsonPath("$.resolvedAt").doesNotExist());
        
        // Step 3: Acknowledge alert
        mockMvc.perform(put("/api/alerts/" + alertId + "/acknowledge")
                .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true))
                .andExpect(jsonPath("$.acknowledgedBy").value(100L));
        
        // Step 4: Resolve alert
        mockMvc.perform(put("/api/alerts/" + alertId + "/resolve")
                .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedAt").exists());
        
        // Step 5: Verify final state
        mockMvc.perform(get("/api/alerts/" + alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true))
                .andExpect(jsonPath("$.acknowledgedBy").value(100L))
                .andExpect(jsonPath("$.resolvedAt").exists());
    }
    
    @Test
    void kafkaDeviceEventIntegration_ShouldProcessDeviceEventsAndCreateAlerts() throws Exception {
        // Given - Kafka device event message
        String deviceEventTopic = "device-telemetry";
        Map<String, Object> deviceEventMessage = Map.of(
            "deviceId", 1,
            "siteId", 1,
            "timestamp", "2025-10-04T10:00:00Z",
            "telemetry", Map.of(
                "temperature", 85.5,
                "voltage", 220.0,
                "power", 1800.0
            ),
            "alertConditions", List.of(
                Map.of(
                    "type", "TEMPERATURE_HIGH",
                    "severity", "HIGH",
                    "message", "Temperature exceeded threshold: 85.5°C"
                )
            )
        );
        
        // When - Send Kafka message
        kafkaTemplate.send(deviceEventTopic, deviceEventMessage).get(10, TimeUnit.SECONDS);
        
        // Allow time for Kafka consumer to process the message
        Thread.sleep(3000);
        
        // Then - Verify alert was created from Kafka event
        List<Alert> alerts = alertRepository.findByDeviceIdOrderByCreatedAtDesc(1L);
        assertTrue(!alerts.isEmpty(), "Alert should be created from Kafka device event");
        
        Alert createdAlert = alerts.get(0);
        assertEquals("TEMPERATURE_HIGH", createdAlert.getType());
        assertEquals(Alert.AlertSeverity.HIGH, createdAlert.getSeverity());
        assertEquals("Device temperature is 85.5°C", createdAlert.getMessage());
        assertEquals(1L, createdAlert.getDeviceId());
        assertEquals(1L, createdAlert.getSiteId());
        assertFalse(createdAlert.isAcknowledged());
        
        // Verify notification processing occurred
        var notificationHistory = alertHistoryRepository.findByAlertIdOrderByCreatedAtDesc(createdAlert.getId());
        assertTrue(!notificationHistory.isEmpty(), "Notification history should be created");
    }
    
    @Test
    void kafkaDeviceStatusIntegration_ShouldProcessDeviceStatusEvents() throws Exception {
        // Given - Device status event
        String deviceStatusTopic = "device-status";
        Map<String, Object> statusEventMessage = Map.of(
            "deviceId", 2,
            "siteId", 1,
            "timestamp", "2025-10-04T10:00:00Z",
            "status", "OFFLINE",
            "previousStatus", "ONLINE",
            "reason", "Connection timeout"
        );
        
        // When - Send Kafka status message
        kafkaTemplate.send(deviceStatusTopic, statusEventMessage).get(10, TimeUnit.SECONDS);
        
        // Allow time for processing
        Thread.sleep(3000);
        
        // Then - Verify device offline alert was created
        List<Alert> alerts = alertRepository.findByTypeOrderByCreatedAtDesc("DEVICE_OFFLINE");
        assertTrue(!alerts.isEmpty(), "Device offline alert should be created");
        
        Alert deviceAlert = alerts.stream()
                .filter(alert -> alert.getDeviceId().equals(2L))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Device offline alert for device 2 not found"));
        assertEquals("DEVICE_OFFLINE", deviceAlert.getType());
        assertEquals(Alert.AlertSeverity.HIGH, deviceAlert.getSeverity());
        assertEquals(2L, deviceAlert.getDeviceId());
        assertEquals(1L, deviceAlert.getSiteId());
    }
    
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(@org.springframework.lang.NonNull ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                "spring.kafka.consumer.group-id=test-notification-group",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "app.notification.email.enabled=false", // Disable email for tests
                "app.websocket.enabled=false" // Disable WebSocket for tests
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}