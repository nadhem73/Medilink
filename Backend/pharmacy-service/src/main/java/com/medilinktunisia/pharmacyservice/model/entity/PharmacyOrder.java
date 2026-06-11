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
import java.util.ArrayList;
import java.util.List;

/**
 * Entité PharmacyOrder - Commande de réapprovisionnement
 * Gestion des commandes auprès des fournisseurs
 */
@Entity
@Table(name = "pharmacy_orders", indexes = {
    @Index(name = "idx_order_pharmacy", columnList = "pharmacy_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_date", columnList = "order_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PharmacyOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    // Fournisseur
    @Column(nullable = false)
    private String supplierName;

    private String supplierContact;

    private String supplierEmail;

    // Dates
    @Column(nullable = false)
    private LocalDateTime orderDate;

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime actualDeliveryDate;

    // Montant
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost;

    // Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // Détails
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PharmacyOrderItem> items = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String trackingNumber;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Méthodes helper
    public void addItem(PharmacyOrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(PharmacyOrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(PharmacyOrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (taxAmount != null) {
            this.totalAmount = this.totalAmount.add(taxAmount);
        }
        
        if (shippingCost != null) {
            this.totalAmount = this.totalAmount.add(shippingCost);
        }
    }

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
}
