package com.medilinktunisia.prescriptionservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PrescriptionResponse {
    private Long id;
    private Long consultationId;
    private Long patientId;
    private Long doctorId;
    private Long pharmacieId;
    private String status;
    private String notes;
    private List<PrescriptionItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
