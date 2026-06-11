package com.medilinktunisia.ambulanceservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Historique des positions GPS pour le tracking en temps réel
 */
@Entity
@Table(name = "ambulance_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ambulance_id", nullable = false)
    private Ambulance ambulance;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double speed; // Vitesse en km/h

    private Double heading; // Direction (0-360 degrés)

    private Double accuracy; // Précision GPS en mètres

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "emergency_id")
    private Emergency emergency; // Si en mission
}
