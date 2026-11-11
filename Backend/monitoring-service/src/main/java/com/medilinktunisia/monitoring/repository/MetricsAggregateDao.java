package com.medilinktunisia.monitoring.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agrégations SQL sur service_health_history : évite de charger des
 * centaines de milliers de mesures en mémoire (1 mesure / 10 s / service).
 */
@Repository
@RequiredArgsConstructor
public class MetricsAggregateDao {

    private static final String KNOWN = "service_name IN ('auth-service','patient-service','doctor-service'," +
            "'pharmacy-service','prescription-service','bilan-service','ai-service','api-gateway')";

    /** Métriques exposées aux graphiques -> colonne SQL (liste blanche). */
    private static final Map<String, String> COLUMNS = Map.of(
            "cpu", "cpu_percent",
            "ram", "ram_percent",
            "disk", "disk_percent",
            "response_time", "response_time_ms",
            "uptime", "uptime_hours",
            "threads", "thread_count",
            "requests", "requests_count",
            "errors", "errors_count");

    private final JdbcTemplate jdbc;

    public static String column(String metric) {
        return metric == null ? null : COLUMNS.get(metric.toLowerCase());
    }

    public record CounterTotals(long requests, long errors, long notifications) {}

    /**
     * Totaux réels de compteurs cumulés sur une période. Chaque « vie » d'un service
     * (identifiée par started_at) contribue max - min, ou max si elle a démarré dans la période.
     */
    public CounterTotals counterTotals(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT started_at, " +
                "max(requests_count), min(requests_count), max(errors_count), min(errors_count), " +
                "max(notifications_count), min(notifications_count) " +
                "FROM service_health_history WHERE \"timestamp\" BETWEEN ? AND ? AND started_at IS NOT NULL AND " + KNOWN +
                " GROUP BY service_name, started_at";
        long[] t = new long[3];
        Timestamp fromTs = Timestamp.valueOf(from);
        jdbc.query(sql, rs -> {
            Timestamp started = rs.getTimestamp(1);
            boolean startedInWindow = !started.before(fromTs);
            t[0] += delta(rs.getObject(2), rs.getObject(3), startedInWindow);
            t[1] += delta(rs.getObject(4), rs.getObject(5), startedInWindow);
            t[2] += delta(rs.getObject(6), rs.getObject(7), startedInWindow);
        }, fromTs, Timestamp.valueOf(to));
        return new CounterTotals(t[0], t[1], t[2]);
    }

    private static long delta(Object max, Object min, boolean startedInWindow) {
        if (max == null) return 0;
        long mx = ((Number) max).longValue();
        long mn = min == null ? 0 : ((Number) min).longValue();
        return startedInWindow ? mx : Math.max(0, mx - mn);
    }

    public record StatusStats(long snapshots, long online, double avgResponse, long lastOnline) {}

    public StatusStats statusStats(LocalDateTime from, LocalDateTime to) {
        Timestamp f = Timestamp.valueOf(from), t = Timestamp.valueOf(to);
        Object[] row = jdbc.queryForObject(
                "SELECT count(*), count(*) FILTER (WHERE status = 'Online'), " +
                "coalesce(avg(response_time_ms) FILTER (WHERE status <> 'Offline'), 0) " +
                "FROM service_health_history WHERE \"timestamp\" BETWEEN ? AND ? AND " + KNOWN,
                (rs, i) -> new Object[]{rs.getLong(1), rs.getLong(2), rs.getDouble(3)}, f, t);
        Long lastOnline = jdbc.queryForObject(
                "SELECT count(*) FROM (SELECT DISTINCT ON (service_name) status FROM service_health_history " +
                "WHERE \"timestamp\" BETWEEN ? AND ? AND " + KNOWN + " ORDER BY service_name, \"timestamp\" DESC) x " +
                "WHERE status = 'Online'", Long.class, f, t);
        return new StatusStats((long) row[0], (long) row[1], (double) row[2], lastOnline != null ? lastOnline : 0);
    }

    public record DayValue(LocalDate date, double value) {}

    /** Moyenne journalière d'une métrique (tous services confondus). */
    public List<DayValue> dailyAverage(String column, LocalDateTime from, LocalDateTime to) {
        return jdbc.query(
                "SELECT CAST(\"timestamp\" AS date) d, avg(" + column + ") FROM service_health_history " +
                "WHERE \"timestamp\" BETWEEN ? AND ? AND " + column + " IS NOT NULL AND " + KNOWN +
                " GROUP BY d ORDER BY d",
                (rs, i) -> new DayValue(rs.getDate(1).toLocalDate(), rs.getDouble(2)),
                Timestamp.valueOf(from), Timestamp.valueOf(to));
    }

    /** Disponibilité journalière réelle : % de health checks en ligne. */
    public List<DayValue> dailyUptime(LocalDateTime from, LocalDateTime to) {
        return jdbc.query(
                "SELECT CAST(\"timestamp\" AS date) d, 100.0 * count(*) FILTER (WHERE status = 'Online') / count(*) " +
                "FROM service_health_history WHERE \"timestamp\" BETWEEN ? AND ? AND " + KNOWN +
                " GROUP BY d ORDER BY d",
                (rs, i) -> new DayValue(rs.getDate(1).toLocalDate(), rs.getDouble(2)),
                Timestamp.valueOf(from), Timestamp.valueOf(to));
    }

    public record Point(LocalDateTime at, String serviceName, double value) {}

    /**
     * Série temporelle échantillonnée (moyenne par intervalle) pour limiter
     * le nombre de points renvoyés aux graphiques.
     */
    public List<Point> bucketed(String column, String serviceName, LocalDateTime from, LocalDateTime to, int maxPoints) {
        long seconds = Math.max(1, java.time.Duration.between(from, to).getSeconds());
        long bucket = Math.max(10, seconds / Math.max(1, maxPoints));
        boolean perService = serviceName != null && !serviceName.isBlank();
        // "timestamp" est en heure locale sans fuseau : AT TIME ZONE 'UTC' évite tout décalage
        String bucketExpr = "(to_timestamp(floor(extract(epoch FROM \"timestamp\") / " + bucket + ") * " + bucket
                + ") AT TIME ZONE 'UTC')";
        boolean counter = column.equals("requests_count") || column.equals("errors_count");

        List<Object> args = new ArrayList<>(List.of(Timestamp.valueOf(from), Timestamp.valueOf(to)));
        String serviceFilter = "";
        if (perService) {
            serviceFilter = " AND service_name = ?";
            args.add(serviceName);
        }
        String where = " FROM service_health_history WHERE \"timestamp\" BETWEEN ? AND ? AND " + column
                + " IS NOT NULL AND " + KNOWN + serviceFilter;
        String label = perService ? "service_name" : "'all'";

        String sql;
        if (counter) {
            // Compteurs cumulés : on somme les incréments réels par intervalle (requêtes / erreurs par période)
            sql = "SELECT b, " + label + ", sum(d) FROM (" +
                    " SELECT " + bucketExpr + " AS b, service_name," +
                    " CASE WHEN prev IS NULL THEN 0 WHEN v < prev THEN v ELSE v - prev END AS d FROM (" +
                    "  SELECT \"timestamp\", service_name, " + column + " AS v," +
                    "  lag(" + column + ") OVER (PARTITION BY service_name, started_at ORDER BY \"timestamp\") AS prev" +
                    where + ") s) x GROUP BY b" + (perService ? ", service_name" : "") + " ORDER BY b";
        } else {
            sql = "SELECT " + bucketExpr + " AS b, " + label + ", avg(" + column + ")" + where +
                    " GROUP BY b" + (perService ? ", service_name" : "") + " ORDER BY b";
        }
        return jdbc.query(sql,
                (rs, i) -> new Point(rs.getTimestamp(1).toLocalDateTime(), rs.getString(2), rs.getDouble(3)),
                args.toArray());
    }

    public record StatusPoint(String serviceName, LocalDateTime at, String status) {}

    /** Mesures hors ligne / dégradées (peu nombreuses) pour reconstituer les incidents. */
    public List<StatusPoint> nonOnline(LocalDateTime from, LocalDateTime to) {
        return jdbc.query(
                "SELECT service_name, \"timestamp\", status FROM service_health_history " +
                "WHERE \"timestamp\" BETWEEN ? AND ? AND status <> 'Online' AND " + KNOWN +
                " ORDER BY service_name, \"timestamp\"",
                (rs, i) -> new StatusPoint(rs.getString(1), rs.getTimestamp(2).toLocalDateTime(), rs.getString(3)),
                Timestamp.valueOf(from), Timestamp.valueOf(to));
    }

    public record StatusChange(long id, String serviceName, LocalDateTime at, String status, String previous,
                               Double responseTimeMs, Double cpuPercent, Double ramPercent) {}

    /** Changements d'état des services (passage hors ligne, dégradé, retour en ligne). */
    public List<StatusChange> statusChanges(LocalDateTime from, LocalDateTime to) {
        return jdbc.query(
                "SELECT id, service_name, \"timestamp\", status, prev, response_time_ms, cpu_percent, ram_percent FROM (" +
                " SELECT id, service_name, \"timestamp\", status, response_time_ms, cpu_percent, ram_percent," +
                " lag(status) OVER (PARTITION BY service_name ORDER BY \"timestamp\") AS prev" +
                " FROM service_health_history WHERE \"timestamp\" BETWEEN ? AND ? AND " + KNOWN + ") x" +
                " WHERE prev IS DISTINCT FROM status ORDER BY \"timestamp\" DESC",
                (rs, i) -> new StatusChange(rs.getLong(1), rs.getString(2), rs.getTimestamp(3).toLocalDateTime(),
                        rs.getString(4), rs.getString(5),
                        (Double) rs.getObject(6), (Double) rs.getObject(7), (Double) rs.getObject(8)),
                Timestamp.valueOf(from), Timestamp.valueOf(to));
    }

    /** Première mesure en ligne d'un service après une date (fin d'incident). */
    public LocalDateTime firstOnlineAfter(String serviceName, LocalDateTime after) {
        List<LocalDateTime> r = jdbc.query(
                "SELECT \"timestamp\" FROM service_health_history WHERE service_name = ? AND \"timestamp\" > ? " +
                "AND status = 'Online' ORDER BY \"timestamp\" LIMIT 1",
                (rs, i) -> rs.getTimestamp(1).toLocalDateTime(), serviceName, Timestamp.valueOf(after));
        return r.isEmpty() ? null : r.get(0);
    }
}
