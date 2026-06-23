package com.smarthealthtunisia.configserver.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("native")
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void securityBeansShouldLoad() {
        assertThat(securityFilterChain).isNotNull();
        assertThat(userDetailsService).isNotNull();
        assertThat(passwordEncoder).isNotNull();
    }
}
