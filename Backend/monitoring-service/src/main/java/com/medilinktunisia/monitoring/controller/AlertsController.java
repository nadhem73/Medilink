package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.dto.AlertDto;
import com.medilinktunisia.monitoring.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitoring/alerts")
@RequiredArgsConstructor
public class AlertsController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertDto>> getAlerts(
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(alertService.getAlerts(priority, status));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlertDto> acknowledgeAlert(@PathVariable String id) {
        AlertDto alert = alertService.acknowledgeAlert(id);
        return alert != null ? ResponseEntity.ok(alert) : ResponseEntity.notFound().build();
    }
}