package com.medilinktunisia.pharmacyservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Pharmacy - Représente une pharmacie
 * Selon cahier des charges Section 6.4 Module Pharmacies
 */
@Entity
@Table(name = "pharmacies", indexes = {
    @Index(name = "idx_pharmacy_license", columnList = "license_number"),
    @Index(name = "idx_pharmacy_city", columnList = "city"),
    @Index(name = "idx_pharmacy_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId; // Référence vers auth-service

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(nullable = false)
    private String ownerName;

    // Adresse
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String zipCode;

    private String region;

    // Contact
    @Column(nullable = false)
    private String phone;

    private String alternativePhone;

    @Column(nullable = false)
    private String email;

    // Géolocalisation
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // Horaires (format JSON ou texte)
    @Column(columnDefinition = "TEXT")
    private String openingHours;

    // Services disponibles
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean homeDelivery;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean electronicPrescriptionEnabled;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean nightService;

    // Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PharmacyStatus status = PharmacyStatus.ACTIVE;

    // Description et informations
    @Column(columnDefinition = "TEXT")
    private String description;

    private String website;

    // Statistiques
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalMedicationsSold = 0;

    @Column
    private Double rating;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer reviewsCount = 0;

    // Relations
    @OneToMany(mappedBy = "pharmacy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicationStock> medications = new ArrayList<>();

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Méthodes helper
    public void addMedication(MedicationStock medication) {
        medications.add(medication);
        medication.setPharmacy(this);
    }

    public void removeMedication(MedicationStock medication) {
        medications.remove(medication);
        medication.setPharmacy(null);
    }

    public enum PharmacyStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING_APPROVAL
    }
}
