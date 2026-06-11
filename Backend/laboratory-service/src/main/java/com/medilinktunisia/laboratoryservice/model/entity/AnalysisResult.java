package com.medilinktunisia.laboratoryservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entité AnalysisResult - Résultat d'une analyse
 * Selon cahier des charges Section 6.5 - Upload des résultats au format PDF
 */
@Entity
@Table(name = "analysis_results", indexes = {
    @Index(name = "idx_result_request", columnList = "request_id"),
    @Index(name = "idx_result_analysis_type", columnList = "analysis_type_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private AnalysisRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_type_id", nullable = false)
    private AnalysisType analysisType;

    // Résultat
    @Column(columnDefinition = "TEXT")
    private String result; // Valeur du résultat

    @Column
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String referenceRange; // Plage de référence

    @Enumerated(EnumType.STRING)
    private ResultStatus resultStatus; // NORMAL, ABNORMAL, CRITICAL

    // Fichier PDF
    @Column
    private String pdfFileName;

    @Column
    private String pdfFilePath;

    @Column
    private Long pdfFileSize;

    // Interprétation
    @Column(columnDefinition = "TEXT")
    private String interpretation; // Interprétation du résultat

    @Column(columnDefinition = "TEXT")
    private String comments; // Commentaires du biologiste

    // Validation
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean validated = false;

    private String validatedBy; // Nom du biologiste validateur

    private LocalDateTime validatedAt;

    // Technicien
    private String performedBy; // Technicien ayant effectué l'analyse

    private LocalDateTime performedAt;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ResultStatus {
        NORMAL,
        ABNORMAL_LOW,
        ABNORMAL_HIGH,
        CRITICAL,
        INDETERMINATE
    }
}
