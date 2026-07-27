package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogPageDto {
    private List<LogEntryDto> logs;
    private long totalCount;
    private int page;
    private int pageSize;
    private int totalPages;
}
