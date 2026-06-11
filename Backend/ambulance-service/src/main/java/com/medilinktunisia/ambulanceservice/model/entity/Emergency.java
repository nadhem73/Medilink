package com.medilinktunisia.ambulanceservice.model.entity;

import com.medilinktunisia.ambulanceservice.model.enums.EmergencyPriority;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyStatus;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Emergency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String emergencyCode; // Code unique de l'urgence

    // Appelant
    @Column(nullable = false)
    private String callerName;

    @Column(nullable = false)
    private String callerPhone;

    private String callerRelationToPatient; // Relation avec le patient

    // Patient
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private Long patientId; // Référence au patient si connu

    // Type et priorité
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyType emergencyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyPriority priority;

    @Column(nullable = false, length = 2000)
    private String description; // Description de l'urgence

    @Column(length = 1000)
    private String symptoms; // Symptômes observés

    // Localisation
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String locationDetails; // Détails supplémentaires (étage, code porte, etc.)

    // Ambulance assignée
    @ManyToOne
    @JoinColumn(name = "ambulance_id")
    private Ambulance assignedAmbulance;

    // Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status = EmergencyStatus.PENDING;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime assignedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime departedAt;
    private LocalDateTime completedAt;

    // Temps estimés
    private Integer estimatedArrivalMinutes;
    private Integer actualResponseTimeMinutes;

    // Hôpital de destination
    private String destinationHospital;
    private String destinationHospitalAddress;

    // Notes et observations
    @Column(length = 2000)
    private String paramedicNotes;

    @Column(length = 1000)
    private String dispatcherNotes;

    // Facturation
    private Boolean requiresPayment = false;
    private Double estimatedCost;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
