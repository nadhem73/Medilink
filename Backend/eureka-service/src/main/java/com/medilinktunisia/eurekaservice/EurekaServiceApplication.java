package com.medilinktunisia.eurekaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Service Application - Service Discovery
 * Point central d'enregistrement et de découverte de tous les microservices
 * Smart Health Tunisia
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServiceApplication.class, args);
        System.out.println("""
            
            ========================================
            🚀 Eureka Service Started Successfully
            ========================================
            📍 Eureka Dashboard: http://localhost:8761
            👤 Username: admin
            🔑 Password: admin123
            ========================================
            ✅ Ready to accept service registrations
            ========================================
            """);
    }
}
