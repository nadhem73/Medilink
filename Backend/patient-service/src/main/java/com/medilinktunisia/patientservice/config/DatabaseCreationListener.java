package com.medilinktunisia.patientservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Crée automatiquement la base PostgreSQL du service si elle n'existe pas,
 * avant l'initialisation du DataSource. Évite l'erreur "database does not exist"
 * au premier démarrage.
 */
@Slf4j
public class DatabaseCreationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            return;
        }

        // Sépare l'URL serveur du nom de base : jdbc:postgresql://host:port/dbName
        int lastSlash = url.lastIndexOf('/');
        String dbName = url.substring(lastSlash + 1);
        String serverUrl = url.substring(0, lastSlash + 1) + "postgres";

        try (Connection connection = DriverManager.getConnection(serverUrl, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
            if (!rs.next()) {
                statement.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                log.info("Database '{}' created successfully", dbName);
            }
        } catch (Exception e) {
            log.error("Failed to create database '{}': {}", dbName, e.getMessage());
        }
    }
}
