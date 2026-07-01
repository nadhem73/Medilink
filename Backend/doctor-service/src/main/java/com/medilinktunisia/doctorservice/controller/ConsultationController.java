package com.medilinktunisia.doctorservice.controller;

import com.medilinktunisia.doctorservice.dto.ConsultationRequest;
import com.medilinktunisia.doctorservice.dto.ConsultationResponse;
import com.medilinktunisia.doctorservice.service.ConsultationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService service;

    @GetMapping("/today")
    public ResponseEntity<List<ConsultationResponse>> getTodayConsultations(HttpServletRequest request) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.getTodayConsultations(doctorId));
    }

    @GetMapping
    public ResponseEntity<List<ConsultationResponse>> getAllConsultations(
            HttpServletRequest request,
            @RequestParam(required = false) String status) {
        Long doctorId = (Long) request.getAttribute("userId");
        if (status != null) {
            return ResponseEntity.ok(service.getConsultationsByStatus(doctorId, status));
        }
        return ResponseEntity.ok(service.getAllConsultations(doctorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> getConsultation(@PathVariable Long id) {
        return ResponseEntity.ok(service.getConsultation(id));
    }

    @PostMapping
    public ResponseEntity<ConsultationResponse> startConsultation(
            HttpServletRequest request,
            @RequestBody ConsultationRequest body) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.startConsultation(doctorId, body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultationResponse> updateConsultation(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody ConsultationRequest body) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.updateConsultation(id, doctorId, body));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ConsultationResponse> completeConsultation(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody ConsultationRequest body) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.completeConsultation(id, doctorId, body));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ConsultationResponse>> getConsultationsByPatient(
            HttpServletRequest request,
            @PathVariable Long patientId) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.getConsultationsByPatient(doctorId, patientId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelConsultation(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long doctorId = (Long) request.getAttribute("userId");
        service.cancelConsultation(id, doctorId);
        return ResponseEntity.noContent().build();
    }
}
