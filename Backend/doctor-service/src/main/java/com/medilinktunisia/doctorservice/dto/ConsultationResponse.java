package com.medilinktunisia.doctorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ConsultationResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String type;
    private String reason;
    private String diagnosis;
    private String observations;
    private String bloodPressure;
    private Integer pulse;
    private BigDecimal temperature;
    private BigDecimal weight;
    private BigDecimal height;
    private BigDecimal bmi;
    private String requestedExams;
    private LocalDate followUpDate;
    private Long prescriptionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
