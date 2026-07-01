package com.medilinktunisia.prescriptionservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prescription_items", indexes = {
        @Index(name = "idx_prescription_items_prescription", columnList = "prescription_id")
})
@Getter
@Setter
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_prescription"))
    private Prescription prescription;

    @Column(name = "medicament_id", nullable = false)
    private Long medicamentId;

    @Column(name = "medicament_name", length = 150, nullable = false)
    private String medicamentName;

    @Column(length = 60)
    private String dosage;

    @Column(length = 100)
    private String forme;

    @Column(length = 255)
    private String posologie;

    @Column(name = "duree_traitement")
    private Integer dureeTraitement;

    @Column(name = "voie_administration", length = 50)
    private String voieAdministration;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "quantite_prescrite")
    private Integer quantitePrescrite;
}
