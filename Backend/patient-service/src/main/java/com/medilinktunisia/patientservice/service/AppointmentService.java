package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.exception.AppointmentNotFoundException;
import com.medilinktunisia.patientservice.exception.PatientNotFoundException;
import com.medilinktunisia.patientservice.model.dto.AppointmentCreateRequest;
import com.medilinktunisia.patientservice.model.dto.AppointmentDto;
import com.medilinktunisia.patientservice.model.entity.Patient;
import com.medilinktunisia.patientservice.model.entity.PatientAppointment;
import com.medilinktunisia.patientservice.model.enums.AppointmentStatus;
import com.medilinktunisia.patientservice.repository.PatientAppointmentRepository;
import com.medilinktunisia.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final PatientAppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    /**
     * Créer un rendez-vous
     */
    @Transactional
    public AppointmentDto createAppointment(Long patientId, AppointmentCreateRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        PatientAppointment appointment = PatientAppointment.builder()
                .patient(patient)
                .doctorId(request.getDoctorId())
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes() != null ? 
                        request.getEstimatedDurationMinutes() : 30)
                .appointmentType(request.getAppointmentType())
                .appointmentReason(request.getAppointmentReason())
                .appointmentStatus(AppointmentStatus.SCHEDULED)
                .build();

        PatientAppointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Rendez-vous créé pour le patient: {}", patientId);

        return AppointmentDto.fromEntity(savedAppointment);
    }

    /**
     * Récupérer tous les rendez-vous d'un patient
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByDateDesc(patientId).stream()
                .map(AppointmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rendez-vous à venir
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointments(Long patientId) {
        return appointmentRepository.findUpcomingAppointments(patientId, LocalDate.now()).stream()
                .map(AppointmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer l'historique des rendez-vous
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getPastAppointments(Long patientId) {
        return appointmentRepository.findPastAppointments(patientId, LocalDate.now()).stream()
                .map(AppointmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un rendez-vous par ID
     */
    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(Long appointmentId) {
        PatientAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
        return AppointmentDto.fromEntity(appointment);
    }

    /**
     * Annuler un rendez-vous
     */
    @Transactional
    public AppointmentDto cancelAppointment(Long appointmentId, String cancellationReason, String cancelledBy) {
        PatientAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(cancellationReason);
        appointment.setCancelledBy(cancelledBy);
        appointment.setCancelledAt(LocalDateTime.now());

        PatientAppointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Rendez-vous annulé: {}", appointmentId);

        return AppointmentDto.fromEntity(updatedAppointment);
    }

    /**
     * Confirmer un rendez-vous
     */
    @Transactional
    public AppointmentDto confirmAppointment(Long appointmentId) {
        PatientAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmationSentAt(LocalDateTime.now());

        PatientAppointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Rendez-vous confirmé: {}", appointmentId);

        return AppointmentDto.fromEntity(updatedAppointment);
    }

    /**
     * Marquer un rendez-vous comme terminé
     */
    @Transactional
    public AppointmentDto completeAppointment(Long appointmentId) {
        PatientAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);

        PatientAppointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Rendez-vous terminé: {}", appointmentId);

        return AppointmentDto.fromEntity(updatedAppointment);
    }
}
