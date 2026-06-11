package com.medilinktunisia.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application - Central Entry Point
 * Point d'entrée unique pour tous les microservices
 * Smart Health Tunisia
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("""
            
            ========================================
            🚀 API Gateway Started Successfully
            ========================================
            📍 Gateway URL: http://localhost:8080
            🔒 Security: Enabled (JWT)
            🔄 Service Discovery: Connected
            📊 Actuator: /actuator
            ========================================
            ✅ Ready to route requests
            ========================================
            """);
    }
}
