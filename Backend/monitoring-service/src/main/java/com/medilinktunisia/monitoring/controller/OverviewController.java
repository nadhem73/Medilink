package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.dto.OverviewDto;
import com.medilinktunisia.monitoring.service.MetricsPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class OverviewController {

    private final MetricsPersistenceService persistenceService;

    @GetMapping("/overview")
    public ResponseEntity<OverviewDto> getOverview() {
        return ResponseEntity.ok(persistenceService.getLatestOverview());
    }
}
