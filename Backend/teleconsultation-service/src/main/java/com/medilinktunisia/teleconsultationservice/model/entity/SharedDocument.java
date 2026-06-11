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
@Table(name = "shared_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teleconsultation_id", nullable = false)
    @JsonIgnore
    private Teleconsultation teleconsultation;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Long uploadedBy;

    @Column(nullable = false)
    private String uploaderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole uploaderRole;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
