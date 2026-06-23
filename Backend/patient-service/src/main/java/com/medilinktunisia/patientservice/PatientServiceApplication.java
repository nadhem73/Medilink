package com.medilinktunisia.patientservice;

import com.medilinktunisia.patientservice.config.DatabaseCreationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Slf4j
public class PatientServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(PatientServiceApplication.class);
        application.addListeners(new DatabaseCreationListener());
        application.run(args);
        log.info("Patient-Service started on port 8082");
    }
}
