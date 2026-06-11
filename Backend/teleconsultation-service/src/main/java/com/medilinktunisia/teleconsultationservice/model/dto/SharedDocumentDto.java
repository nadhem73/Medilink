package com.medilinktunisia.teleconsultationservice.model.dto;

import com.medilinktunisia.teleconsultationservice.model.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedDocumentDto {

    private Long id;
    private Long consultationId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Long uploadedBy;
    private String uploaderName;
    private ParticipantRole uploaderRole;
    private String description;
    private LocalDateTime uploadedAt;
}
