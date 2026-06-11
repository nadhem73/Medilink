package com.medilinktunisia.teleconsultationservice.controller;

import com.medilinktunisia.teleconsultationservice.model.dto.ConsultationCreateRequest;
import com.medilinktunisia.teleconsultationservice.model.dto.ConsultationDto;
import com.medilinktunisia.teleconsultationservice.service.TeleconsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teleconsultations")
@RequiredArgsConstructor
@Slf4j
public class TeleconsultationController {

    private final TeleconsultationService teleconsultationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<ConsultationDto> createConsultation(
            @Valid @RequestBody ConsultationCreateRequest request) {
        log.info("REST request to create teleconsultation");
        ConsultationDto consultation = teleconsultationService.createConsultation(request);
        return new ResponseEntity<>(consultation, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<ConsultationDto> getConsultationById(@PathVariable Long id) {
        log.info("REST request to get consultation: {}", id);
        ConsultationDto consultation = teleconsultationService.getConsultationById(id);
        return ResponseEntity.ok(consultation);
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<ConsultationDto> getConsultationBySessionId(
            @PathVariable String sessionId) {
        log.info("REST request to get consultation by session ID: {}", sessionId);
        ConsultationDto consultation = teleconsultationService.getConsultationBySessionId(sessionId);
        return ResponseEntity.ok(consultation);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getPatientConsultations(
            @PathVariable Long patientId) {
        log.info("REST request to get consultations for patient: {}", patientId);
        List<ConsultationDto> consultations = teleconsultationService.getPatientConsultations(patientId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getDoctorConsultations(
            @PathVariable Long doctorId) {
        log.info("REST request to get consultations for doctor: {}", doctorId);
        List<ConsultationDto> consultations = teleconsultationService.getDoctorConsultations(doctorId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/upcoming/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getUpcomingConsultations(
            @PathVariable Long userId) {
        log.info("REST request to get upcoming consultations for user: {}", userId);
        List<ConsultationDto> consultations = teleconsultationService.getUpcomingConsultations(userId);
        return ResponseEntity.ok(consultations);
    }

    @PostMapping("/{sessionId}/start")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<ConsultationDto> startConsultation(
            @PathVariable String sessionId,
            @RequestParam Long userId) {
        log.info("REST request to start consultation: {}", sessionId);
        ConsultationDto consultation = teleconsultationService.startConsultation(sessionId, userId);
        return ResponseEntity.ok(consultation);
    }

    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<ConsultationDto> endConsultation(
            @PathVariable String sessionId,
            @RequestParam Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        log.info("REST request to end consultation: {}", sessionId);
        String notes = body != null ? body.get("notes") : null;
        ConsultationDto consultation = teleconsultationService.endConsultation(sessionId, userId, notes);
        return ResponseEntity.ok(consultation);
    }

    @PostMapping("/{sessionId}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Void> cancelConsultation(
            @PathVariable String sessionId,
            @RequestParam Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        log.info("REST request to cancel consultation: {}", sessionId);
        String reason = body != null ? body.get("reason") : "No reason provided";
        teleconsultationService.cancelConsultation(sessionId, userId, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/join")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Void> joinSession(
            @PathVariable String sessionId,
            @RequestParam Long userId) {
        log.info("REST request to join session: {}", sessionId);
        teleconsultationService.joinSession(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/leave")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Void> leaveSession(
            @PathVariable String sessionId,
            @RequestParam Long userId) {
        log.info("REST request to leave session: {}", sessionId);
        teleconsultationService.leaveSession(sessionId, userId);
        return ResponseEntity.ok().build();
    }
}
