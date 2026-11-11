package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.SuspiciousIpDto;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lit les données réelles de la plateforme directement dans les bases des
 * autres services (même serveur PostgreSQL) :
 *  - medilink_auth.security_events / users : sécurité, sessions, comptes
 *  - medilink_bilan.bilans : documents traités par OCR
 * En cas d'erreur de lecture, les compteurs valent 0 (jamais de valeur inventée).
 */
@Service
@Slf4j
public class PlatformStatsService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String authDbUrl;
    private final String bilanDbUrl;
    private final String user;
    private final String password;
    private final int sessionMinutes;
    private final int suspiciousThreshold;

    public PlatformStatsService(
            @Value("${monitoring-sources.auth-db-url}") String authDbUrl,
            @Value("${monitoring-sources.bilan-db-url}") String bilanDbUrl,
            @Value("${monitoring-sources.db-username}") String user,
            @Value("${monitoring-sources.db-password}") String password,
            @Value("${monitoring-sources.session-minutes:15}") int sessionMinutes,
            @Value("${monitoring-sources.suspicious-failures-threshold:5}") int suspiciousThreshold) {
        this.authDbUrl = authDbUrl;
        this.bilanDbUrl = bilanDbUrl;
        this.user = user;
        this.password = password;
        this.sessionMinutes = sessionMinutes;
        this.suspiciousThreshold = suspiciousThreshold;
    }

    // ── Sécurité ───────────────────────────────────────────────────────────

    @Getter
    @Builder
    public static class SecurityStats {
        private long loginAttemptsToday;
        private long failedLoginsToday;
        private long suspendedAccounts;
        private long expiredJwtToday;
        private long activeSessions;
        private long expiredSessionsToday;
        private List<SuspiciousIpDto> suspiciousIps;
    }

    public SecurityStats securityStats() {
        LocalDateTime midnight = LocalDate.now().atStartOfDay();
        LocalDateTime sessionCutoff = LocalDateTime.now().minusMinutes(sessionMinutes);
        try (Connection c = auth()) {
            long attempts = count(c, "SELECT count(*) FROM security_events WHERE type IN ('LOGIN_SUCCESS','LOGIN_FAILURE') AND created_at >= ?", midnight);
            long failed = count(c, "SELECT count(*) FROM security_events WHERE type = 'LOGIN_FAILURE' AND created_at >= ?", midnight);
            long expiredJwt = count(c, "SELECT count(*) FROM security_events WHERE type = 'JWT_EXPIRED' AND created_at >= ?", midnight);
            // Session active = utilisateur ayant obtenu un access token encore valide
            long active = count(c, "SELECT count(DISTINCT identifier) FROM security_events WHERE type IN ('LOGIN_SUCCESS','TOKEN_REFRESH') AND created_at >= ?", sessionCutoff);
            // Session expirée aujourd'hui = dernier token obtenu aujourd'hui mais plus valide
            long expiredSessions = count(c,
                    "SELECT count(*) FROM (SELECT identifier, max(created_at) AS last_at FROM security_events " +
                    "WHERE type IN ('LOGIN_SUCCESS','TOKEN_REFRESH') AND created_at >= ? GROUP BY identifier) t WHERE t.last_at < ?",
                    midnight, sessionCutoff);
            long suspended;
            try (PreparedStatement ps = c.prepareStatement("SELECT count(*) FROM users WHERE status = 'SUSPENDED'");
                 ResultSet rs = ps.executeQuery()) {
                suspended = rs.next() ? rs.getLong(1) : 0;
            }
            return SecurityStats.builder()
                    .loginAttemptsToday(attempts)
                    .failedLoginsToday(failed)
                    .expiredJwtToday(expiredJwt)
                    .activeSessions(active)
                    .expiredSessionsToday(expiredSessions)
                    .suspendedAccounts(suspended)
                    .suspiciousIps(suspiciousIps(c))
                    .build();
        } catch (SQLException e) {
            log.warn("Security stats unavailable: {}", e.getMessage());
            return SecurityStats.builder().suspiciousIps(List.of()).build();
        }
    }

    /** IPs ayant au moins N échecs de connexion sur les dernières 24 h. */
    private List<SuspiciousIpDto> suspiciousIps(Connection c) throws SQLException {
        List<SuspiciousIpDto> result = new ArrayList<>();
        String sql = "SELECT ip_address, count(*) AS n, max(created_at) AS last_at FROM security_events " +
                "WHERE type = 'LOGIN_FAILURE' AND created_at >= ? AND ip_address IS NOT NULL " +
                "GROUP BY ip_address HAVING count(*) >= ? ORDER BY n DESC LIMIT 10";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusHours(24)));
            ps.setInt(2, suspiciousThreshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ip = rs.getString(1);
                    result.add(SuspiciousIpDto.builder()
                            .ip(ip)
                            .attempts(rs.getLong(2))
                            .lastAttempt(rs.getTimestamp(3).toLocalDateTime().format(TS))
                            .location(isPrivateIp(ip) ? "Réseau local" : "Externe")
                            .build());
                }
            }
        }
        return result;
    }

    @Getter
    @Builder
    public static class SecurityEventRow {
        private long id;
        private String type;
        private String identifier;
        private String ipAddress;
        private String detail;
        private LocalDateTime createdAt;
    }

    /** Événements de sécurité réels pour l'explorateur de logs. */
    public List<SecurityEventRow> securityEvents(LocalDateTime from, LocalDateTime to) {
        List<SecurityEventRow> rows = new ArrayList<>();
        String sql = "SELECT id, type, identifier, ip_address, detail, created_at FROM security_events " +
                "WHERE created_at >= ? AND created_at < ? ORDER BY created_at DESC LIMIT 5000";
        try (Connection c = auth(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(SecurityEventRow.builder()
                            .id(rs.getLong(1))
                            .type(rs.getString(2))
                            .identifier(rs.getString(3))
                            .ipAddress(rs.getString(4))
                            .detail(rs.getString(5))
                            .createdAt(rs.getTimestamp(6).toLocalDateTime())
                            .build());
                }
            }
        } catch (SQLException e) {
            log.warn("Security events unavailable: {}", e.getMessage());
        }
        return rows;
    }

    // ── Plateforme (tableau de bord admin) ───────────────────────────────────

    @Getter
    @Builder
    public static class PlatformStats {
        private long activeUsers;
        private long newUsersThisWeek;
        private long pendingProAccounts;
        private long patients;
        private long doctors;
        private long pharmacies;
        private long ocrDocuments;
        private long failedLogins24h;
        private long suspiciousIps24h;
    }

    public PlatformStats platformStats() {
        PlatformStats.PlatformStatsBuilder b = PlatformStats.builder();
        try (Connection c = auth()) {
            b.activeUsers(count(c, "SELECT count(*) FROM users WHERE status = 'ACTIVE'"));
            b.newUsersThisWeek(count(c, "SELECT count(*) FROM users WHERE created_at >= ?", LocalDateTime.now().minusDays(7)));
            b.pendingProAccounts(count(c, "SELECT count(*) FROM users WHERE status = 'PENDING' AND role IN ('DOCTOR','PHARMACY')"));
            b.patients(count(c, "SELECT count(*) FROM users WHERE role = 'PATIENT'"));
            b.doctors(count(c, "SELECT count(*) FROM users WHERE role = 'DOCTOR'"));
            b.pharmacies(count(c, "SELECT count(*) FROM users WHERE role = 'PHARMACY'"));
            b.failedLogins24h(count(c, "SELECT count(*) FROM security_events WHERE type = 'LOGIN_FAILURE' AND created_at >= ?", LocalDateTime.now().minusHours(24)));
            b.suspiciousIps24h(suspiciousIps(c).size());
        } catch (SQLException e) {
            log.warn("Platform stats unavailable: {}", e.getMessage());
        }
        b.ocrDocuments(ocrDocuments());
        return b.build();
    }

    /** Bilans dont le texte a été extrait par OCR. */
    public long ocrDocuments() {
        try (Connection c = bilan()) {
            return count(c, "SELECT count(*) FROM bilans WHERE raw_ocr_text IS NOT NULL AND raw_ocr_text <> ''");
        } catch (SQLException e) {
            log.warn("OCR stats unavailable: {}", e.getMessage());
            return 0;
        }
    }

    // ── Utilitaires JDBC ─────────────────────────────────────────────────────

    private Connection auth() throws SQLException {
        return DriverManager.getConnection(authDbUrl, user, password);
    }

    private Connection bilan() throws SQLException {
        return DriverManager.getConnection(bilanDbUrl, user, password);
    }

    private static long count(Connection c, String sql, LocalDateTime... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setTimestamp(i + 1, Timestamp.valueOf(params[i]));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private static boolean isPrivateIp(String ip) {
        if (ip == null) return false;
        return ip.startsWith("127.") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")
                || ip.startsWith("10.") || ip.startsWith("192.168.")
                || ip.matches("^172\\.(1[6-9]|2\\d|3[01])\\..*");
    }
}
