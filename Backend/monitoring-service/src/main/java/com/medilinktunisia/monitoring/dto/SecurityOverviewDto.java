package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityOverviewDto {
    private long loginAttempts;
    private long failedLogins;
    private long lockedAccounts;
    private long expiredJwt;
    private long activeSessions;
    private long expiredSessions;
    private long suspiciousActivities;
    private List<SuspiciousIpDto> topSuspiciousIps;
}
