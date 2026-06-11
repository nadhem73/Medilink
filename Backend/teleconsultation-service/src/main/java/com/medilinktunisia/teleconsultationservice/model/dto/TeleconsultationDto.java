package com.medilinktunisia.teleconsultationservice.model.dto;

import com.medilinktunisia.teleconsultationservice.model.enums.ConsultationStatus;
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
public class TeleconsultationDto {

    private Long id;
    private String consultationCode;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String reason;
    private ConsultationStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String notes;
    private String diagnosis;
    private Long prescriptionId;
    private List<MessageDto> messages;
    private List<SharedDocumentDto> sharedDocuments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
