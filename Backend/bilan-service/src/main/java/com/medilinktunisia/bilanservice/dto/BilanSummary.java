package com.medilinktunisia.bilanservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BilanSummary {
    private UUID id;
    private String typeBilan;
    private LocalDate dateBilan;
    private String status;
    private int resultCount;
    private int abnormalCount;
    private String reviewStatus;
    private Long patientId;
    private Long doctorId;
}
