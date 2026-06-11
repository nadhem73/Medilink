package com.medilinktunisia.prescriptionservice.model.entity;

import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String prescriptionNumber;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String doctorSpecialty;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime prescriptionDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column
    private LocalDateTime dispensedDate;

    @Column
    private Long pharmacyId;

    @Column
    private String pharmacyName;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescriptionMedication> medications = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (prescriptionDate == null) {
            prescriptionDate = LocalDateTime.now();
        }
        if (expiryDate == null) {
            // Par défaut, l'ordonnance expire après 30 jours
            expiryDate = prescriptionDate.plusDays(30);
        }
        if (prescriptionNumber == null) {
            prescriptionNumber = generatePrescriptionNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generatePrescriptionNumber() {
        return "RX" + System.currentTimeMillis();
    }

    public void addMedication(PrescriptionMedication medication) {
        medications.add(medication);
        medication.setPrescription(this);
    }

    public void removeMedication(PrescriptionMedication medication) {
        medications.remove(medication);
        medication.setPrescription(null);
    }
}
