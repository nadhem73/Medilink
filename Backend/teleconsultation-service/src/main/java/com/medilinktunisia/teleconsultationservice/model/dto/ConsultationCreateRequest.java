package com.medilinktunisia.teleconsultationservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationCreateRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Patient name is required")
    private String patientName;
    
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    @NotNull(message = "Doctor name is required")
    private String doctorName;
    
    private String doctorSpecialty;
    
    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledTime;
    
    private String reason;
}
