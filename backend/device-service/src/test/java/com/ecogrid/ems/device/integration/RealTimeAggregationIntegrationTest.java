package com.ecogrid.ems.device.integration;

import com.ecogrid.ems.device.DeviceServiceApplication;
import com.ecogrid.ems.device.config.IntegrationTestConfiguration;
import com.ecogrid.ems.device.config.TestMqttConfig;
import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.DeviceType;
import com.ecogrid.ems.device.entity.Site;
import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.DeviceTypeRepository;
import com.ecogrid.ems.device.repository.SiteRepository;
import com.ecogrid.ems.device.repository.DeviceTelemetryRepository;
import com.ecogrid.ems.device.repository.DeviceStatusCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.awaitility.Awaitility;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for RealTimeAggregationService analytics pipeline
 * Tests complete MQTT → Kafka → Aggregation → Analytics topics flow
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DeviceServiceApplication.class,
    properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@Import({IntegrationTestConfiguration.class, TestMqttConfig.class})  
@EmbeddedKafka(
    partitions = 1,
    topics = {"device-telemetry", "site-bms-aggregation", "site-solar-aggregation", "site-evcharger-aggregation"}
)
@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
class RealTimeAggregationIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeAggregationIntegrationTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ems_aggregation_test")
            .withUsername("ems_test_user")
            .withPassword("ems_test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // Configure MQTT to use embedded broker
        registry.add("mqtt.broker.url", () -> "tcp://localhost:1883");
        registry.add("mqtt.client.id", () -> "test-aggregation-client");
        registry.add("mqtt.topic.pattern", () -> "ecogrid/sites/+/devices/+/telemetry/+");
    }

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private DeviceTelemetryRepository telemetryRepository;

    @Autowired
    private DeviceStatusCacheRepository statusCacheRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MqttClient productionMqttClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    // Test data
    private Site testSite;
    private List<Device> bmsDevices = new ArrayList<>();
    private List<Device> solarDevices = new ArrayList<>();
    private List<Device> evChargerDevices = new ArrayList<>();
    private DeviceType bmsDeviceType;
    private DeviceType solarDeviceType;
    private DeviceType evChargerDeviceType;

    // Test consumers for verifying aggregation output
    private Consumer<String, String> aggregationConsumer;

    @BeforeEach
    void setUp() throws Exception {
        logger.info("🚀 Setting up RealTimeAggregation integration test");
        
        // Setup Kafka consumer for aggregation topics
        setupAggregationConsumer();
        
        // Clean up any existing test data in separate transaction
        transactionTemplate.execute(status -> {
            cleanupTestData();
            return null;
        });
        
        // Setup test data in separate committed transaction
        transactionTemplate.execute(status -> {
            setupTestData();
            return null;
        });
        
        logger.info("✅ Test setup complete - {} BMS, {} Solar, {} EV Charger devices created", 
                   bmsDevices.size(), solarDevices.size(), evChargerDevices.size());
    }

    @AfterEach
    void tearDown() throws Exception {
        logger.info("🧹 Cleaning up test environment");
        
        if (aggregationConsumer != null) {
            aggregationConsumer.close();
        }
        
        // Clean up test data
        cleanupTestData();
        
        logger.info("✅ Test cleanup complete");
    }

    @Test
    @DisplayName("Should aggregate BMS telemetry data and publish to site-bms-aggregation topic")
    void shouldAggregateBMSTelemetryData() throws Exception {
        logger.info("🔋 Testing BMS aggregation pipeline");

        // Given: Multiple BMS devices with telemetry data
        for (Device bmsDevice : bmsDevices) {
            Map<String, Object> bmsData = createBMSTelemetryData();
            String topic = String.format("ecogrid/sites/%d/devices/%s/telemetry/bms", 
                                       testSite.getId(), bmsDevice.getSerialNumber());
            
            // When: Publish MQTT telemetry messages
            publishMqttMessage(topic, bmsData);
        }

        // Then: Verify BMS aggregation is published to site-bms-aggregation topic
        verifyBMSAggregation();
        
        logger.info("✅ BMS aggregation test completed successfully");
    }

    @Test
    @DisplayName("Should aggregate Solar Array telemetry data and publish to site-solar-aggregation topic") 
    void shouldAggregateSolarTelemetryData() throws Exception {
        logger.info("☀️ Testing Solar Array aggregation pipeline");

        // Given: Multiple Solar Array devices with telemetry data
        for (Device solarDevice : solarDevices) {
            Map<String, Object> solarData = createSolarTelemetryData();
            String topic = String.format("ecogrid/sites/%d/devices/%s/telemetry/solar_array", 
                                       testSite.getId(), solarDevice.getSerialNumber());
            
            // When: Publish MQTT telemetry messages
            publishMqttMessage(topic, solarData);
        }

        // Then: Verify Solar aggregation is published to site-solar-aggregation topic
        verifySolarAggregation();
        
        logger.info("✅ Solar Array aggregation test completed successfully");
    }

    @Test
    @DisplayName("Should aggregate EV Charger telemetry data and publish to site-evcharger-aggregation topic")
    void shouldAggregateEVChargerTelemetryData() throws Exception {
        logger.info("🚗 Testing EV Charger aggregation pipeline");

        // Given: Multiple EV Charger devices with telemetry data
        for (Device evChargerDevice : evChargerDevices) {
            Map<String, Object> evChargerData = createEVChargerTelemetryData();
            String topic = String.format("ecogrid/sites/%d/devices/%s/telemetry/ev_charger", 
                                       testSite.getId(), evChargerDevice.getSerialNumber());
            
            // When: Publish MQTT telemetry messages
            publishMqttMessage(topic, evChargerData);
        }

        // Then: Verify EV Charger aggregation is published to site-evcharger-aggregation topic
        verifyEVChargerAggregation();
        
        logger.info("✅ EV Charger aggregation test completed successfully");
    }

    // === Helper Methods ===

    private void setupAggregationConsumer() {
        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("aggregation-test-consumer", "true", embeddedKafkaBroker));
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        aggregationConsumer = consumerFactory.createConsumer();
        aggregationConsumer.subscribe(Arrays.asList("site-bms-aggregation", "site-solar-aggregation", "site-evcharger-aggregation"));
    }

    private void setupTestData() {
        // Create Site
        testSite = new Site();
        testSite.setName("Test Aggregation Site");
        testSite.setDescription("Site for testing real-time aggregations");
        testSite.setTimezone("UTC");
        testSite = siteRepository.save(testSite);

        // Create Device Types
        createDeviceTypes();

        // Create Test Devices (2 of each type)
        createBMSDevices();
        createSolarDevices();
        createEVChargerDevices();

        logger.info("📊 Created test data: Site ID {}, {} total devices", testSite.getId(), 
                   bmsDevices.size() + solarDevices.size() + evChargerDevices.size());
    }

    private void createDeviceTypes() {
        // BMS Device Type
        bmsDeviceType = new DeviceType();
        bmsDeviceType.setName("BMS");
        bmsDeviceType.setCategory("ENERGY_STORAGE");
        bmsDeviceType = deviceTypeRepository.save(bmsDeviceType);

        // Solar Array Device Type
        solarDeviceType = new DeviceType();
        solarDeviceType.setName("Solar Array");
        solarDeviceType.setCategory("RENEWABLE_GENERATION");
        solarDeviceType = deviceTypeRepository.save(solarDeviceType);

        // EV Charger Device Type
        evChargerDeviceType = new DeviceType();
        evChargerDeviceType.setName("EV Charger");
        evChargerDeviceType.setCategory("LOAD");
        evChargerDeviceType = deviceTypeRepository.save(evChargerDeviceType);
    }

    private void createBMSDevices() {
        for (int i = 1; i <= 2; i++) {
            Device bmsDevice = new Device();
            bmsDevice.setSerialNumber("TEST-BMS-" + String.format("%03d", i));
            bmsDevice.setName("Test BMS Device " + i);
            bmsDevice.setManufacturer("Test BMS Manufacturer");
            bmsDevice.setModel("Test BMS Model v1.0");
            bmsDevice.setDeviceType(bmsDeviceType);
            bmsDevice.setSite(testSite);
            bmsDevice.setStatus(Device.DeviceStatus.OFFLINE);
            bmsDevice.setRatedPowerKw(BigDecimal.valueOf(100.0));
            bmsDevices.add(deviceRepository.save(bmsDevice));
        }
    }

    private void createSolarDevices() {
        for (int i = 1; i <= 2; i++) {
            Device solarDevice = new Device();
            solarDevice.setSerialNumber("TEST-SOLAR-" + String.format("%03d", i));
            solarDevice.setName("Test Solar Array " + i);
            solarDevice.setManufacturer("Test Solar Manufacturer");
            solarDevice.setModel("Test Solar Model v2.0");
            solarDevice.setDeviceType(solarDeviceType);
            solarDevice.setSite(testSite);
            solarDevice.setStatus(Device.DeviceStatus.OFFLINE);
            solarDevice.setRatedPowerKw(BigDecimal.valueOf(75.0));
            solarDevices.add(deviceRepository.save(solarDevice));
        }
    }

    private void createEVChargerDevices() {
        for (int i = 1; i <= 2; i++) {
            Device evChargerDevice = new Device();
            evChargerDevice.setSerialNumber("TEST-EVCHARGER-" + String.format("%03d", i));
            evChargerDevice.setName("Test EV Charger " + i);
            evChargerDevice.setManufacturer("Test EV Charger Manufacturer");
            evChargerDevice.setModel("Test EV Charger Model v3.0");
            evChargerDevice.setSite(testSite);
            evChargerDevice.setDeviceType(evChargerDeviceType);
            evChargerDevice.setStatus(Device.DeviceStatus.OFFLINE);
            evChargerDevice.setRatedPowerKw(BigDecimal.valueOf(50.0));
            evChargerDevices.add(deviceRepository.save(evChargerDevice));
        }
    }

    private Map<String, Object> createBMSTelemetryData() {
        Map<String, Object> telemetryData = new HashMap<>();
        telemetryData.put("timestamp", LocalDateTime.now().toString());
        telemetryData.put("voltage", 52.4);
        telemetryData.put("current", 15.2);
        telemetryData.put("temperature", 25.5);
        telemetryData.put("soc", 85.0); // State of charge
        telemetryData.put("healthStatus", "EXCELLENT");
        telemetryData.put("cycleCount", 1250);
        telemetryData.put("chargeRate", 0.0);
        telemetryData.put("remainingCapacity", 42.5);
        telemetryData.put("nominalCapacity", 50.0);
        telemetryData.put("roundTripEfficiency", 95.0);
        return telemetryData;
    }

    private Map<String, Object> createSolarTelemetryData() {
        Map<String, Object> telemetryData = new HashMap<>();
        telemetryData.put("timestamp", LocalDateTime.now().toString());
        telemetryData.put("currentOutput", 65.5);
        telemetryData.put("energyYield", 450.0);
        telemetryData.put("inverterEfficiency", 18.5);
        telemetryData.put("systemEfficiency", 17.8);
        telemetryData.put("panelTemperature", 35.2);
        telemetryData.put("irradiance", 800.0);
        telemetryData.put("performanceRatio", 0.82);
        telemetryData.put("inverterStatus", "OPERATIONAL");
        return telemetryData;
    }

    private Map<String, Object> createEVChargerTelemetryData() {
        Map<String, Object> telemetryData = new HashMap<>();
        telemetryData.put("timestamp", LocalDateTime.now().toString());
        telemetryData.put("powerDelivered", 25.0);
        telemetryData.put("energyDelivered", 15.5);
        telemetryData.put("avgSessionDuration", 60); // minutes
        telemetryData.put("connectorStatus", "CHARGING");
        telemetryData.put("utilizationRate", 0.75);
        telemetryData.put("revenue", 12.50);
        // Add required fields that were missing
        telemetryData.put("activeSessions", 1);
        telemetryData.put("totalSessions", 25);
        telemetryData.put("networkConnectivity", true);
        telemetryData.put("paymentSystemStatus", "ONLINE");
        telemetryData.put("faults", 0);
        telemetryData.put("uptime", 99.5);
        return telemetryData;
    }

    private void publishMqttMessage(String topic, Map<String, Object> data) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(data);
        MqttMessage message = new MqttMessage(jsonPayload.getBytes());
        message.setQos(1);
        
        logger.info("📤 Publishing MQTT message to topic: {} - Data: {}", topic, jsonPayload);
        productionMqttClient.publish(topic, message);
        
        // Small delay to ensure message is processed
        Thread.sleep(500);
    }

    private void verifyBMSAggregation() {
        logger.info("🔍 Verifying BMS aggregation on site-bms-aggregation topic");
        
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records = aggregationConsumer.poll(java.time.Duration.ofMillis(1000));
                    
                    ConsumerRecord<String, String> bmsAggregationRecord = null;
                    for (ConsumerRecord<String, String> record : records) {
                        if ("site-bms-aggregation".equals(record.topic())) {
                            bmsAggregationRecord = record;
                            break;
                        }
                    }
                    
                    assertThat(bmsAggregationRecord).isNotNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> aggregationData = objectMapper.readValue(bmsAggregationRecord.value(), Map.class);
                    
                    // Verify BMS aggregation fields
                    assertThat(aggregationData).containsKey("totalCapacity");
                    assertThat(aggregationData).containsKey("avgSOC");
                    assertThat(aggregationData).containsKey("totalChargeRate");
                    assertThat(aggregationData).containsKey("avgTemperature");
                    assertThat(aggregationData).containsKey("avgEfficiency");
                    
                    logger.info("✅ BMS aggregation verified - totalCapacity: {}, avgSOC: {}", 
                               aggregationData.get("totalCapacity"), aggregationData.get("avgSOC"));
                });
    }

    private void verifySolarAggregation() {
        logger.info("🔍 Verifying Solar aggregation on site-solar-aggregation topic");
        
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records = aggregationConsumer.poll(java.time.Duration.ofMillis(1000));
                    
                    ConsumerRecord<String, String> solarAggregationRecord = null;
                    for (ConsumerRecord<String, String> record : records) {
                        if ("site-solar-aggregation".equals(record.topic())) {
                            solarAggregationRecord = record;
                            break;
                        }
                    }
                    
                    assertThat(solarAggregationRecord).isNotNull();
                    
                    @SuppressWarnings("unchecked") 
                    Map<String, Object> aggregationData = objectMapper.readValue(solarAggregationRecord.value(), Map.class);
                    
                    // Verify Solar aggregation fields
                    assertThat(aggregationData).containsKey("totalOutput");
                    assertThat(aggregationData).containsKey("dailyYield");
                    assertThat(aggregationData).containsKey("avgEfficiency");
                    assertThat(aggregationData).containsKey("avgPanelTemp");
                    assertThat(aggregationData).containsKey("irradiance");
                    assertThat(aggregationData).containsKey("performanceRatio");
                    
                    logger.info("✅ Solar aggregation verified - totalOutput: {}, dailyYield: {}", 
                               aggregationData.get("totalOutput"), aggregationData.get("dailyYield"));
                });
    }

    private void verifyEVChargerAggregation() {
        logger.info("🔍 Verifying EV Charger aggregation on site-evcharger-aggregation topic");
        
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records = aggregationConsumer.poll(java.time.Duration.ofMillis(1000));
                    
                    ConsumerRecord<String, String> evChargerAggregationRecord = null;
                    for (ConsumerRecord<String, String> record : records) {
                        if ("site-evcharger-aggregation".equals(record.topic())) {
                            evChargerAggregationRecord = record;
                            break;
                        }
                    }
                    
                    assertThat(evChargerAggregationRecord).isNotNull();
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> aggregationData = objectMapper.readValue(evChargerAggregationRecord.value(), Map.class);
                    
                    // Verify EV Charger aggregation fields
                    assertThat(aggregationData).containsKey("totalChargers");
                    assertThat(aggregationData).containsKey("activeChargers");
                    assertThat(aggregationData).containsKey("activeSessions");
                    assertThat(aggregationData).containsKey("totalPowerDelivery");
                    assertThat(aggregationData).containsKey("dailyRevenue");
                    assertThat(aggregationData).containsKey("avgUtilization");
                    
                    logger.info("✅ EV Charger aggregation verified - totalChargers: {}, activeSessions: {}", 
                               aggregationData.get("totalChargers"), aggregationData.get("activeSessions"));
                });
    }

    private void cleanupTestData() {
        logger.info("🧹 Cleaning up test data");
        
        // Clear devices (this will cascade to related data)
        bmsDevices.clear();
        solarDevices.clear(); 
        evChargerDevices.clear();
        
        try {
            // Delete in proper order to avoid foreign key constraint violations
            telemetryRepository.deleteAll();
            statusCacheRepository.deleteAll();
            deviceRepository.deleteAll();
            deviceTypeRepository.deleteAll();
            siteRepository.deleteAll();
        } catch (Exception e) {
            logger.warn("Error during cleanup: {}", e.getMessage());
        }
        
        logger.info("✅ Test cleanup complete");
    }
}