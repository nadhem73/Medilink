package com.medilinktunisia.authservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Médecin (sous-type «doctor» Medecin du diagramme).
 * Ne contient que l'<b>identité professionnelle</b> du médecin ;
 * les données gérées dans le panel (disponibilité, biographie, tarif) sont
 * gérées par le doctor-service dans l'entité DoctorProfile.
 */
@Entity
@Table(name = "doctors", indexes = {
        @Index(name = "idx_doctors_license", columnList = "license_number", unique = true)
})
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Doctor extends User {

    /** specialite dans le diagramme. */
    @Column(length = 150)
    private String specialty;

    /**
     * numeroOrdre dans le diagramme — numéro d'inscription à l'ordre des médecins.
     * Sert d'identifiant de connexion du médecin (équivalent du CIN pour le patient).
     */
    @Column(name = "license_number", length = 50, unique = true)
    private String licenseNumber;

    /** hopital dans le diagramme. */
    @Column(length = 150)
    private String hospital;
}
