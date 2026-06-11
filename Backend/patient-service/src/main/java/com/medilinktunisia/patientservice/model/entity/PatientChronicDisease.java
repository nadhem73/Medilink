package com.medilinktunisia.patientservice.model.entity;

import com.medilinktunisia.patientservice.model.enums.SeverityLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_chronic_diseases", indexes = {
    @Index(name = "idx_chronic_disease_patient", columnList = "patient_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientChronicDisease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "disease_name", nullable = false, length = 200)
    private String diseaseName;

    @Column(name = "icd10_code", length = 20)
    private String icd10Code; // Code international ICD-10

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", length = 20)
    private SeverityLevel severityLevel;

    @Column(name = "current_treatment", columnDefinition = "TEXT")
    private String currentTreatment;

    @Column(name = "monitoring_notes", columnDefinition = "TEXT")
    private String monitoringNotes;

    @Column(name = "is_controlled", nullable = false)
    @Builder.Default
    private Boolean isControlled = false;

    // Métadonnées
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
