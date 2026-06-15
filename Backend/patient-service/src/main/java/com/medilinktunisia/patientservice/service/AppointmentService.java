package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.dto.AppointmentDto;
import com.medilinktunisia.patientservice.dto.AppointmentRequest;
import com.medilinktunisia.patientservice.model.Appointment;
import com.medilinktunisia.patientservice.model.AppointmentMode;
import com.medilinktunisia.patientservice.model.AppointmentStatus;
import com.medilinktunisia.patientservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repository;

    /**
     * Crée un nouveau rendez-vous pour le patient.
     */
    public AppointmentDto createAppointment(Long patientId, AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(request.getDoctorId());
        appointment.setDateTime(request.getDateTime());
        appointment.setNotes(request.getNotes());

        try {
            appointment.setMode(AppointmentMode.valueOf(request.getMode().toUpperCase()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Mode de consultation invalide. Valeurs possibles: PRESENTIEL, TELECONSULTATION");
        }

        appointment.setStatus(AppointmentStatus.PENDING);

        return toDto(repository.save(appointment));
    }

    /**
     * Liste tous les rendez-vous d'un patient donné, triés par date décroissante.
     */
    public List<AppointmentDto> getPatientAppointments(Long patientId) {
        return repository.findByPatientIdOrderByDateTimeDesc(patientId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Liste tous les rendez-vous d'un médecin donné, triés par date décroissante.
     */
    public List<AppointmentDto> getDoctorAppointments(Long doctorId) {
        return repository.findByDoctorIdOrderByDateTimeDesc(doctorId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Liste les rendez-vous d'un médecin dans une plage de dates (vue calendrier).
     */
    public List<AppointmentDto> getDoctorAppointmentsForRange(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(doctorId, start, end).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Annule un rendez-vous si le patient en est le propriétaire.
     */
    public AppointmentDto cancelAppointment(Long patientId, Long appointmentId) {
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        if (!appointment.getPatientId().equals(patientId)) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à annuler ce rendez-vous");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toDto(repository.save(appointment));
    }

    /**
     * Confirme un rendez-vous si le médecin en est le destinataire.
     */
    public AppointmentDto confirmAppointment(Long doctorId, Long appointmentId) {
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à confirmer ce rendez-vous");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Impossible de confirmer un rendez-vous annulé");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return toDto(repository.save(appointment));
    }

    /**
     * Annule un rendez-vous par le médecin (depuis le panel médecin).
     */
    public AppointmentDto cancelAppointmentByDoctor(Long doctorId, Long appointmentId) {
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à annuler ce rendez-vous");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toDto(repository.save(appointment));
    }

    private AppointmentDto toDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
                .dateTime(a.getDateTime())
                .status(a.getStatus().name())
                .mode(a.getMode().name())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
