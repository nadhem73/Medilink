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
public class ChatMessage {

    private String consultationCode;
    private Long senderId;
    private String senderName;
    private String sender; // Alias for senderName
    private ParticipantRole senderRole;
    private MessageType messageType;
    private MessageType type; // Alias for messageType
    private String content;
    private String fileUrl;
    private String fileName;
    private LocalDateTime timestamp;
    
    public String getSender() {
        return senderName != null ? senderName : sender;
    }
    
    public MessageType getType() {
        return messageType != null ? messageType : type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
        this.messageType = type;
    }
}
