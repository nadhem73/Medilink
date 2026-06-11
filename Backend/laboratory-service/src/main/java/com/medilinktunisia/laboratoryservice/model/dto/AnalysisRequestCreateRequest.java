package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisRequest;
import jakarta.validation.constraints.*;
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
public class AnalysisRequestCreateRequest {

    @NotNull(message = "Laboratory ID is required")
    private Long laboratoryId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long doctorId;

    private Long prescriptionId;

    @NotBlank(message = "Patient name is required")
    @Size(max = 100)
    private String patientName;

    @NotBlank(message = "Patient phone is required")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number format")
    private String patientPhone;

    @Email(message = "Invalid email format")
    private String patientEmail;

    @NotNull(message = "Request date is required")
    private LocalDateTime requestDate;

    private LocalDateTime collectionDate;

    private LocalDateTime expectedResultDate;

    @NotNull(message = "Priority is required")
    private AnalysisRequest.RequestPriority priority;

    @NotNull(message = "Collection type is required")
    private AnalysisRequest.CollectionType collectionType;

    @Size(max = 500)
    private String collectionAddress; // Requis si collectionType = HOME_COLLECTION

    @NotEmpty(message = "At least one analysis item is required")
    private List<AnalysisItemCreateRequest> items;

    @Size(max = 2000)
    private String clinicalInfo;

    @Size(max = 1000)
    private String instructions;

    private String assignedTechnician;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisItemCreateRequest {
        @NotNull(message = "Analysis type ID is required")
        private Long analysisTypeId;

        @Size(max = 500)
        private String notes;
    }
}
