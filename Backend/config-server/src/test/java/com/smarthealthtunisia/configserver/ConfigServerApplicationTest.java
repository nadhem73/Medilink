package com.smarthealthtunisia.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("native")
class ConfigServerApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainStarts() {
        ConfigServerApplication.main(new String[]{});
    }
}
