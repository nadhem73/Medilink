package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.dto.LogEntryDto;
import com.medilinktunisia.monitoring.dto.LogPageDto;
import com.medilinktunisia.monitoring.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring/logs")
@RequiredArgsConstructor
public class LogsController {

    private final LogService logService;

    @GetMapping
    public ResponseEntity<LogPageDto> getLogs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        return ResponseEntity.ok(logService.getLogs(service, level, search, dateFrom, dateTo, page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogEntryDto> getLogById(@PathVariable String id) {
        LogEntryDto log = logService.getLogById(id);
        return log != null ? ResponseEntity.ok(log) : ResponseEntity.notFound().build();
    }
}