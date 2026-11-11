package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.dto.SecurityOverviewDto;
import com.medilinktunisia.monitoring.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService securityService;

    @GetMapping("/security")
    public ResponseEntity<SecurityOverviewDto> getSecurityOverview() {
        return ResponseEntity.ok(securityService.getSecurityOverview());
    }
}