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
 * Entité PharmacyOrderItem - Ligne de commande de réapprovisionnement
 */
@Entity
@Table(name = "pharmacy_order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_medication", columnList = "medication_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PharmacyOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PharmacyOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(nullable = false)
    private Integer quantityOrdered;

    private Integer quantityReceived;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private String batchNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Méthode pour calculer le prix total
    public void calculateTotalPrice() {
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantityOrdered));
        
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = subtotal.multiply(discountPercentage).divide(new BigDecimal(100));
            this.totalPrice = subtotal.subtract(discount);
        } else {
            this.totalPrice = subtotal;
        }
    }
}
