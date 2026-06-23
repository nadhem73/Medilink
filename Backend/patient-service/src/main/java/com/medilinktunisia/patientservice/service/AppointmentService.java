package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.dto.AppointmentDto;
import com.medilinktunisia.patientservice.dto.AppointmentRequest;
import com.medilinktunisia.patientservice.model.Appointment;
import com.medilinktunisia.patientservice.model.AppointmentMode;
import com.medilinktunisia.patientservice.model.AppointmentStatus;
import com.medilinktunisia.patientservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository repository;

    /**
     * Crée un nouveau rendez-vous pour le patient.
     */
    public AppointmentDto createAppointment(Long patientId, AppointmentRequest request) {
        log.info("Creating appointment for patientId={} with doctorId={} at {}", patientId, request.getDoctorId(), request.getDateTime());

        // Vérifier que le patient n'a pas déjà un rendez-vous actif chez ce médecin
        boolean alreadyBooked = repository.existsByPatientIdAndDoctorIdAndStatusIn(
                patientId,
                request.getDoctorId(),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
        );
        if (alreadyBooked) {
            log.warn("PatientId={} already has an active appointment with doctorId={}", patientId, request.getDoctorId());
            throw new IllegalStateException("Vous avez déjà un rendez-vous en cours avec ce médecin. Veuillez annuler ou attendre la consultation.");
        }

        // Vérifier que le créneau est libre (consultation = 30 min)
        validateTimeSlot(request.getDoctorId(), request.getDateTime());

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(request.getDoctorId());
        appointment.setDateTime(request.getDateTime());
        appointment.setNotes(request.getNotes());

        try {
            appointment.setMode(AppointmentMode.valueOf(request.getMode().toUpperCase()));
        } catch (Exception e) {
            log.error("Invalid appointment mode '{}' for patientId={}", request.getMode(), patientId);
            throw new IllegalArgumentException("Mode de consultation invalide. Valeurs possibles: PRESENTIEL, TELECONSULTATION");
        }

        appointment.setStatus(AppointmentStatus.PENDING);

        AppointmentDto saved = toDto(repository.save(appointment));
        log.info("Appointment created successfully with id={} for patientId={}", saved.getId(), patientId);
        return saved;
    }

    /**
     * Crée un rendez-vous de suivi (initié par le médecin).
     * Contourne la vérification de double réservation.
     */
    public AppointmentDto createFollowUpAppointment(Long doctorId, Long patientId, LocalDateTime dateTime) {
        validateTimeSlot(doctorId, dateTime);

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setDateTime(dateTime);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setNotes("Consultation de suivi");
        appointment.setStatus(AppointmentStatus.CONFIRMED);

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
        log.info("PatientId={} cancelling appointment id={}", patientId, appointmentId);
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment id={} not found for cancellation by patientId={}", appointmentId, patientId);
                    return new IllegalArgumentException("Rendez-vous introuvable");
                });

        if (!appointment.getPatientId().equals(patientId)) {
            log.warn("PatientId={} not authorized to cancel appointment id={}", patientId, appointmentId);
            throw new IllegalStateException("Vous n'êtes pas autorisé à annuler ce rendez-vous");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        AppointmentDto cancelled = toDto(repository.save(appointment));
        log.info("Appointment id={} cancelled by patientId={}", appointmentId, patientId);
        return cancelled;
    }

    /**
     * Confirme un rendez-vous si le médecin en est le destinataire.
     */
    public AppointmentDto confirmAppointment(Long doctorId, Long appointmentId) {
        log.info("DoctorId={} confirming appointment id={}", doctorId, appointmentId);
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment id={} not found for confirmation by doctorId={}", appointmentId, doctorId);
                    return new IllegalArgumentException("Rendez-vous introuvable");
                });

        if (!appointment.getDoctorId().equals(doctorId)) {
            log.warn("DoctorId={} not authorized to confirm appointment id={}", doctorId, appointmentId);
            throw new IllegalStateException("Vous n'êtes pas autorisé à confirmer ce rendez-vous");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            log.warn("Cannot confirm cancelled appointment id={} by doctorId={}", appointmentId, doctorId);
            throw new IllegalStateException("Impossible de confirmer un rendez-vous annulé");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        AppointmentDto confirmed = toDto(repository.save(appointment));
        log.info("Appointment id={} confirmed by doctorId={}", appointmentId, doctorId);
        return confirmed;
    }

    /**
     * Annule un rendez-vous par le médecin (depuis le panel médecin).
     */
    public AppointmentDto cancelAppointmentByDoctor(Long doctorId, Long appointmentId) {
        log.info("DoctorId={} cancelling appointment id={}", doctorId, appointmentId);
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment id={} not found for doctor-cancellation by doctorId={}", appointmentId, doctorId);
                    return new IllegalArgumentException("Rendez-vous introuvable");
                });

        if (!appointment.getDoctorId().equals(doctorId)) {
            log.warn("DoctorId={} not authorized to cancel appointment id={}", doctorId, appointmentId);
            throw new IllegalStateException("Vous n'êtes pas autorisé à annuler ce rendez-vous");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        AppointmentDto cancelled = toDto(repository.save(appointment));
        log.info("Appointment id={} cancelled by doctorId={}", appointmentId, doctorId);
        return cancelled;
    }

    /**
     * Marque un rendez-vous comme terminé après la finalisation de la consultation.
     */
    public AppointmentDto completeAppointment(Long doctorId, Long appointmentId) {
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à modifier ce rendez-vous");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Impossible de terminer un rendez-vous annulé");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return toDto(repository.save(appointment));
    }

    /**
     * Retourne la liste des IDs des médecins chez qui le patient a déjà un rendez-vous actif
     * (PENDING ou CONFIRMED). Utile pour empêcher les doubles réservations chez le même médecin.
     */
    public List<Long> getActiveDoctorIdsForPatient(Long patientId) {
        return repository.findActiveDoctorIdsByPatientId(
                patientId,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
        );
    }

    /**
     * Retourne la liste des créneaux disponibles (format HH:mm) pour un médecin
     * à une date donnée, selon ses horaires de travail (tranches de 30 min).
     */
    public List<String> getAvailableSlots(Long doctorId, String date,
                                          String debutMatin, String finMatin,
                                          String debutApresMidi, String finApresMidi) {
        List<String> slots = new java.util.ArrayList<>();

        // Générer les créneaux pour les deux plages horaires
        slots.addAll(generateSlots(debutMatin, finMatin, date));
        slots.addAll(generateSlots(debutApresMidi, finApresMidi, date));

        // Exclure les créneaux déjà réservés (PENDING ou CONFIRMED)
        LocalDateTime dayStart = LocalDateTime.parse(date + "T00:00:00");
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Appointment> booked = repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(doctorId, dayStart, dayEnd)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .toList();

        return slots.stream()
                .filter(slot -> {
                    LocalDateTime slotStart = LocalDateTime.parse(date + "T" + slot + ":00");
                    LocalDateTime slotEnd = slotStart.plusMinutes(30);
                    return booked.stream().noneMatch(app ->
                            app.getDateTime().isBefore(slotEnd) &&
                            app.getDateTime().plusMinutes(30).isAfter(slotStart));
                })
                .toList();
    }

    private List<String> generateSlots(String debut, String fin, String date) {
        List<String> slots = new java.util.ArrayList<>();
        try {
            java.time.LocalTime start = java.time.LocalTime.parse(debut);
            java.time.LocalTime end = java.time.LocalTime.parse(fin);
            while (start.isBefore(end)) {
                slots.add(start.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                start = start.plusMinutes(30);
            }
        } catch (Exception ignored) {}
        return slots;
    }

    /**
     * Vérifie qu'aucun rendez-vous actif n'occupe le créneau (consultation = 30 min).
     */
    public void validateTimeSlot(Long doctorId, LocalDateTime dateTime) {
        LocalDateTime start = dateTime.minusMinutes(29);
        LocalDateTime end = dateTime.plusMinutes(30);
        boolean overlap = repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(doctorId, start, end)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .findFirst()
                .isPresent();
        if (overlap) {
            throw new IllegalStateException("Ce créneau est déjà réservé. La durée d'une consultation est de 30 minutes.");
        }
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
