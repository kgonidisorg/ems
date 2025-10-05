package com.ecogrid.ems.device.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Simple unit tests for DeviceStatusCache entity behavior without Spring context
 */
class DeviceStatusCacheSimpleUnitTest {

    private Site testSite;
    private DeviceType testDeviceType;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        // Create test site
        testSite = new Site();
        testSite.setId(1L);
        testSite.setName("Test Site");
        testSite.setDescription("Test Location");
        testSite.setCapacityMw(BigDecimal.valueOf(100.0));
        testSite.setStatus(Site.SiteStatus.ACTIVE);

        // Create test device type
        testDeviceType = new DeviceType();
        testDeviceType.setId(1L);
        testDeviceType.setName("BMS");
        testDeviceType.setCategory("ENERGY_STORAGE");

        // Create test device
        testDevice = new Device();
        testDevice.setId(1L);
        testDevice.setName("Test BMS Device");
        testDevice.setSerialNumber("TEST-BMS-001");
        testDevice.setManufacturer("Test Manufacturer");
        testDevice.setModel("Test Model");
        testDevice.setFirmwareVersion("1.0.0");
        testDevice.setStatus(Device.DeviceStatus.ONLINE);
        testDevice.setSite(testSite);
        testDevice.setDeviceType(testDeviceType);
        testDevice.setMqttTopic("ecogrid/sites/1/devices/TEST-BMS-001/telemetry");
    }

    @Test
    @DisplayName("Should create DeviceStatusCache with proper constructor and verify ID assignment")
    void testDeviceStatusCacheConstructor() {
        System.out.println("🔍 Testing DeviceStatusCache Constructor:");
        System.out.println("  Device ID: " + testDevice.getId());
        System.out.println("  Device ID is null: " + (testDevice.getId() == null));
        
        // When
        DeviceStatusCache statusCache = new DeviceStatusCache(testDevice, DeviceStatusCache.DeviceStatus.ONLINE);
        
        // Then
        System.out.println("  StatusCache Device ID after constructor: " + statusCache.getDeviceId());
        System.out.println("  StatusCache Device reference set: " + (statusCache.getDevice() != null));
        System.out.println("  StatusCache Device reference equals testDevice: " + (statusCache.getDevice() == testDevice));
        
        assertThat(statusCache.getDevice()).isSameAs(testDevice);
        assertThat(statusCache.getDeviceId()).isEqualTo(testDevice.getId());
        assertThat(statusCache.getStatus()).isEqualTo(DeviceStatusCache.DeviceStatus.ONLINE);
        
        System.out.println("✅ Constructor test passed - ID properly assigned");
    }

    @Test
    @DisplayName("Should handle null device ID gracefully")
    void testNullDeviceId() {
        System.out.println("🔍 Testing with null Device ID:");
        
        // Given - device with null ID
        Device deviceWithNullId = new Device();
        deviceWithNullId.setId(null);
        deviceWithNullId.setName("Device with null ID");
        
        System.out.println("  Device ID is null: " + (deviceWithNullId.getId() == null));
        
        // When
        DeviceStatusCache statusCache = new DeviceStatusCache(deviceWithNullId, DeviceStatusCache.DeviceStatus.ONLINE);
        
        // Then
        System.out.println("  StatusCache Device ID: " + statusCache.getDeviceId());
        System.out.println("  StatusCache Device reference set: " + (statusCache.getDevice() != null));
        
        assertThat(statusCache.getDevice()).isSameAs(deviceWithNullId);
        assertThat(statusCache.getDeviceId()).isNull();
        assertThat(statusCache.getStatus()).isEqualTo(DeviceStatusCache.DeviceStatus.ONLINE);
        
        System.out.println("✅ Null ID test passed - DeviceStatusCache handles null properly");
    }

    @Test
    @DisplayName("Should test manual ID setting behavior")
    void testManualIdSetting() {
        System.out.println("🔍 Testing manual ID setting:");
        
        // When - Create cache and manually set IDs
        DeviceStatusCache statusCache = new DeviceStatusCache();
        statusCache.setDeviceId(testDevice.getId());
        statusCache.setDevice(testDevice);
        statusCache.setStatus(DeviceStatusCache.DeviceStatus.ONLINE);
        statusCache.setLastSeen(LocalDateTime.now());
        statusCache.setAlertCount(0);

        System.out.println("  Device ID: " + testDevice.getId());
        System.out.println("  Manually set StatusCache Device ID: " + statusCache.getDeviceId());
        System.out.println("  StatusCache Device reference equals testDevice: " + (statusCache.getDevice() == testDevice));

        // Then
        assertThat(statusCache.getDeviceId()).isEqualTo(testDevice.getId());
        assertThat(statusCache.getDevice()).isSameAs(testDevice);
        assertThat(statusCache.getStatus()).isEqualTo(DeviceStatusCache.DeviceStatus.ONLINE);
        
        System.out.println("✅ Manual ID setting test passed");
    }

    @Test
    @DisplayName("Should test setDevice method behavior with @MapsId logic")
    void testSetDeviceMethod() {
        System.out.println("🔍 Testing setDevice method:");
        
        // Given
        DeviceStatusCache statusCache = new DeviceStatusCache();
        System.out.println("  Initial StatusCache Device ID: " + statusCache.getDeviceId());
        
        // When - Set device (this should trigger @MapsId-like behavior in the setter)
        statusCache.setDevice(testDevice);
        
        // Then
        System.out.println("  After setDevice - StatusCache Device ID: " + statusCache.getDeviceId());
        System.out.println("  After setDevice - Device reference set: " + (statusCache.getDevice() != null));
        
        assertThat(statusCache.getDevice()).isSameAs(testDevice);
        assertThat(statusCache.getDeviceId()).isEqualTo(testDevice.getId());
        
        System.out.println("✅ setDevice method test passed - ID automatically set");
    }

    @Test
    @DisplayName("Should test DeviceStatusCache with complete data")
    void testCompleteDeviceStatusCache() {
        System.out.println("🔍 Testing complete DeviceStatusCache setup:");
        
        // Given
        Map<String, Object> currentData = new HashMap<>();
        currentData.put("voltage", 48.5);
        currentData.put("current", 12.3);
        currentData.put("temperature", 25.0);
        currentData.put("soc", 85.0);
        
        LocalDateTime now = LocalDateTime.now();
        
        // When
        DeviceStatusCache statusCache = new DeviceStatusCache(testDevice, DeviceStatusCache.DeviceStatus.ONLINE);
        statusCache.setLastSeen(now);
        statusCache.setCurrentData(currentData);
        statusCache.setAlertCount(2);
        statusCache.setUptime24h(BigDecimal.valueOf(99.5));
        statusCache.setUpdatedAt(now);
        
        // Then
        System.out.println("  Device ID: " + statusCache.getDeviceId());
        System.out.println("  Status: " + statusCache.getStatus());
        System.out.println("  Alert Count: " + statusCache.getAlertCount());
        System.out.println("  Uptime 24h: " + statusCache.getUptime24h());
        System.out.println("  Current Data keys: " + statusCache.getCurrentData().keySet());
        
        assertThat(statusCache.getDeviceId()).isEqualTo(testDevice.getId());
        assertThat(statusCache.getStatus()).isEqualTo(DeviceStatusCache.DeviceStatus.ONLINE);
        assertThat(statusCache.getLastSeen()).isEqualTo(now);
        assertThat(statusCache.getCurrentData()).containsEntry("voltage", 48.5);
        assertThat(statusCache.getCurrentData()).containsEntry("soc", 85.0);
        assertThat(statusCache.getAlertCount()).isEqualTo(2);
        assertThat(statusCache.getUptime24h()).isEqualTo(BigDecimal.valueOf(99.5));
        assertThat(statusCache.getUpdatedAt()).isEqualTo(now);
        
        System.out.println("✅ Complete DeviceStatusCache test passed");
    }

    @Test
    @DisplayName("Should analyze @MapsId annotation behavior expectations")
    void testMapsIdExpectedBehavior() {
        System.out.println("🔍 Analyzing @MapsId expected behavior:");
        
        System.out.println("  📋 @MapsId Annotation Analysis:");
        System.out.println("    - @MapsId should make deviceId share the same value as device.getId()");
        System.out.println("    - When device is set, deviceId should automatically be populated");
        System.out.println("    - This creates a shared primary key relationship");
        System.out.println("    - Hibernate should handle the ID propagation during persistence");
        
        // Current behavior test
        DeviceStatusCache statusCache = new DeviceStatusCache(testDevice, DeviceStatusCache.DeviceStatus.ONLINE);
        
        System.out.println("  📊 Current Behavior:");
        System.out.println("    - Device ID: " + testDevice.getId());
        System.out.println("    - StatusCache Device ID: " + statusCache.getDeviceId());
        System.out.println("    - IDs match: " + (testDevice.getId().equals(statusCache.getDeviceId())));
        
        // The issue might be during Hibernate persistence, not in the entity itself
        System.out.println("  🚨 Potential Issue Analysis:");
        System.out.println("    - Entity-level ID assignment works correctly");
        System.out.println("    - Issue likely occurs during Hibernate persistence");
        System.out.println("    - @MapsId might not be working with current Hibernate version");
        System.out.println("    - Or there might be transaction/session management issues");
        
        assertThat(statusCache.getDeviceId()).isEqualTo(testDevice.getId());
        System.out.println("✅ Entity-level @MapsId behavior works correctly");
    }
}