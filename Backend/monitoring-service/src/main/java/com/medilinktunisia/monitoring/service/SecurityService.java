package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.SecurityOverviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Indicateurs de sécurité réels, issus du journal security_events d'auth-service
 * (connexions, échecs, JWT expirés, sessions) et de la table users (comptes suspendus).
 */
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final PlatformStatsService platformStats;

    public SecurityOverviewDto getSecurityOverview() {
        PlatformStatsService.SecurityStats s = platformStats.securityStats();
        return SecurityOverviewDto.builder()
                .loginAttempts(s.getLoginAttemptsToday())
                .failedLogins(s.getFailedLoginsToday())
                .lockedAccounts(s.getSuspendedAccounts())
                .expiredJwt(s.getExpiredJwtToday())
                .activeSessions(s.getActiveSessions())
                .expiredSessions(s.getExpiredSessionsToday())
                .suspiciousActivities(s.getSuspiciousIps().size())
                .topSuspiciousIps(s.getSuspiciousIps())
                .build();
    }
}
