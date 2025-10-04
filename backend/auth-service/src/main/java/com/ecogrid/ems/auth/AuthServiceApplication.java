package com.ecogrid.ems.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EcoGrid EMS Authentication Service
 * 
 * Provides JWT-based authentication and authorization for the EMS system.
 * Handles user registration, login, role management, and token validation.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}