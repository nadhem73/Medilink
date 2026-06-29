package com.medilinktunisia.pharmacyservice;

import com.medilinktunisia.pharmacyservice.config.DatabaseCreationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Pharmacy Service Application
 * Gestion des pharmacies, stocks de médicaments et ordonnances électroniques
 *
 * @author Smart Health Tunisia Team
 * @version 1.0.0
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PharmacyServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(PharmacyServiceApplication.class);
        application.addListeners(new DatabaseCreationListener());
        application.run(args);
        log.info("Pharmacy Service started successfully");
    }
}
