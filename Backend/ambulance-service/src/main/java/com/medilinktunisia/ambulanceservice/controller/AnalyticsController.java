package com.medilinktunisia.ambulanceservice.controller;

import com.medilinktunisia.ambulanceservice.model.dto.AnalyticsDto;
import com.medilinktunisia.ambulanceservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Contrôleur pour les statistiques et analytics
 */
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDto> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStatistics());
    }

    @GetMapping("/response-time")
    public ResponseEntity<Double> getAverageResponseTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getAverageResponseTime(startDate, endDate));
    }

    @GetMapping("/emergencies/by-type")
    public ResponseEntity<?> getEmergenciesByType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getEmergenciesByType(startDate, endDate));
    }

    @GetMapping("/ambulances/utilization")
    public ResponseEntity<?> getAmbulanceUtilization() {
        return ResponseEntity.ok(analyticsService.getAmbulanceUtilization());
    }
}
