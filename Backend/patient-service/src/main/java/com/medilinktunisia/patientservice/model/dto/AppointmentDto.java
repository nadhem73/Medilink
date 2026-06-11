package com.medilinktunisia.patientservice.model.dto;

import com.medilinktunisia.patientservice.model.entity.PatientAppointment;
import com.medilinktunisia.patientservice.model.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private Integer estimatedDurationMinutes;
    private String appointmentType;
    private String appointmentReason;
    private AppointmentStatus appointmentStatus;
    private LocalDateTime confirmationSentAt;
    private LocalDateTime reminderSentAt;
    private String cancellationReason;
    private String cancelledBy;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppointmentDto fromEntity(PatientAppointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .doctorId(appointment.getDoctorId())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .estimatedDurationMinutes(appointment.getEstimatedDurationMinutes())
                .appointmentType(appointment.getAppointmentType())
                .appointmentReason(appointment.getAppointmentReason())
                .appointmentStatus(appointment.getAppointmentStatus())
                .confirmationSentAt(appointment.getConfirmationSentAt())
                .reminderSentAt(appointment.getReminderSentAt())
                .cancellationReason(appointment.getCancellationReason())
                .cancelledBy(appointment.getCancelledBy())
                .cancelledAt(appointment.getCancelledAt())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
