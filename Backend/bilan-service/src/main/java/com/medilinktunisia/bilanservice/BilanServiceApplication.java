package com.medilinktunisia.bilanservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BilanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BilanServiceApplication.class, args);
        log.info("Bilan Service started successfully");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
