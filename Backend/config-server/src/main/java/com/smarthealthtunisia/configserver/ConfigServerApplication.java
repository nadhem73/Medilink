package com.smarthealthtunisia.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application - Centralized Configuration Management
 * Gère toutes les configurations pour les microservices
 * Smart Health Tunisia
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        System.out.println("""
            
            ========================================
            🚀 Config Server Started Successfully
            ========================================
            📍 Config Server URL: http://localhost:8888
            🔒 Security: Enabled
            📁 Config Location: Native/Git
            📊 Actuator: /actuator
            ========================================
            ✅ Ready to serve configurations
            ========================================
            """);
    }
}
