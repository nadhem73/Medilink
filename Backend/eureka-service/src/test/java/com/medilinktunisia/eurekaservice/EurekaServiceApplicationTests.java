package com.medilinktunisia.eurekaservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EurekaServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test que le contexte Spring se charge correctement
    }

    @Test
    void eurekaServerStarts() {
        // Test que le serveur Eureka démarre
    }
}
