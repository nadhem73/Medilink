package com.medilinktunisia.doctorservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "doctor_educations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false, length = 200)
    private String institution; // Université ou école de médecine

    @Column(nullable = false, length = 100)
    private String degree; // MD, PhD, etc.

    @Column(length = 100)
    private String fieldOfStudy; // Medicine, Surgery, etc.

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 100)
    private String country;

    @Column(columnDefinition = "TEXT")
    private String description;
}
