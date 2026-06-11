package com.medilinktunisia.ambulanceservice.model.entity;

import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ambulances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String registrationNumber; // Matricule

    @Column(nullable = false)
    private String vehicleModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmbulanceStatus status = AmbulanceStatus.AVAILABLE;

    // Géolocalisation
    @Column(nullable = false)
    private Double currentLatitude;

    @Column(nullable = false)
    private Double currentLongitude;

    // Station de base
    @Column(nullable = false)
    private String baseStation;

    private Double baseLatitude;
    private Double baseLongitude;

    // Équipement
    @Column(length = 1000)
    private String equipment; // Liste des équipements disponibles

    private Boolean hasDefibrillator = false;
    private Boolean hasOxygenSupply = false;
    private Boolean hasAdvancedLifeSupport = false;

    // Équipage
    private String driverName;
    private String driverPhone;
    private String paramedicName;
    private String paramedicPhone;

    // Capacité
    private Integer capacity = 1; // Nombre de patients transportables

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;

    @Column(length = 500)
    private String notes;
}
