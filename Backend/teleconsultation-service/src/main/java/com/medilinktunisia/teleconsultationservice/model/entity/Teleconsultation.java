package com.medilinktunisia.teleconsultationservice.model.entity;

import com.medilinktunisia.teleconsultationservice.model.enums.ConsultationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teleconsultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teleconsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String consultationCode;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String doctorSpecialty;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.SCHEDULED;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column
    private Long prescriptionId;

    @OneToMany(mappedBy = "teleconsultation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConsultationMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "teleconsultation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SharedDocument> sharedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "teleconsultation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConsultationParticipant> participants = new ArrayList<>();

    @Column
    private String sessionId;

    @Column
    private String cancellationReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (consultationCode == null) {
            consultationCode = generateConsultationCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateConsultationCode() {
        return "TC" + System.currentTimeMillis();
    }

    public void startConsultation() {
        this.status = ConsultationStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
    }

    public void endConsultation() {
        this.status = ConsultationStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
    }

    public void addParticipant(ConsultationParticipant participant) {
        participants.add(participant);
        participant.setTeleconsultation(this);
    }

    public void removeParticipant(ConsultationParticipant participant) {
        participants.remove(participant);
        participant.setTeleconsultation(null);
    }
}
