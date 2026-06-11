package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisRequest;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRequestUpdateRequest {

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number format")
    private String patientPhone;

    private String patientEmail;

    private LocalDateTime collectionDate;

    private LocalDateTime expectedResultDate;

    private AnalysisRequest.RequestStatus status;

    private AnalysisRequest.RequestPriority priority;

    @Size(max = 500)
    private String collectionAddress;

    @Size(max = 2000)
    private String clinicalInfo;

    @Size(max = 1000)
    private String laboratoryNotes;

    @Size(max = 1000)
    private String instructions;

    private String assignedTechnician;

    private Boolean patientNotified;

    private Boolean doctorNotified;
}
