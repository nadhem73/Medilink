package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.service.PlatformStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Statistiques réelles de la plateforme pour le tableau de bord admin. */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformStatsService platformStats;

    @GetMapping("/platform")
    public ResponseEntity<PlatformStatsService.PlatformStats> getPlatformStats() {
        return ResponseEntity.ok(platformStats.platformStats());
    }
}
