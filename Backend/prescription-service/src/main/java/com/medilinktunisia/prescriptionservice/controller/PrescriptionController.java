package com.medilinktunisia.prescriptionservice.controller;

import com.medilinktunisia.prescriptionservice.dto.PrescriptionCreateRequest;
import com.medilinktunisia.prescriptionservice.dto.PrescriptionResponse;
import com.medilinktunisia.prescriptionservice.service.PrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(
            HttpServletRequest request,
            @Valid @RequestBody PrescriptionCreateRequest body) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.createPrescription(doctorId, body));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getPrescription(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescription(id));
    }

    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionByConsultation(
            @PathVariable Long consultationId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByConsultation(consultationId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatient(patientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionCreateRequest body) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, doctorId, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelPrescription(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long doctorId = (Long) request.getAttribute("userId");
        prescriptionService.cancelPrescription(id, doctorId);
        return ResponseEntity.noContent().build();
    }
}
