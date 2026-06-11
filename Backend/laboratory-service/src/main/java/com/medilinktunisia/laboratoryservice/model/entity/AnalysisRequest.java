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
import java.util.ArrayList;
import java.util.List;

/**
 * Entité AnalysisRequest - Demande d'analyse médicale
 * Selon cahier des charges Section 6.5 - Gestion des demandes d'analyses
 */
@Entity
@Table(name = "analysis_requests", indexes = {
    @Index(name = "idx_request_laboratory", columnList = "laboratory_id"),
    @Index(name = "idx_request_patient", columnList = "patient_id"),
    @Index(name = "idx_request_status", columnList = "status"),
    @Index(name = "idx_request_date", columnList = "request_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AnalysisRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    // Références externes
    @Column(nullable = false)
    private Long patientId; // Référence vers patient-service

    @Column
    private Long doctorId; // Référence vers doctor-service (optionnel)

    @Column
    private Long prescriptionId; // Référence vers prescription-service (optionnel)

    // Informations patient
    private String patientName;

    private String patientPhone;

    private String patientEmail;

    // Date et statut
    @Column(nullable = false)
    private LocalDateTime requestDate;

    private LocalDateTime collectionDate; // Date du prélèvement

    private LocalDateTime expectedResultDate; // Date prévue des résultats

    private LocalDateTime actualResultDate; // Date réelle des résultats

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestPriority priority = RequestPriority.NORMAL;

    // Type de prélèvement
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollectionType collectionType;

    @Column(columnDefinition = "TEXT")
    private String collectionAddress; // Pour prélèvement à domicile

    // Montant
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean paid = false;

    // Détails des analyses
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnalysisItem> items = new ArrayList<>();

    // Résultats
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnalysisResult> results = new ArrayList<>();

    // Notes
    @Column(columnDefinition = "TEXT")
    private String clinicalInfo; // Informations cliniques du médecin

    @Column(columnDefinition = "TEXT")
    private String laboratoryNotes; // Notes du laboratoire

    @Column(columnDefinition = "TEXT")
    private String instructions; // Instructions spéciales

    // Technicien assigné
    private String assignedTechnician;

    // Notification
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean patientNotified = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean doctorNotified = false;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Méthodes helper
    public void addItem(AnalysisItem item) {
        items.add(item);
        item.setRequest(this);
    }

    public void removeItem(AnalysisItem item) {
        items.remove(item);
        item.setRequest(null);
    }

    public void addResult(AnalysisResult result) {
        results.add(result);
        result.setRequest(this);
    }

    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(AnalysisItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum RequestStatus {
        PENDING,           // En attente
        CONFIRMED,         // Confirmée
        SAMPLE_COLLECTED,  // Échantillon prélevé
        IN_PROGRESS,       // En cours d'analyse
        COMPLETED,         // Terminée
        READY,             // Résultats prêts
        DELIVERED,         // Résultats livrés
        CANCELLED          // Annulée
    }

    public enum RequestPriority {
        NORMAL,
        URGENT,
        EMERGENCY
    }

    public enum CollectionType {
        IN_LAB,           // Au laboratoire
        HOME_COLLECTION,  // À domicile
        HOSPITAL          // À l'hôpital
    }
}
