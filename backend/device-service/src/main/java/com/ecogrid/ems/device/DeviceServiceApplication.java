package com.ecogrid.ems.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EcoGrid EMS Device Management Service
 * 
 * Manages IoT devices, site configurations, and device communication.
 * Handles device registration, status monitoring, and command dispatch.
 */
@SpringBootApplication(scanBasePackages = {"com.ecogrid.ems.device", "com.ecogrid.ems.shared"})
@EnableScheduling
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}