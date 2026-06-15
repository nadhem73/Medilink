package com.medilinktunisia.doctorservice;

import com.medilinktunisia.doctorservice.config.DatabaseCreationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DoctorServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorServiceApplication.class);
        application.addListeners(new DatabaseCreationListener());
        application.run(args);
    }
}
