package com.medilinktunisia.pharmacyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Profil opérationnel d'une pharmacie (domaine «Gestion Pharmacie & Stock» du diagramme).
 * Lié à l'utilisateur de l'auth-service par son identifiant ({@code userId}).
 * Regroupe les données que la pharmacie gère depuis son panel (statut ouvert/fermé,
 * pharmacie de garde, horaires détaillés, seuil d'alerte de stock) ; son identité
 * (nom, adresse, licence) est gérée par l'auth-service dans l'entité Pharmacy.
 */
@Entity
@Table(name = "pharmacy_profiles", indexes = {
        @Index(name = "idx_pharmacy_profiles_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
public class PharmacyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant de l'utilisateur (User) côté auth-service. */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** statut ouvert/fermé — pharmacie actuellement ouverte au public. */
    @Column(nullable = false)
    private Boolean open = true;

    /** pharmacie de garde (service de nuit). */
    @Column(name = "night_duty", nullable = false)
    private Boolean nightDuty = false;

    /** Seuil d'alerte de rupture de stock (recevoirAlerteRupture du diagramme). */
    @Column(name = "stock_alert_threshold")
    private Integer stockAlertThreshold = 10;

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
