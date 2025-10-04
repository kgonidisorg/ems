package com.ecogrid.ems.device.controller;

import com.ecogrid.ems.device.dto.DeviceRequest;
import com.ecogrid.ems.device.dto.DeviceResponse;
import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DeviceController
 */
@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private DeviceRequest deviceRequest;
    private DeviceResponse deviceResponse;

    @BeforeEach
    void setUp() {
        deviceRequest = new DeviceRequest(
                "DEV-001",
                "Test Solar Inverter",
                "High-efficiency solar inverter for testing",
                "SOLAR_INVERTER",
                "SP-2000X",
                "SolarTech Inc",
                "v1.2.3",
                "ONLINE",
                new BigDecimal("5.5"),
                "device/DEV-001/telemetry",
                "192.168.1.100",
                "AA:BB:CC:DD:EE:FF",
                LocalDateTime.now(),
                1L,
                null,
                null
        );

        deviceResponse = new DeviceResponse(
                1L,
                "DEV-001",
                "Test Solar Inverter",
                "High-efficiency solar inverter for testing",
                "SOLAR_INVERTER",
                "SP-2000X",
                "SolarTech Inc",
                "v1.2.3",
                "ONLINE",
                new BigDecimal("5.5"),
                "device/DEV-001/telemetry",
                "192.168.1.100",
                "AA:BB:CC:DD:EE:FF",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L,
                "Test Site",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void createDevice_ValidRequest_ShouldReturnCreatedDevice() throws Exception {
        // Arrange
        when(deviceService.createDevice(any(DeviceRequest.class))).thenReturn(deviceResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNumber").value("DEV-001"))
                .andExpect(jsonPath("$.name").value("Test Solar Inverter"))
                .andExpect(jsonPath("$.deviceType").value("SOLAR_INVERTER"));

        verify(deviceService).createDevice(any(DeviceRequest.class));
    }

    @Test
    void createDevice_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(deviceService.createDevice(any(DeviceRequest.class)))
                .thenThrow(new IllegalArgumentException("Serial number already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Serial number already exists"));

        verify(deviceService).createDevice(any(DeviceRequest.class));
    }

    @Test
    void createDevice_ServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(deviceService.createDevice(any(DeviceRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Failed to create device"));

        verify(deviceService).createDevice(any(DeviceRequest.class));
    }

    @Test
    void getDeviceById_ExistingDevice_ShouldReturnDevice() throws Exception {
        // Arrange
        when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(deviceResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNumber").value("DEV-001"))
                .andExpect(jsonPath("$.name").value("Test Solar Inverter"));

        verify(deviceService).getDeviceById(1L);
    }

    @Test
    void getDeviceById_NonExistingDevice_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(deviceService.getDeviceById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/999"))
                .andExpect(status().isNotFound());

        verify(deviceService).getDeviceById(999L);
    }

    @Test
    void getDeviceById_ServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(deviceService.getDeviceById(1L))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Failed to retrieve device"));

        verify(deviceService).getDeviceById(1L);
    }

    @Test
    void getDeviceBySerialNumber_ExistingDevice_ShouldReturnDevice() throws Exception {
        // Arrange
        when(deviceService.getDeviceBySerialNumber("DEV-001")).thenReturn(Optional.of(deviceResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/serial/DEV-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serialNumber").value("DEV-001"))
                .andExpect(jsonPath("$.name").value("Test Solar Inverter"));

        verify(deviceService).getDeviceBySerialNumber("DEV-001");
    }

    @Test
    void getDeviceBySerialNumber_NonExistingDevice_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(deviceService.getDeviceBySerialNumber("NON-EXIST")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/serial/NON-EXIST"))
                .andExpect(status().isNotFound());

        verify(deviceService).getDeviceBySerialNumber("NON-EXIST");
    }

    @Test
    void getAllDevices_WithPagination_ShouldReturnPagedDevices() throws Exception {
        // Arrange
        List<DeviceResponse> devices = Arrays.asList(deviceResponse);
        Page<DeviceResponse> devicePage = new PageImpl<>(devices, PageRequest.of(0, 10), 1);
        when(deviceService.getAllDevices(any(PageRequest.class))).thenReturn(devicePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(deviceService).getAllDevices(any(PageRequest.class));
    }

    @Test
    void getDevicesBySite_ExistingSite_ShouldReturnDevices() throws Exception {
        // Arrange
        List<DeviceResponse> devices = Arrays.asList(deviceResponse);
        when(deviceService.getDevicesBySite(1L)).thenReturn(devices);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/site/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].siteId").value(1));

        verify(deviceService).getDevicesBySite(1L);
    }

    @Test
    void updateDevice_ValidRequest_ShouldReturnUpdatedDevice() throws Exception {
        // Arrange
        DeviceResponse updatedResponse = new DeviceResponse(
                1L, "DEV-001", "Updated Solar Inverter", "Updated description",
                "SOLAR_INVERTER", "SP-2000X", "SolarTech Inc", "v1.2.4",
                "ONLINE", new BigDecimal("5.5"), "device/DEV-001/telemetry",
                "192.168.1.100", "AA:BB:CC:DD:EE:FF", LocalDateTime.now(),
                LocalDateTime.now(), LocalDateTime.now(), 1L, "Test Site", null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(deviceService.updateDevice(eq(1L), any(DeviceRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Solar Inverter"));

        verify(deviceService).updateDevice(eq(1L), any(DeviceRequest.class));
    }

    @Test
    void updateDevice_NonExistingDevice_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(deviceService.updateDevice(eq(999L), any(DeviceRequest.class)))
                .thenThrow(new IllegalArgumentException("Device not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/devices/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Device not found"));

        verify(deviceService).updateDevice(eq(999L), any(DeviceRequest.class));
    }

    @Test
    void deleteDevice_ExistingDevice_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(deviceService).deleteDevice(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/devices/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Device deleted successfully"));

        verify(deviceService).deleteDevice(1L);
    }

    @Test
    void deleteDevice_NonExistingDevice_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Device not found"))
                .when(deviceService).deleteDevice(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/devices/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Device not found"));

        verify(deviceService).deleteDevice(999L);
    }

    @Test
    void updateDeviceStatus_ValidRequest_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(deviceService).updateDeviceStatus(eq(1L), any(Device.DeviceStatus.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/devices/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"OFFLINE\"}"))
                .andExpect(status().isOk());

        verify(deviceService).updateDeviceStatus(eq(1L), eq(Device.DeviceStatus.OFFLINE));
    }

    @Test
    void getDevicesByStatus_ShouldReturnDevicesWithSpecificStatus() throws Exception {
        // Arrange
        List<DeviceResponse> onlineDevices = Arrays.asList(deviceResponse);
        when(deviceService.getDevicesByStatus(Device.DeviceStatus.ONLINE)).thenReturn(onlineDevices);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/status/ONLINE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ONLINE"));

        verify(deviceService).getDevicesByStatus(Device.DeviceStatus.ONLINE);
    }

    @Test
    void getDevicesByType_ShouldReturnDevicesOfSpecificType() throws Exception {
        // Arrange
        List<DeviceResponse> solarInverters = Arrays.asList(deviceResponse);
        when(deviceService.getDevicesByType(Device.DeviceType.SOLAR_INVERTER)).thenReturn(solarInverters);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/type/SOLAR_INVERTER"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].deviceType").value("SOLAR_INVERTER"));

        verify(deviceService).getDevicesByType(Device.DeviceType.SOLAR_INVERTER);
    }
}