package com.medilinktunisia.teleconsultationservice.model.dto;

import com.medilinktunisia.teleconsultationservice.model.enums.MessageType;
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
public class MessageDto {

    private Long id;
    private Long consultationId;
    private Long senderId;
    private String senderName;
    private ParticipantRole senderRole;
    private MessageType messageType;
    private String content;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Boolean isRead;
    private LocalDateTime sentAt;
}
