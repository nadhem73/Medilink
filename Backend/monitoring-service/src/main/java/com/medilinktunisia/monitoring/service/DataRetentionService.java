package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.repository.ServiceHealthHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataRetentionService {

    private final ServiceHealthHistoryRepository repository;

    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = repository.deleteByTimestampBefore(cutoff);
        log.info("Data retention: purged {} records older than 30 days", deleted);
    }
}
