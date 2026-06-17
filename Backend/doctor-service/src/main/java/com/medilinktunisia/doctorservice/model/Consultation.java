package com.medilinktunisia.doctorservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations", indexes = {
        @Index(name = "idx_consultations_doctor_id", columnList = "doctor_id"),
        @Index(name = "idx_consultations_patient_id", columnList = "patient_id"),
        @Index(name = "idx_consultations_status", columnList = "status"),
        @Index(name = "idx_consultations_appointment_id", columnList = "appointment_id")
})
@Getter
@Setter
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsultationType type;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(name = "blood_pressure", length = 10)
    private String bloodPressure;

    @Column(name = "pulse")
    private Integer pulse;

    @Column(name = "temperature", precision = 4, scale = 1)
    private BigDecimal temperature;

    @Column(name = "weight", precision = 5, scale = 1)
    private BigDecimal weight;

    @Column(name = "height", precision = 5, scale = 1)
    private BigDecimal height;

    @Column(name = "bmi", precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(name = "requested_exams", columnDefinition = "TEXT")
    private String requestedExams;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) {
            this.status = ConsultationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
