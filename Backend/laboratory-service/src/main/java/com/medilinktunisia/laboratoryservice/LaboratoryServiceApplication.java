package com.medilinktunisia.laboratoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Laboratory Service Application
 * Gestion des laboratoires d'analyses médicales et résultats
 * 
 * @author Smart Health Tunisia Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class LaboratoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaboratoryServiceApplication.class, args);
    }
}
