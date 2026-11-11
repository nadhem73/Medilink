package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.dto.ServiceHealthDto;
import com.medilinktunisia.monitoring.service.MetricsPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class ServicesController {

    private final MetricsPersistenceService persistenceService;

    @GetMapping("/services")
    public ResponseEntity<List<ServiceHealthDto>> getServices() {
        return ResponseEntity.ok(persistenceService.getLatestServices());
    }
}
