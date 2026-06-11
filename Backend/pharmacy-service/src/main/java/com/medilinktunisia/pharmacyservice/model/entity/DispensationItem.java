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

/**
 * Entité DispensationItem - Ligne de dispensation d'ordonnance
 */
@Entity
@Table(name = "dispensation_items", indexes = {
    @Index(name = "idx_disp_item_dispensation", columnList = "dispensation_id"),
    @Index(name = "idx_disp_item_medication", columnList = "medication_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DispensationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensation_id", nullable = false)
    private PrescriptionDispensation dispensation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private MedicationStock stock;

    // Quantités
    @Column(nullable = false)
    private Integer quantityPrescribed;

    @Column(nullable = false)
    private Integer quantityDispensed;

    // Prix
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal reimbursementAmount;

    // Instructions
    @Column(columnDefinition = "TEXT")
    private String dosageInstructions;

    @Column(columnDefinition = "TEXT")
    private String pharmacistInstructions;

    // Substitution
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean substituted = false;

    private Long originalMedicationId; // Si substitution

    @Column(columnDefinition = "TEXT")
    private String substitutionReason;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Méthode pour calculer le prix total
    public void calculateTotalPrice() {
        this.totalPrice = unitPrice.multiply(new BigDecimal(quantityDispensed));
    }
}
