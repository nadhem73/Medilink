package com.medilinktunisia.eurekaservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Service Application - Service Discovery
 * Point central d'enregistrement et de découverte de tous les microservices
 * Smart Health Tunisia
 */
@Slf4j
@SpringBootApplication
@EnableEurekaServer
public class EurekaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServiceApplication.class, args);
        log.info("Eureka Service started successfully");
    }
}
