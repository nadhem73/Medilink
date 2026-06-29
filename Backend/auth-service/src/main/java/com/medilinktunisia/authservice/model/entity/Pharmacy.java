package com.medilinktunisia.authservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Pharmacie (sous-type «pharmacie» du diagramme).
 * Contient les informations d'identité et de contact de la pharmacie.
 */
@Entity
@Table(name = "pharmacies", indexes = {
        @Index(name = "idx_pharmacies_license", columnList = "license_number", unique = true)
})
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Pharmacy extends User {

    /** nom de la pharmacie dans le diagramme. */
    @Column(name = "pharmacy_name", nullable = false, length = 200)
    private String pharmacyName;

    /** adresse dans le diagramme. */
    @Column(length = 255)
    private String address;

    /** numéroLicence dans le diagramme — identifiant unique de la pharmacie. */
    @Column(name = "license_number", length = 50, unique = true)
    private String licenseNumber;

    /** horairesOuverture dans le diagramme. */
    @Column(name = "opening_hours", length = 500)
    private String openingHours;

    /** localisation (GeoPoint du diagramme) — latitude de la pharmacie. */
    @Column(name = "latitude")
    private Double latitude;

    /** localisation (GeoPoint du diagramme) — longitude de la pharmacie. */
    @Column(name = "longitude")
    private Double longitude;
}
