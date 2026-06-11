package com.medilinktunisia.teleconsultationservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medilinktunisia.teleconsultationservice.model.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teleconsultation_id", nullable = false)
    @JsonIgnore
    private Teleconsultation teleconsultation;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Column
    private LocalDateTime joinedAt;

    @Column
    private LocalDateTime leftAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
