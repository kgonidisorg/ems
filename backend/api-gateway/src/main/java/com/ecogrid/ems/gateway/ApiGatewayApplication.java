package com.ecogrid.ems.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EcoGrid EMS API Gateway
 * 
 * Single entry point for all client requests to EMS microservices.
 * Provides routing, authentication, rate limiting, and request/response
 * Updated with optimized Docker build caching.
 * transformation.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}