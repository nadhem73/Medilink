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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité MedicationStock - Gestion du stock de médicaments par pharmacie
 * Selon cahier des charges Section 6.4 Module Pharmacies - Gestion des stocks
 */
@Entity
@Table(name = "medication_stocks", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"pharmacy_id", "medication_id"}),
    indexes = {
        @Index(name = "idx_stock_pharmacy", columnList = "pharmacy_id"),
        @Index(name = "idx_stock_medication", columnList = "medication_id"),
        @Index(name = "idx_stock_quantity", columnList = "quantity"),
        @Index(name = "idx_stock_expiry", columnList = "expiry_date")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MedicationStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    // Quantité en stock
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer minimumStockLevel = 10;

    @Column(nullable = false)
    @Builder.Default
    private Integer reorderLevel = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer maximumStockLevel = 500;

    // Prix spécifique à la pharmacie (peut différer du prix du référentiel)
    @Column(precision = 10, scale = 3)
    private BigDecimal sellingPrice;

    @Column(precision = 10, scale = 3)
    private BigDecimal purchasePrice;

    // Lot et expiration
    private String batchNumber;

    private LocalDate expiryDate;

    // Localisation dans la pharmacie
    private String shelfLocation;

    // Statut de disponibilité
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockStatus status = StockStatus.IN_STOCK;

    // Alertes
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean lowStockAlertSent = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean outOfStockAlertSent = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean expiryAlertSent = false;

    // Dernière commande
    private LocalDateTime lastOrderDate;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer lastOrderQuantity = 0;

    // Statistiques
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalSold = 0;

    private LocalDateTime lastSaleDate;

    // Notes
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Méthodes helper
    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            this.totalSold += amount;
            this.lastSaleDate = LocalDateTime.now();
            updateStatus();
        } else {
            throw new IllegalStateException("Quantité insuffisante en stock");
        }
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
        updateStatus();
    }

    public void updateStatus() {
        if (this.quantity == 0) {
            this.status = StockStatus.OUT_OF_STOCK;
        } else if (this.quantity <= this.minimumStockLevel) {
            this.status = StockStatus.CRITICAL;
        } else if (this.quantity <= this.reorderLevel) {
            this.status = StockStatus.LOW;
        } else {
            this.status = StockStatus.IN_STOCK;
        }
    }

    public boolean needsReorder() {
        return this.quantity <= this.reorderLevel;
    }

    public boolean isExpiringSoon() {
        if (this.expiryDate == null) {
            return false;
        }
        return this.expiryDate.isBefore(LocalDate.now().plusMonths(3));
    }

    public boolean isExpired() {
        if (this.expiryDate == null) {
            return false;
        }
        return this.expiryDate.isBefore(LocalDate.now());
    }

    public enum StockStatus {
        IN_STOCK,
        LOW,
        CRITICAL,
        OUT_OF_STOCK,
        EXPIRED
    }
}
