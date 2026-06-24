package com.medilinktunisia.doctorservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConsultationRequest {
    private Long patientId;
    private Long appointmentId;
    private String type;
    private String reason;
    private String diagnosis;
    private String observations;
    private String bloodPressure;
    private Integer pulse;
    private BigDecimal temperature;
    private BigDecimal weight;
    private BigDecimal height;
    private String requestedExams;
    private LocalDateTime followUpDate;
}
