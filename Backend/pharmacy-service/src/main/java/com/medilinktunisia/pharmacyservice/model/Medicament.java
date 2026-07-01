package com.medilinktunisia.pharmacyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Médicament du catalogue national tunisien (référentiel «médicaments»).
 * Alimenté au démarrage à partir du dataset {@code data/medicaments.csv}
 * (cf. {@code MedicamentSeeder}). Sert de base au stock et aux recherches
 * de la pharmacie. Données purement référentielles : pas d'horodatage.
 */
@Entity
@Table(name = "medicaments", indexes = {
        @Index(name = "idx_medicaments_name", columnList = "name"),
        @Index(name = "idx_medicaments_dci", columnList = "dci"),
        @Index(name = "idx_medicaments_type", columnList = "type")
})
@Getter
@Setter
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom commercial du médicament (colonne «Name» du dataset). */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Dosage / concentration (colonne «Dosage», ex. «10 mg»). */
    @Column(name = "dosage", length = 60)
    private String dosage;

    /** Forme galénique (colonne «Forme», ex. «Comprimes»). */
    @Column(name = "forme", length = 100)
    private String forme;

    /** Présentation / conditionnement (colonne «Presentation», ex. «B/30»). */
    @Column(name = "presentation", length = 100)
    private String presentation;

    /** Prix public (colonne «Price», en dinars). */
    @Column(name = "price", precision = 10, scale = 3)
    private BigDecimal price;

    /** Montant remboursé par la CNAM (colonne «Remboursement»). */
    @Column(name = "remboursement", precision = 10, scale = 3)
    private BigDecimal remboursement;

    /** Dénomination Commune Internationale / principe actif (colonne «DCI»). */
    @Column(name = "dci", length = 120)
    private String dci;

    /** Classe thérapeutique (colonne «Type», ex. «Antipsychotiques»). */
    @Column(name = "type", length = 100)
    private String type;

    /** Médicament soumis à prescription (colonne «Prescription» : Oui/Non). */
    @Column(name = "prescription_required", nullable = false)
    private Boolean prescriptionRequired = false;
}
