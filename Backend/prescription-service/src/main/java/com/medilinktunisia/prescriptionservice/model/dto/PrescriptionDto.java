package com.medilinktunisia.prescriptionservice.model.dto;

import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDto {

    private Long id;
    private String prescriptionNumber;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String diagnosis;
    private String notes;
    private PrescriptionStatus status;
    private LocalDateTime prescriptionDate;
    private LocalDateTime expiryDate;
    private LocalDateTime dispensedDate;
    private Long pharmacyId;
    private String pharmacyName;
    private List<MedicationItemDto> medications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
