package com.medilinktunisia.teleconsultationservice.model.dto;

import com.medilinktunisia.teleconsultationservice.model.enums.ConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Teleconsultation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationDto {
    private Long id;
    private String sessionId;
    private String consultationNumber;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String reason;
    private ConsultationStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime startTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime endTime;
    private LocalDateTime actualEndTime;
    private Integer durationMinutes;
    private String notes;
    private String diagnosis;
    private List<ParticipantDto> participants;
    private List<MessageDto> messages;
    private List<SharedDocumentDto> sharedDocuments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
