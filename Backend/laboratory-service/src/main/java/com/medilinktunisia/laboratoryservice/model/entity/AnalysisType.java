package com.medilinktunisia.laboratoryservice.model.entity;

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
 * Entité AnalysisType - Type d'analyse disponible (référentiel)
 */
@Entity
@Table(name = "analysis_types", indexes = {
    @Index(name = "idx_analysis_code", columnList = "analysis_code"),
    @Index(name = "idx_analysis_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AnalysisType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String analysisCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Prix
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean reimbursable = false;

    @Column(precision = 5, scale = 2)
    private BigDecimal reimbursementRate;

    // Préparation et délais
    @Column(columnDefinition = "TEXT")
    private String preparationInstructions; // Instructions de préparation (jeûne, etc.)

    @Column
    private Integer turnaroundTimeHours; // Délai standard en heures

    @Column(columnDefinition = "TEXT")
    private String sampleType; // Type d'échantillon (sang, urine, etc.)

    private Integer sampleVolumeML; // Volume nécessaire

    // Valeurs de référence
    @Column(columnDefinition = "TEXT")
    private String referenceRanges; // Valeurs de référence normales

    @Column(columnDefinition = "TEXT")
    private String unit; // Unité de mesure

    // Statut
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean active = true;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum AnalysisCategory {
        HEMATOLOGY,           // Hématologie
        BIOCHEMISTRY,         // Biochimie
        IMMUNOLOGY,           // Immunologie
        MICROBIOLOGY,         // Microbiologie
        SEROLOGY,             // Sérologie
        HORMONES,             // Hormones
        TOXICOLOGY,           // Toxicologie
        GENETICS,             // Génétique
        HISTOPATHOLOGY,       // Histopathologie
        CYTOLOGY,             // Cytologie
        URINE_ANALYSIS,       // Analyse d'urine
        PARASITOLOGY,         // Parasitologie
        VIROLOGY,             // Virologie
        OTHER                 // Autre
    }
}
