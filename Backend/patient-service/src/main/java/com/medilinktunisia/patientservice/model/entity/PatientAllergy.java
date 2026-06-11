package com.medilinktunisia.patientservice.model.entity;

import com.medilinktunisia.patientservice.model.enums.AllergenType;
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
@Table(name = "patient_allergies", indexes = {
    @Index(name = "idx_allergy_patient", columnList = "patient_id"),
    @Index(name = "idx_allergy_severity", columnList = "severity_level")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Informations sur l'allergène
    @Column(name = "allergen_name", nullable = false, length = 200)
    private String allergenName;

    @Enumerated(EnumType.STRING)
    @Column(name = "allergen_type", nullable = false, length = 50)
    private AllergenType allergenType;

    // Réaction observée
    @Column(name = "reaction_description", nullable = false, columnDefinition = "TEXT")
    private String reactionDescription;

    // Gravité
    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false, length = 20)
    private SeverityLevel severityLevel;

    // Dates et diagnostic
    @Column(name = "first_reaction_date")
    private LocalDate firstReactionDate;

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

    @Column(name = "diagnosed_by_doctor_id")
    private Long diagnosedByDoctorId; // Référence vers doctor-service

    // Informations complémentaires
    @Column(name = "treatment_given", columnDefinition = "TEXT")
    private String treatmentGiven;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    // Statut
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Métadonnées
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
