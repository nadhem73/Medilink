package com.medilinktunisia.eurekaservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration personnalisée pour Eureka Server
 * Permet des configurations spécifiques par environnement
 */
@Configuration
public class EurekaConfig {

    /**
     * Configuration pour l'environnement de développement
     */
    @Configuration
    @Profile("dev")
    public static class DevConfig {
        // Configuration spécifique au développement
        // Self-preservation désactivé pour faciliter les tests
    }

    /**
     * Configuration pour l'environnement de production
     */
    @Configuration
    @Profile("prod")
    public static class ProdConfig {
        // Configuration spécifique à la production
        // Self-preservation activé pour la haute disponibilité
    }

    /**
     * Configuration pour l'environnement de test
     */
    @Configuration
    @Profile("test")
    public static class TestConfig {
        // Configuration spécifique aux tests
    }
}
