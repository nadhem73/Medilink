package com.medilinktunisia.doctorservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Profil opérationnel d'un médecin (sous-type «doctor» Medecin du diagramme).
 * Lié à l'utilisateur de l'auth-service par son identifiant ({@code userId}).
 * Regroupe les données que le médecin gère depuis son panel (disponibilité,
 * biographie, tarif) ; son identité professionnelle (spécialité, numéro d'ordre,
 * hôpital) est gérée par l'auth-service dans l'entité Doctor.
 */
@Entity
@Table(name = "doctor_profiles", indexes = {
        @Index(name = "idx_doctor_profiles_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant de l'utilisateur (User) côté auth-service. */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** disponibilite dans le diagramme. */
    @Column(nullable = false)
    private Boolean available = true;

    /** biographie dans le diagramme. */
    @Column(columnDefinition = "TEXT")
    private String biography;

    /** tarif (Decimal) dans le diagramme — montant de la consultation. */
    @Column(precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "debut_matin", length = 5)
    private String debutMatin;

    @Column(name = "fin_matin", length = 5)
    private String finMatin;

    @Column(name = "debut_apres_midi", length = 5)
    private String debutApresMidi;

    @Column(name = "fin_apres_midi", length = 5)
    private String finApresMidi;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
