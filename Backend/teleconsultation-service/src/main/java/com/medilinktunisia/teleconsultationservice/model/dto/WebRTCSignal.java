package com.medilinktunisia.teleconsultationservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebRTCSignal {

    private String type; // "offer", "answer", "candidate"
    private String consultationCode;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private Long fromUserId;
    private Long toUserId;
    private Object signal; // SDP offer/answer or ICE candidate
    private Object data; // Additional data
}
