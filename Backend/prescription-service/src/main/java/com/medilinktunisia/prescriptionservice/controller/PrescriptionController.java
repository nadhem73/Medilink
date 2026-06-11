package com.medilinktunisia.prescriptionservice.controller;

import com.medilinktunisia.prescriptionservice.model.dto.DispenseRequest;
import com.medilinktunisia.prescriptionservice.model.dto.PrescriptionCreateRequest;
import com.medilinktunisia.prescriptionservice.model.dto.PrescriptionDto;
import com.medilinktunisia.prescriptionservice.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Slf4j
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionDto> createPrescription(
            @Valid @RequestBody PrescriptionCreateRequest request) {
        log.info("REST request to create prescription for patient: {}", request.getPatientId());
        PrescriptionDto prescription = prescriptionService.createPrescription(request);
        return new ResponseEntity<>(prescription, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<PrescriptionDto> getPrescriptionById(@PathVariable Long id) {
        log.info("REST request to get prescription: {}", id);
        PrescriptionDto prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/number/{prescriptionNumber}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<PrescriptionDto> getPrescriptionByNumber(
            @PathVariable String prescriptionNumber) {
        log.info("REST request to get prescription by number: {}", prescriptionNumber);
        PrescriptionDto prescription = prescriptionService.getPrescriptionByNumber(prescriptionNumber);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getPatientPrescriptions(
            @PathVariable Long patientId) {
        log.info("REST request to get prescriptions for patient: {}", patientId);
        List<PrescriptionDto> prescriptions = prescriptionService.getPatientPrescriptions(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getDoctorPrescriptions(
            @PathVariable Long doctorId) {
        log.info("REST request to get prescriptions for doctor: {}", doctorId);
        List<PrescriptionDto> prescriptions = prescriptionService.getDoctorPrescriptions(doctorId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getPharmacyPrescriptions(
            @PathVariable Long pharmacyId) {
        log.info("REST request to get prescriptions for pharmacy: {}", pharmacyId);
        List<PrescriptionDto> prescriptions = prescriptionService.getPharmacyPrescriptions(pharmacyId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getActivePrescriptions() {
        log.info("REST request to get all active prescriptions");
        List<PrescriptionDto> prescriptions = prescriptionService.getActivePrescriptions();
        return ResponseEntity.ok(prescriptions);
    }

    @PostMapping("/{id}/dispense")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<PrescriptionDto> dispensePrescription(
            @PathVariable Long id,
            @Valid @RequestBody DispenseRequest request) {
        log.info("REST request to dispense prescription: {}", id);
        PrescriptionDto prescription = prescriptionService.dispensePrescription(id, request);
        return ResponseEntity.ok(prescription);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> cancelPrescription(
            @PathVariable Long id,
            @RequestParam Long doctorId) {
        log.info("REST request to cancel prescription: {}", id);
        prescriptionService.cancelPrescription(id, doctorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}/date-range")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("REST request to get prescriptions for patient {} between {} and {}", 
                patientId, startDate, endDate);
        List<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByDateRange(
                patientId, startDate, endDate);
        return ResponseEntity.ok(prescriptions);
    }

    @PostMapping("/update-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateExpiredPrescriptions() {
        log.info("REST request to update expired prescriptions");
        prescriptionService.updateExpiredPrescriptions();
        return ResponseEntity.ok().build();
    }
}
