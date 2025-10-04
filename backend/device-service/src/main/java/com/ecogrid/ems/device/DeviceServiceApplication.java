package com.ecogrid.ems.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EcoGrid EMS Device Management Service
 * 
 * Manages IoT devices, site configurations, and device communication.
 * Handles device registration, status monitoring, and command dispatch.
 */
@SpringBootApplication
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}