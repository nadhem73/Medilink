package com.medilinktunisia.teleconsultationservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medilinktunisia.teleconsultationservice.model.enums.MessageType;
import com.medilinktunisia.teleconsultationservice.model.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teleconsultation_id", nullable = false)
    @JsonIgnore
    private Teleconsultation teleconsultation;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole senderRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private String fileUrl;

    @Column
    private String fileName;

    @Column
    private Long fileSize;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
