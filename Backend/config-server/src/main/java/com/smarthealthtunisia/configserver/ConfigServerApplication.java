package com.smarthealthtunisia.configserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application - Centralized Configuration Management
 * Gère toutes les configurations pour les microservices
 * Smart Health Tunisia
 */
@Slf4j
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        log.info("Config Server started successfully");
    }
}
