package com.medilinktunisia.prescriptionservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medilinktunisia.prescriptionservice.model.enums.MedicationFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prescription_medications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonIgnore
    private Prescription prescription;

    @Column(nullable = false)
    private String medicationName;

    @Column(nullable = false)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicationFrequency frequency;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private Integer quantityPrescribed;

    @Column
    @Builder.Default
    private Integer quantityDispensed = 0;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column
    private String medicationCode; // Code du médicament (DCI, ATC, etc.)

    @Column
    @Builder.Default
    private Boolean isSubstitutable = true; // Peut être substitué par un générique
}
