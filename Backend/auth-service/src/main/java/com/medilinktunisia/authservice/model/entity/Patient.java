package com.medilinktunisia.authservice.model.entity;

import com.medilinktunisia.authservice.model.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Patient (sous-type «patient» du diagramme).
 * Ne contient que les informations d'<b>identité</b> du patient ;
 * les données purement médicales (groupe sanguin, allergies, etc.) sont
 * gérées par le patient-service dans l'entité MedicalRecord.
 */
@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patients_cin", columnList = "cin", unique = true)
})
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Patient extends User {

    /** dateNaissance dans le diagramme. */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** sexe dans le diagramme. */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 255)
    private String address;

    /** Carte d'identité nationale (CIN) — unique lorsqu'elle est renseignée. */
    @Column(length = 20, unique = true)
    private String cin;
}
