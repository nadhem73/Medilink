package com.medilinktunisia.patientservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentDto {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime dateTime;
    private String status;
    private String mode;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
