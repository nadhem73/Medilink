package com.medilinktunisia.patientservice.model.entity;

import com.medilinktunisia.patientservice.model.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // Informations médicales
    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 10)
    private BloodGroup bloodGroup;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    // Notes générales
    @Column(name = "general_medical_notes", columnDefinition = "TEXT")
    private String generalMedicalNotes;

    // Métadonnées
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthode helper pour calculer l'IMC (BMI)
    public BigDecimal calculateBMI() {
        if (weightKg == null || heightCm == null || heightCm.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        // BMI = poids(kg) / (taille(m))²
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100));
        return weightKg.divide(heightM.multiply(heightM), 2, BigDecimal.ROUND_HALF_UP);
    }
}
