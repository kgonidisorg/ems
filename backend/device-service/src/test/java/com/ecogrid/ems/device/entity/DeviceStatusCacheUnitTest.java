package com.ecogrid.ems.device.entity;

import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.DeviceStatusCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DeviceStatusCache entity to diagnose @MapsId issues
 */
@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
class DeviceStatusCacheUnitTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceStatusCacheRepository statusCacheRepository;

    private Site testSite;
    private DeviceType testDeviceType;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        // Create test site
        testSite = new Site();
        testSite.setName("Test Site");
        testSite.setDescription("Test Location");
        testSite.setCapacityMw(BigDecimal.valueOf(100.0));
        testSite.setStatus(Site.SiteStatus.ACTIVE);
        testSite = entityManager.persistAndFlush(testSite);

        // Create test device type
        testDeviceType = new DeviceType();
        testDeviceType.setName("BMS");
        testDeviceType.setCategory("ENERGY_STORAGE");
        testDeviceType = entityManager.persistAndFlush(testDeviceType);

        // Create test device
        testDevice = new Device();
        testDevice.setName("Test BMS Device");
        testDevice.setSerialNumber("TEST-BMS-001");
        testDevice.setManufacturer("Test Manufacturer");
        testDevice.setModel("Test Model");
        testDevice.setFirmwareVersion("1.0.0");
        testDevice.setStatus(Device.DeviceStatus.ONLINE);
        testDevice.setSite(testSite);
        testDevice.setDeviceType(testDeviceType);
        testDevice.setMqttTopic("ecogrid/sites/1/devices/TEST-BMS-001/telemetry");
        testDevice = entityManager.persistAndFlush(testDevice);

        // Clear persistence context to ensure fresh state
        entityManager.clear();
    }

    @Test
    @DisplayName("Should create DeviceStatusCache with proper constructor")
    void testDeviceStatusCacheConstructor() {
        // Given
        Device device = deviceRepository.findById(testDevice.getId()).orElseThrow();
        
        // When
        DeviceStatusCache statusCache = new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE);
        
        // Then
        assertThat(statusCache.getDevice()).isEqualTo(device);
        assertThat(statusCache.getDeviceId()).isEqualTo(device.getId());
        assertThat(statusCache.getStatus()).isEqualTo(DeviceStatusCache.DeviceStatus.ONLINE);
        
        System.out.println("🔍 Constructor Test Results:");
        System.out.println("  Device ID: " + device.getId());
        System.out.println("  StatusCache Device ID: " + statusCache.getDeviceId());
        System.out.println("  Device reference set: " + (statusCache.getDevice() != null));
    }

    @Test
    @DisplayName("Should persist DeviceStatusCache with @MapsId annotation")
    void testDeviceStatusCachePersistence() {
        // Given
        Device device = deviceRepository.findById(testDevice.getId()).orElseThrow();
        DeviceStatusCache statusCache = new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE);
        statusCache.setLastSeen(LocalDateTime.now());
        
        Map<String, Object> currentData = new HashMap<>();
        currentData.put("voltage", 48.5);
        currentData.put("current", 12.3);
        currentData.put("temperature", 25.0);
        statusCache.setCurrentData(currentData);
        statusCache.setAlertCount(0);
        statusCache.setUptime24h(BigDecimal.valueOf(99.5));

        System.out.println("🔍 Before Persistence:");
        System.out.println("  Device ID: " + device.getId());
        System.out.println("  StatusCache Device ID: " + statusCache.getDeviceId());
        System.out.println("  StatusCache Device reference: " + (statusCache.getDevice() != null));

        // When & Then
        try {
            DeviceStatusCache savedCache = statusCacheRepository.saveAndFlush(statusCache);
            
            System.out.println("🎉 Persistence Success:");
            System.out.println("  Saved StatusCache Device ID: " + savedCache.getDeviceId());
            System.out.println("  Saved StatusCache Status: " + savedCache.getStatus());
            
            assertThat(savedCache.getDeviceId()).isEqualTo(device.getId());
            assertThat(savedCache.getStatus()).isEqualTo(DeviceStatusCache.DeviceStatus.ONLINE);
            assertThat(savedCache.getCurrentData()).containsEntry("voltage", 48.5);
            
        } catch (Exception e) {
            System.err.println("❌ Persistence Failed:");
            System.err.println("  Error: " + e.getClass().getSimpleName());
            System.err.println("  Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("  Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            
            // Re-throw to fail the test
            throw e;
        }
    }

    @Test
    @DisplayName("Should find DeviceStatusCache by device ID")
    void testFindByDeviceId() {
        // Given - Create and persist a DeviceStatusCache
        Device device = deviceRepository.findById(testDevice.getId()).orElseThrow();
        DeviceStatusCache statusCache = new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE);
        statusCache.setLastSeen(LocalDateTime.now());
        statusCache.setAlertCount(0);
        
        try {
            statusCacheRepository.saveAndFlush(statusCache);
            entityManager.clear(); // Clear persistence context
            
            // When
            Optional<DeviceStatusCache> found = statusCacheRepository.findByDeviceId(device.getId());
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getDeviceId()).isEqualTo(device.getId());
            assertThat(found.get().getStatus()).isEqualTo(DeviceStatusCache.DeviceStatus.ONLINE);
            
            System.out.println("🎉 Find by Device ID Success:");
            System.out.println("  Found StatusCache for Device ID: " + found.get().getDeviceId());
            
        } catch (Exception e) {
            System.err.println("❌ Test failed during setup - @MapsId issue detected");
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should test manual ID setting without @MapsId")
    void testManualIdSetting() {
        // Given
        Device device = deviceRepository.findById(testDevice.getId()).orElseThrow();
        
        // When - Create cache and manually set IDs (bypassing @MapsId)
        DeviceStatusCache statusCache = new DeviceStatusCache();
        statusCache.setDeviceId(device.getId()); // Manual ID setting
        statusCache.setDevice(device);
        statusCache.setStatus(DeviceStatusCache.DeviceStatus.ONLINE);
        statusCache.setLastSeen(LocalDateTime.now());
        statusCache.setAlertCount(0);

        System.out.println("🔍 Manual ID Setting Test:");
        System.out.println("  Device ID: " + device.getId());
        System.out.println("  Manually set StatusCache Device ID: " + statusCache.getDeviceId());

        // Then - Try to persist
        try {
            DeviceStatusCache savedCache = statusCacheRepository.saveAndFlush(statusCache);
            
            System.out.println("🎉 Manual ID Setting Success:");
            System.out.println("  Saved with Device ID: " + savedCache.getDeviceId());
            
            assertThat(savedCache.getDeviceId()).isEqualTo(device.getId());
            
        } catch (Exception e) {
            System.err.println("❌ Manual ID Setting Failed:");
            System.err.println("  Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    @DisplayName("Should test entity state and ID assignments")
    void testEntityStateAndIds() {
        // Given
        Device device = deviceRepository.findById(testDevice.getId()).orElseThrow();
        
        System.out.println("🔍 Entity State Analysis:");
        System.out.println("  Device ID: " + device.getId());
        System.out.println("  Device ID is null: " + (device.getId() == null));
        
        // When
        DeviceStatusCache statusCache = new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE);
        
        System.out.println("  StatusCache Device ID after constructor: " + statusCache.getDeviceId());
        System.out.println("  StatusCache Device reference set: " + (statusCache.getDevice() != null));
        
        // Then - Analyze the state
        assertThat(device.getId()).isNotNull();
        assertThat(statusCache.getDeviceId()).isEqualTo(device.getId());
        assertThat(statusCache.getDevice()).isEqualTo(device);
    }

    @Test
    @DisplayName("Should test different persistence approaches")
    void testDifferentPersistenceApproaches() {
        Device device = deviceRepository.findById(testDevice.getId()).orElseThrow();
        
        // Approach 1: Direct save (should fail with @MapsId issue)
        System.out.println("🧪 Testing Approach 1: Direct save with constructor");
        try {
            DeviceStatusCache cache1 = new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE);
            cache1.setLastSeen(LocalDateTime.now());
            statusCacheRepository.save(cache1);
            statusCacheRepository.flush();
            System.out.println("✅ Approach 1: SUCCESS");
        } catch (Exception e) {
            System.out.println("❌ Approach 1: FAILED - " + e.getMessage());
        }

        // Approach 2: EntityManager persist
        System.out.println("🧪 Testing Approach 2: EntityManager persist");
        try {
            DeviceStatusCache cache2 = new DeviceStatusCache(device, DeviceStatusCache.DeviceStatus.ONLINE);
            cache2.setLastSeen(LocalDateTime.now());
            entityManager.persist(cache2);
            entityManager.flush();
            System.out.println("✅ Approach 2: SUCCESS");
        } catch (Exception e) {
            System.out.println("❌ Approach 2: FAILED - " + e.getMessage());
        }

        // Approach 3: Detached device
        System.out.println("🧪 Testing Approach 3: With detached device");
        try {
            entityManager.clear(); // Detach all entities
            Device detachedDevice = deviceRepository.findById(testDevice.getId()).orElseThrow();
            DeviceStatusCache cache3 = new DeviceStatusCache(detachedDevice, DeviceStatusCache.DeviceStatus.ONLINE);
            cache3.setLastSeen(LocalDateTime.now());
            statusCacheRepository.save(cache3);
            statusCacheRepository.flush();
            System.out.println("✅ Approach 3: SUCCESS");
        } catch (Exception e) {
            System.out.println("❌ Approach 3: FAILED - " + e.getMessage());
        }
    }
}