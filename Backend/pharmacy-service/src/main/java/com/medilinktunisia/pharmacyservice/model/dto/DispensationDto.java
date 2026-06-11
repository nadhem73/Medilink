package com.medilinktunisia.pharmacyservice.model.dto;

import com.medilinktunisia.pharmacyservice.model.entity.PrescriptionDispensation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispensationDto {
    private Long id;
    private String dispensationNumber;
    private Long pharmacyId;
    private String pharmacyName;
    private Long prescriptionId;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long doctorId;
    private LocalDateTime dispensationDate;
    private PrescriptionDispensation.DispensationStatus status;
    private Long pharmacistUserId;
    private String pharmacistName;
    private BigDecimal totalAmount;
    private BigDecimal patientPayment;
    private BigDecimal insurancePayment;
    private Boolean prescriptionVerified;
    private Boolean patientIdentityVerified;
    private Boolean insuranceVerified;
    private String pharmacistNotes;
    private String patientInstructions;
    private List<DispensationItemDto> items;
    private LocalDateTime createdAt;
}
