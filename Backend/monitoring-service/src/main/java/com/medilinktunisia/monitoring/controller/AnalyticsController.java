package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.dto.AnalyticsDto;
import com.medilinktunisia.monitoring.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/history")
    public ResponseEntity<AnalyticsDto> getHistory() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }
}