package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryDto {
    private String id;
    private String date;
    private String time;
    private String service;
    private String level;
    private String message;
    private String user;
    private String ipAddress;
    private String traceId;
}
