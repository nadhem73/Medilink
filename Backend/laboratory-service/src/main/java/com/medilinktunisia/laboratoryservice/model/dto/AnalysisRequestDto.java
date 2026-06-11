package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisRequest;
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
public class AnalysisRequestDto {
    private Long id;
    private String requestNumber;
    private Long laboratoryId;
    private String laboratoryName;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private Long doctorId;
    private Long prescriptionId;
    private LocalDateTime requestDate;
    private LocalDateTime collectionDate;
    private LocalDateTime expectedResultDate;
    private LocalDateTime actualResultDate;
    private AnalysisRequest.RequestStatus status;
    private AnalysisRequest.RequestPriority priority;
    private AnalysisRequest.CollectionType collectionType;
    private String collectionAddress;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Boolean paid;
    private String clinicalInfo;
    private String laboratoryNotes;
    private String instructions;
    private String assignedTechnician;
    private Boolean patientNotified;
    private Boolean doctorNotified;
    private List<AnalysisItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
