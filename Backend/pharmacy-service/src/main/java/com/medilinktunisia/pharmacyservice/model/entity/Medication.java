package com.medilinktunisia.pharmacyservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Medication - Référentiel des médicaments
 * Selon cahier des charges Section 10 Base de Données
 */
@Entity
@Table(name = "medications", indexes = {
    @Index(name = "idx_medication_code", columnList = "medication_code"),
    @Index(name = "idx_medication_name", columnList = "name"),
    @Index(name = "idx_medication_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String medicationCode; // Code national du médicament

    @Column(nullable = false)
    private String name;

    private String scientificName;

    @Column(nullable = false)
    private String manufacturer;

    // Catégorie et type
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicationForm form;

    // Dosage
    @Column(nullable = false)
    private String dosage;

    private String dosageUnit;

    // Prix
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal price;

    @Column(precision = 10, scale = 3)
    private BigDecimal subsidizedPrice;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean reimbursable;

    @Column(precision = 5, scale = 2)
    private BigDecimal reimbursementRate;

    // Prescription
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean requiresPrescription = true;

    @Enumerated(EnumType.STRING)
    private PrescriptionType prescriptionType;

    // Informations médicales
    @Column(columnDefinition = "TEXT")
    private String indications;

    @Column(columnDefinition = "TEXT")
    private String contraindications;

    @Column(columnDefinition = "TEXT")
    private String sideEffects;

    @Column(columnDefinition = "TEXT")
    private String dosageInstructions;

    @Column(columnDefinition = "TEXT")
    private String precautions;

    // Caractéristiques
    private String activeIngredient;

    @Column(columnDefinition = "TEXT")
    private String composition;

    private String packaging;

    // Statut et disponibilité
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MedicationStatus status = MedicationStatus.AVAILABLE;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean active = true;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum MedicationCategory {
        ANALGESIC,
        ANTIBIOTIC,
        ANTIVIRAL,
        ANTIFUNGAL,
        CARDIOVASCULAR,
        GASTROINTESTINAL,
        RESPIRATORY,
        ENDOCRINE,
        NEUROLOGICAL,
        DERMATOLOGICAL,
        OPHTHALMOLOGICAL,
        VITAMIN_SUPPLEMENT,
        OTHER
    }

    public enum MedicationForm {
        TABLET,
        CAPSULE,
        SYRUP,
        SUSPENSION,
        INJECTION,
        CREAM,
        OINTMENT,
        GEL,
        DROPS,
        INHALER,
        SUPPOSITORY,
        PATCH,
        OTHER
    }

    public enum PrescriptionType {
        STANDARD,
        CONTROLLED_SUBSTANCE,
        NARCOTIC,
        PSYCHOTROPIC
    }

    public enum MedicationStatus {
        AVAILABLE,
        OUT_OF_STOCK,
        DISCONTINUED,
        PENDING_APPROVAL
    }
}
