package com.medilinktunisia.patientservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
public class DatabaseCreationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment environment = event.getEnvironment();
        String jdbcUrl = environment.getProperty("spring.datasource.url");

        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
            return;
        }

        String username = environment.getProperty("spring.datasource.username", "");
        String password = environment.getProperty("spring.datasource.password", "");

        try {
            String databaseName = extractDatabaseName(jdbcUrl);
            String adminJdbcUrl = buildAdminJdbcUrl(jdbcUrl);

            ensureDatabaseExists(adminJdbcUrl, username, password, databaseName);
        } catch (Exception exception) {
            log.warn("Impossible de vérifier/créer automatiquement la base PostgreSQL: {}", exception.getMessage());
        }
    }

    private void ensureDatabaseExists(String adminJdbcUrl, String username, String password, String databaseName) throws Exception {
        try (Connection connection = DriverManager.getConnection(adminJdbcUrl, username, password)) {
            if (databaseExists(connection, databaseName)) {
                log.info("Base PostgreSQL déjà présente: {}", databaseName);
                return;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + databaseName.replace("\"", "\"\"") + "\"");
                log.info("Base PostgreSQL créée automatiquement: {}", databaseName);
            }
        }
    }

    private boolean databaseExists(Connection connection, String databaseName) throws Exception {
        String sql = "SELECT 1 FROM pg_database WHERE datname = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String path = uri.getPath();

        if (path == null || path.length() <= 1) {
            throw new IllegalArgumentException("URL JDBC PostgreSQL invalide: " + jdbcUrl);
        }

        return path.substring(1);
    }

    private String buildAdminJdbcUrl(String jdbcUrl) {
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));

        URI adminUri = URI.create(String.format(
                "%s://%s%s%s/postgres%s",
                uri.getScheme(),
                uri.getHost(),
                uri.getPort() > 0 ? ":" + uri.getPort() : "",
                "",
                uri.getQuery() != null ? "?" + uri.getQuery() : ""
        ));

        return "jdbc:" + adminUri;
    }
}
