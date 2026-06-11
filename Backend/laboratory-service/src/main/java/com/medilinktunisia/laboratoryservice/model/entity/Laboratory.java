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
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Laboratory - Représente un laboratoire d'analyses médicales
 * Selon cahier des charges Section 6.5 Module Laboratoires
 */
@Entity
@Table(name = "laboratories", indexes = {
    @Index(name = "idx_laboratory_license", columnList = "license_number"),
    @Index(name = "idx_laboratory_city", columnList = "city"),
    @Index(name = "idx_laboratory_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Laboratory {

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
    private String directorName;

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

    // Horaires
    @Column(columnDefinition = "TEXT")
    private String openingHours;

    // Services et accréditations
    @Column(columnDefinition = "TEXT")
    private String accreditations; // Accréditations et certifications

    @Column(columnDefinition = "TEXT")
    private String specialties; // Spécialités du laboratoire

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean homeCollection = true; // Prélèvement à domicile

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean urgentAnalysisAvailable = true;

    // Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LaboratoryStatus status = LaboratoryStatus.ACTIVE;

    // Description
    @Column(columnDefinition = "TEXT")
    private String description;

    private String website;

    // Statistiques
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalAnalyses = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalPatients = 0;

    @Column
    private Double rating;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer reviewsCount = 0;

    // Délais moyens
    @Column
    private Integer averageTurnaroundTimeHours; // Délai moyen de rendu des résultats

    // Relations
    @OneToMany(mappedBy = "laboratory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnalysisRequest> analysisRequests = new ArrayList<>();

    // Audit
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum LaboratoryStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING_APPROVAL
    }
}
