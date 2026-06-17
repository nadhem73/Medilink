package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.dto.AppointmentDto;
import com.medilinktunisia.patientservice.dto.AppointmentRequest;
import com.medilinktunisia.patientservice.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/patients/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService service;

    /**
     * Crée un nouveau rendez-vous pour le patient connecté.
     */
    @PostMapping
    public ResponseEntity<AppointmentDto> createAppointment(
            HttpServletRequest request,
            @Valid @RequestBody AppointmentRequest body) {
        Long patientId = (Long) request.getAttribute("userId");
        log.info("POST /api/patients/appointments - patientId={}, doctorId={}", patientId, body.getDoctorId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createAppointment(patientId, body));
    }

    /**
     * Liste tous les rendez-vous du patient connecté.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getMyAppointments(HttpServletRequest request) {
        Long patientId = (Long) request.getAttribute("userId");
        log.info("GET /api/patients/appointments - patientId={}", patientId);
        return ResponseEntity.ok(service.getPatientAppointments(patientId));
    }

    /**
     * Liste tous les rendez-vous du médecin connecté.
     */
    @GetMapping("/doctor")
    public ResponseEntity<List<AppointmentDto>> getMyDoctorAppointments(HttpServletRequest request) {
        Long doctorId = (Long) request.getAttribute("userId");
        log.info("GET /api/patients/appointments/doctor - doctorId={}", doctorId);
        return ResponseEntity.ok(service.getDoctorAppointments(doctorId));
    }

    /**
     * Retourne la liste des IDs des médecins chez qui le patient a déjà un rendez-vous actif
     * (PENDING ou CONFIRMED). Utilisé par le frontend pour bloquer les doubles réservations.
     */
    @GetMapping("/active-doctor-ids")
    public ResponseEntity<List<Long>> getActiveDoctorIds(HttpServletRequest request) {
        Long patientId = (Long) request.getAttribute("userId");
        log.info("GET /api/patients/appointments/active-doctor-ids - patientId={}", patientId);
        return ResponseEntity.ok(service.getActiveDoctorIdsForPatient(patientId));
    }

    /**
     * Vérifie si un créneau est disponible pour un médecin (consultation = 30 min).
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long doctorId,
            @RequestParam String dateTime) {
        log.info("GET /api/patients/appointments/check-availability - doctorId={}, dateTime={}", doctorId, dateTime);
        try {
            service.validateTimeSlot(doctorId, LocalDateTime.parse(dateTime));
            return ResponseEntity.ok(true);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Retourne les créneaux disponibles (HH:mm) pour un médecin à une date donnée.
     */
    @GetMapping("/available-slots")
    public ResponseEntity<List<String>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam String date,
            @RequestParam String debutMatin,
            @RequestParam String finMatin,
            @RequestParam String debutApresMidi,
            @RequestParam String finApresMidi) {
        log.info("GET /api/patients/appointments/available-slots - doctorId={}, date={}", doctorId, date);
        return ResponseEntity.ok(service.getAvailableSlots(
                doctorId, date, debutMatin, finMatin, debutApresMidi, finApresMidi));
    }

    /**
     * Annule un rendez-vous du patient connecté.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDto> cancelAppointment(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long patientId = (Long) request.getAttribute("userId");
        log.info("PUT /api/patients/appointments/{}/cancel - patientId={}", id, patientId);
        return ResponseEntity.ok(service.cancelAppointment(patientId, id));
    }

    /**
     * Confirme un rendez-vous (action médecin).
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentDto> confirmAppointment(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long doctorId = (Long) request.getAttribute("userId");
        log.info("PUT /api/patients/appointments/{}/confirm - doctorId={}", id, doctorId);
        return ResponseEntity.ok(service.confirmAppointment(doctorId, id));
    }

    /**
     * Annule un rendez-vous depuis le panel médecin.
     */
    @PutMapping("/{id}/doctor-cancel")
    public ResponseEntity<AppointmentDto> cancelAppointmentByDoctor(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long doctorId = (Long) request.getAttribute("userId");
        log.info("PUT /api/patients/appointments/{}/doctor-cancel - doctorId={}", id, doctorId);
        return ResponseEntity.ok(service.cancelAppointmentByDoctor(doctorId, id));
    }
}
