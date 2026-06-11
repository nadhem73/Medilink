package com.medilinktunisia.doctorservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "doctor_experiences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false, length = 200)
    private String hospital; // Hôpital ou clinique

    @Column(nullable = false, length = 100)
    private String position; // Poste occupé

    @Column(length = 100)
    private String department; // Service

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean isCurrent; // Poste actuel

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(columnDefinition = "TEXT")
    private String description;
}
