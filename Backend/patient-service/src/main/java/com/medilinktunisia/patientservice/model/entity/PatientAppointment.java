package com.medilinktunisia.patientservice.model.entity;

import com.medilinktunisia.patientservice.model.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "patient_appointments", indexes = {
    @Index(name = "idx_appointment_patient", columnList = "patient_id"),
    @Index(name = "idx_appointment_doctor", columnList = "doctor_id"),
    @Index(name = "idx_appointment_date", columnList = "appointment_date"),
    @Index(name = "idx_appointment_status", columnList = "appointment_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId; // Référence vers doctor-service

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Column(name = "estimated_duration_minutes")
    @Builder.Default
    private Integer estimatedDurationMinutes = 30;

    @Column(name = "appointment_type", length = 50)
    private String appointmentType; // CONSULTATION, FOLLOW_UP, EMERGENCY, TELECONSULTATION

    @Column(name = "appointment_reason", length = 500)
    private String appointmentReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_status", length = 50, nullable = false)
    @Builder.Default
    private AppointmentStatus appointmentStatus = AppointmentStatus.SCHEDULED;

    @Column(name = "confirmation_sent_at")
    private LocalDateTime confirmationSentAt;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_by", length = 50)
    private String cancelledBy; // PATIENT, DOCTOR, SYSTEM

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthode helper pour vérifier si le rendez-vous est passé
    public boolean isPast() {
        return LocalDate.now().isAfter(appointmentDate) ||
                (LocalDate.now().isEqual(appointmentDate) && LocalTime.now().isAfter(appointmentTime));
    }

    // Méthode helper pour vérifier si le rendez-vous est dans moins de 24h
    public boolean isWithin24Hours() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        LocalDateTime now = LocalDateTime.now();
        return appointmentDateTime.isAfter(now) && appointmentDateTime.minusHours(24).isBefore(now);
    }
}
