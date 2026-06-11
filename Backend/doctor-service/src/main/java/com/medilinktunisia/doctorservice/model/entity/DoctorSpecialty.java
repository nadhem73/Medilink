package com.medilinktunisia.doctorservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctor_specialties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false, length = 100)
    private String specialtyName; // Ex: Cardiology, Pediatrics, General Medicine

    @Column(nullable = false)
    private Boolean isPrimary; // Spécialité principale

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer yearsInSpecialty;
}
