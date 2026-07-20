package com.medilinktunisia.bilanservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanBilanResponse {
    private UUID id;
    private String typeBilan;
    private String format;
    private LocalDate dateBilan;
    private String laboratoire;
    private String status;
    private String reviewStatus;
    private Long patientId;
    private Long doctorId;
    private List<BilanResultDto> resultats;
}
