package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.LogEntryDto;
import com.medilinktunisia.monitoring.dto.LogPageDto;
import com.medilinktunisia.monitoring.repository.MetricsAggregateDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Journal réel de la plateforme :
 *  - changements d'état des services (hors ligne, dégradé, retour en ligne) ;
 *  - événements de sécurité d'auth-service (connexions, échecs, JWT expirés).
 */
@Service
@RequiredArgsConstructor
public class LogService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MetricsAggregateDao aggregates;
    private final PlatformStatsService platformStats;

    public LogPageDto getLogs(String service, String level, String search,
                              String dateFrom, String dateTo, int page, int pageSize) {
        LocalDateTime from = dateFrom != null && !dateFrom.isEmpty()
                ? LocalDate.parse(dateFrom).atStartOfDay()
                : LocalDateTime.now().minusDays(7);
        LocalDateTime to = dateTo != null && !dateTo.isEmpty()
                ? LocalDate.parse(dateTo).plusDays(1).atStartOfDay()
                : LocalDateTime.now();

        List<Entry> all = collect(from, to);

        String q = search != null ? search.toLowerCase(Locale.ROOT) : "";
        List<LogEntryDto> filtered = all.stream()
                .filter(e -> service == null || service.isEmpty()
                        || e.serviceId.equalsIgnoreCase(service) || e.dto.getService().equalsIgnoreCase(service))
                .filter(e -> level == null || level.isEmpty() || e.dto.getLevel().equalsIgnoreCase(level))
                .filter(e -> q.isEmpty()
                        || e.dto.getMessage().toLowerCase(Locale.ROOT).contains(q)
                        || e.dto.getUser().toLowerCase(Locale.ROOT).contains(q)
                        || e.dto.getIpAddress().toLowerCase(Locale.ROOT).contains(q))
                .map(e -> e.dto)
                .collect(Collectors.toList());

        int size = Math.max(1, pageSize);
        int total = filtered.size();
        int start = Math.max(0, page) * size;
        int end = Math.min(start + size, total);
        return LogPageDto.builder()
                .logs(start < total ? filtered.subList(start, end) : List.of())
                .totalCount(total)
                .page(page)
                .pageSize(size)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    public LogEntryDto getLogById(String id) {
        // Les identifiants encodent la source : on relit les 30 derniers jours
        LocalDateTime now = LocalDateTime.now();
        return collect(now.minusDays(30), now).stream()
                .map(e -> e.dto)
                .filter(d -> d.getId().equals(id))
                .findFirst().orElse(null);
    }

    private record Entry(LocalDateTime at, String serviceId, LogEntryDto dto) {}

    private List<Entry> collect(LocalDateTime from, LocalDateTime to) {
        List<Entry> entries = new ArrayList<>();

        for (MetricsAggregateDao.StatusChange c : aggregates.statusChanges(from, to)) {
            String name = MetricsPersistenceService.formatDisplayName(c.serviceName());
            String level;
            String message;
            switch (c.status()) {
                case "Offline" -> {
                    level = "ERROR";
                    message = name + " est hors ligne (health check sans réponse).";
                }
                case "Degraded" -> {
                    level = "WARNING";
                    message = name + " est dégradé (health check en erreur).";
                }
                default -> {
                    level = "INFO";
                    message = c.previous() == null
                            ? name + " en ligne" + metricsSuffix(c)
                            : name + " de nouveau en ligne" + metricsSuffix(c);
                }
            }
            entries.add(new Entry(c.at(), c.serviceName(), dto("H-" + c.id(), c.at(), name, level, message, "system", "")));
        }

        for (PlatformStatsService.SecurityEventRow e : platformStats.securityEvents(from, to)) {
            String who = e.getIdentifier() != null ? e.getIdentifier() : "inconnu";
            String level;
            String message;
            switch (e.getType()) {
                case "LOGIN_FAILURE" -> {
                    level = "WARNING";
                    message = "Échec de connexion pour " + who
                            + (e.getDetail() != null ? " : " + e.getDetail() : "");
                }
                case "LOGIN_SUCCESS" -> {
                    level = "INFO";
                    message = "Connexion réussie : " + who;
                }
                case "JWT_EXPIRED" -> {
                    level = "INFO";
                    message = "Token JWT expiré pour " + who
                            + (e.getDetail() != null ? " (" + e.getDetail() + ")" : "");
                }
                case "TOKEN_REFRESH" -> {
                    level = "INFO";
                    message = "Session prolongée (refresh token) : " + who;
                }
                default -> {
                    level = "INFO";
                    message = e.getType() + " : " + who;
                }
            }
            entries.add(new Entry(e.getCreatedAt(), "auth-service",
                    dto("S-" + e.getId(), e.getCreatedAt(), "Auth Service", level, message, who,
                            e.getIpAddress() != null ? e.getIpAddress() : "")));
        }

        entries.sort(Comparator.comparing(Entry::at).reversed());
        return entries;
    }

    private static String metricsSuffix(MetricsAggregateDao.StatusChange c) {
        List<String> parts = new ArrayList<>();
        if (c.responseTimeMs() != null) parts.add(String.format(Locale.ROOT, "%.0f ms", c.responseTimeMs()));
        if (c.cpuPercent() != null) parts.add(String.format(Locale.ROOT, "CPU %.1f%%", c.cpuPercent()));
        if (c.ramPercent() != null) parts.add(String.format(Locale.ROOT, "heap %.1f%%", c.ramPercent()));
        return parts.isEmpty() ? "." : " (" + String.join(", ", parts) + ").";
    }

    private static LogEntryDto dto(String id, LocalDateTime at, String service, String level,
                                   String message, String user, String ip) {
        return LogEntryDto.builder()
                .id(id)
                .date(at.format(DATE))
                .time(at.format(TIME))
                .service(service)
                .level(level)
                .message(message)
                .user(user)
                .ipAddress(ip)
                .traceId("")
                .build();
    }
}
