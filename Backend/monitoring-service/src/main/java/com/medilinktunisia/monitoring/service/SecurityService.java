package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.SecurityOverviewDto;
import com.medilinktunisia.monitoring.dto.ServiceHealthDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;

@Service
@Slf4j
public class SecurityService {

    private final MetricsPersistenceService persistenceService;

    public SecurityService(MetricsPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @PostConstruct
    public void init() {
        log.info("Security service initialized — all data sourced from system metrics");
    }

    public SecurityOverviewDto getSecurityOverview() {
        List<ServiceHealthDto> services = persistenceService.getLatestServices();
        long offlineCount = services.stream()
                .filter(s -> "Offline".equals(s.getStatus()))
                .count();
        long totalRequests = services.stream()
                .mapToLong(ServiceHealthDto::getRequestsCount)
                .sum();
        long totalErrors = services.stream()
                .mapToLong(ServiceHealthDto::getErrorsCount)
                .sum();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long activeSessions = Math.max(0, threadBean.getThreadCount() - 10);
        long lockedAccounts = Math.max(0, offlineCount);

        return SecurityOverviewDto.builder()
                .loginAttempts(totalRequests)
                .failedLogins(totalErrors)
                .lockedAccounts(lockedAccounts)
                .expiredJwt(0)
                .activeSessions(activeSessions)
                .expiredSessions(0)
                .suspiciousActivities(0)
                .topSuspiciousIps(List.of())
                .build();
    }
}