package com.medilinktunisia.patientservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Dossier médical d'un patient.
 * Lié à l'utilisateur de l'auth-service par son identifiant ({@code userId}).
 * Regroupe les données médicales saisies à l'inscription et affichées
 * dans le dossier médical du panel patient.
 */
@Entity
@Table(name = "medical_records", indexes = {
        @Index(name = "idx_medical_records_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant de l'utilisateur (User) côté auth-service. */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    /** Taille en cm. */
    private Double height;

    /** Poids en kg. */
    private Double weight;

    /** Allergies connues (texte libre / liste séparée par des virgules). */
    @Column(columnDefinition = "TEXT")
    private String allergies;

    /** Maladies chroniques / antécédents. */
    @Column(name = "chronic_diseases", columnDefinition = "TEXT")
    private String chronicDiseases;

    @Column(name = "current_treatments", columnDefinition = "TEXT")
    private String currentTreatments;

    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "insurance_company", length = 150)
    private String insuranceCompany;

    @Column(name = "insurance_number", length = 50)
    private String insuranceNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
