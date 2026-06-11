package com.medilinktunisia.apigateway.controller;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur principal de l'API Gateway
 * Fournit des informations sur l'état de la gateway et des services
 */
@RestController
@RequestMapping("/")
public class GatewayController {

    private final DiscoveryClient discoveryClient;

    public GatewayController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Smart Health Tunisia API Gateway");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("description", "Central entry point for all Smart Health Tunisia microservices");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("gateway", "healthy");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getRegisteredServices() {
        List<String> services = discoveryClient.getServices();
        Map<String, Object> response = new HashMap<>();
        
        response.put("total", services.size());
        response.put("services", services);
        
        Map<String, List<ServiceInstance>> servicesWithInstances = new HashMap<>();
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            servicesWithInstances.put(service, instances);
        }
        response.put("instances", servicesWithInstances);
        
        return ResponseEntity.ok(response);
    }
}
