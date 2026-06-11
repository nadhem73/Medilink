package com.medilinktunisia.pharmacyservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité PrescriptionDispensation - Dispensation d'ordonnance électronique
 * Selon cahier des charges Section 6.4 Module Pharmacies - Réception des ordonnances électroniques
 */
@Entity
@Table(name = "prescription_dispensations", indexes = {
    @Index(name = "idx_dispensation_pharmacy", columnList = "pharmacy_id"),
    @Index(name = "idx_dispensation_prescription", columnList = "prescription_id"),
    @Index(name = "idx_dispensation_patient", columnList = "patient_id"),
    @Index(name = "idx_dispensation_date", columnList = "dispensation_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PrescriptionDispensation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String dispensationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    // Références externes
    @Column(nullable = false)
    private Long prescriptionId; // Référence vers prescription-service

    @Column(nullable = false)
    private Long patientId; // Référence vers patient-service

    @Column(nullable = false)
    private Long doctorId; // Référence vers doctor-service

    // Informations patient
    private String patientName;

    private String patientPhone;

    // Date et statut
    @Column(nullable = false)
    private LocalDateTime dispensationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DispensationStatus status = DispensationStatus.PENDING;

    // Pharmacien qui a dispensé
    private Long pharmacistUserId;

    private String pharmacistName;

    // Montant
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal patientPayment; // Montant payé par le patient

    @Column(precision = 10, scale = 2)
    private BigDecimal insurancePayment; // Montant remboursé par l'assurance

    // Détails des médicaments dispensés
    @OneToMany(mappedBy = "dispensation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DispensationItem> items = new ArrayList<>();

    // Vérifications
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean prescriptionVerified = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean patientIdentityVerified = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean insuranceVerified = false;

    // Notes
    @Column(columnDefinition = "TEXT")
    private String pharmacistNotes;

    @Column(columnDefinition = "TEXT")
    private String patientInstructions;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Méthodes helper
    public void addItem(DispensationItem item) {
        items.add(item);
        item.setDispensation(this);
    }

    public void removeItem(DispensationItem item) {
        items.remove(item);
        item.setDispensation(null);
    }

    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(DispensationItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum DispensationStatus {
        PENDING,
        IN_PREPARATION,
        READY,
        DISPENSED,
        CANCELLED,
        PARTIALLY_DISPENSED
    }
}
