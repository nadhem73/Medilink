package com.medilinktunisia.patientservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_vaccinations", indexes = {
    @Index(name = "idx_vaccination_patient", columnList = "patient_id"),
    @Index(name = "idx_vaccination_date", columnList = "vaccination_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientVaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "vaccine_name", nullable = false, length = 200)
    private String vaccineName;

    @Column(name = "vaccination_date", nullable = false)
    private LocalDate vaccinationDate;

    @Column(name = "next_dose_date")
    private LocalDate nextDoseDate;

    @Column(name = "dose_number")
    private Integer doseNumber;

    @Column(name = "administered_by_name", length = 200)
    private String administeredByName;

    @Column(name = "administration_site", length = 100)
    private String administrationSite; // Ex: "Bras gauche"

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "side_effects", columnDefinition = "TEXT")
    private String sideEffects;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
