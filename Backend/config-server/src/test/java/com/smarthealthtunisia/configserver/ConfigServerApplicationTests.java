package com.smarthealthtunisia.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("native")
class ConfigServerApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
