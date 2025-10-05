package com.ecogrid.ems.device.integration;

import com.ecogrid.ems.device.DeviceServiceApplication;
import com.ecogrid.ems.device.config.IntegrationTestConfiguration;
import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.DeviceType;
import com.ecogrid.ems.device.entity.Site;
import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.DeviceTypeRepository;
import com.ecogrid.ems.device.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic integration test for device service infrastructure
 * Tests the Spring Boot context, database connectivity, and basic entity operations
 */
@SpringBootTest(classes = DeviceServiceApplication.class)
@Import(IntegrationTestConfiguration.class)
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"device.telemetry", "device.status", "device.alerts"})
@ActiveProfiles("test")
@DirtiesContext
class BasicDeviceServiceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(BasicDeviceServiceIntegrationTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ems_basic_test")
            .withUsername("ems_test_user")
            .withPassword("ems_test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private SiteRepository siteRepository;

    private Site testSite;
    private DeviceType bmsDeviceType;

    @BeforeEach
    @Transactional
    void setUp() {
        logger.info("🚀 Setting up basic integration test...");
        
        // Create DeviceType
        bmsDeviceType = new DeviceType();
        bmsDeviceType.setName("BMS");
        bmsDeviceType.setCategory("STORAGE");
        
        Map<String, Object> telemetrySchema = new HashMap<>();
        telemetrySchema.put("voltage", "number");
        telemetrySchema.put("current", "number");
        telemetrySchema.put("temperature", "number");
        telemetrySchema.put("soc", "number");
        bmsDeviceType.setTelemetrySchema(telemetrySchema);
        
        bmsDeviceType = deviceTypeRepository.save(bmsDeviceType);
        
        // Create Site
        testSite = new Site();
        testSite.setName("Test Site");
        testSite.setDescription("Test Location");
        testSite.setTimezone("UTC");
        testSite = siteRepository.save(testSite);
        
        logger.info("✅ Test setup complete - Site: {}, DeviceType: {}", testSite.getName(), bmsDeviceType.getName());
    }

    @Test
    @DisplayName("Should start Spring Boot context and connect to database")
    void shouldStartContextAndConnectToDatabase() {
        logger.info("🧪 Testing Spring Boot context and database connectivity...");

        // Verify repositories are available
        assertThat(deviceRepository).isNotNull();
        assertThat(deviceTypeRepository).isNotNull();
        assertThat(siteRepository).isNotNull();
        
        // Verify database connectivity by counting records
        long siteCount = siteRepository.count();
        long deviceTypeCount = deviceTypeRepository.count();
        
        assertThat(siteCount).isPositive();
        assertThat(deviceTypeCount).isPositive();
        
        logger.info("✅ Context and database test successful - Sites: {}, DeviceTypes: {}", siteCount, deviceTypeCount);
    }

    @Test
    @DisplayName("Should create and persist device entities")
    @Transactional
    void shouldCreateAndPersistDeviceEntities() {
        logger.info("🧪 Testing device entity creation and persistence...");

        // Create a test device
        Device testDevice = new Device();
        testDevice.setSerialNumber("TEST-BMS-BASIC-001");
        testDevice.setName("Test BMS Device");
        testDevice.setDescription("Basic integration test device");
        testDevice.setDeviceType(bmsDeviceType);
        testDevice.setModel("TestBMS-v1");
        testDevice.setManufacturer("TestManufacturer");
        testDevice.setFirmwareVersion("1.0.0");
        testDevice.setStatus(Device.DeviceStatus.OFFLINE);
        testDevice.setRatedPowerKw(BigDecimal.valueOf(10.0));
        testDevice.setSite(testSite);
        testDevice.setMqttTopic(String.format("sites/%d/devices/%s/telemetry", testSite.getId(), testDevice.getSerialNumber()));
        
        // Save device
        Device savedDevice = deviceRepository.save(testDevice);
        
        // Verify persistence
        assertThat(savedDevice.getId()).isNotNull();
        assertThat(savedDevice.getSerialNumber()).isEqualTo("TEST-BMS-BASIC-001");
        assertThat(savedDevice.getDeviceType().getName()).isEqualTo("BMS");
        assertThat(savedDevice.getSite().getName()).isEqualTo("Test Site");
        
        // Verify retrieval
        Device retrievedDevice = deviceRepository.findBySerialNumber("TEST-BMS-BASIC-001").orElse(null);
        assertThat(retrievedDevice).isNotNull();
        assertThat(retrievedDevice.getName()).isEqualTo("Test BMS Device");
        
        logger.info("✅ Device entity persistence test successful - Device ID: {}", savedDevice.getId());
    }

    @Test
    @DisplayName("Should handle DeviceType entity relationships")
    @Transactional
    void shouldHandleDeviceTypeRelationships() {
        logger.info("🧪 Testing DeviceType entity relationships...");

        // Create multiple devices with the same type
        Device device1 = createTestDevice("DEVICE-001");
        Device device2 = createTestDevice("DEVICE-002");
        
        deviceRepository.save(device1);
        deviceRepository.save(device2);
        
        // Test repository method that uses DeviceType entity
        long deviceCount = deviceRepository.countByDeviceType(bmsDeviceType);
        assertThat(deviceCount).isEqualTo(2);
        
        // Test finding devices by type
        var devicesByType = deviceRepository.findByDeviceType(bmsDeviceType);
        assertThat(devicesByType).hasSize(2);
        
        logger.info("✅ DeviceType relationship test successful - Found {} devices of type {}", deviceCount, bmsDeviceType.getName());
    }

    private Device createTestDevice(String serialNumber) {
        Device device = new Device();
        device.setSerialNumber(serialNumber);
        device.setName("Test Device " + serialNumber);
        device.setDescription("Test device for integration testing");
        device.setDeviceType(bmsDeviceType);
        device.setModel("TestBMS-v1");
        device.setManufacturer("TestManufacturer");
        device.setFirmwareVersion("1.0.0");
        device.setStatus(Device.DeviceStatus.OFFLINE);
        device.setRatedPowerKw(BigDecimal.valueOf(10.0));
        device.setSite(testSite);
        device.setMqttTopic(String.format("sites/%d/devices/%s/telemetry", testSite.getId(), serialNumber));
        return device;
    }
}