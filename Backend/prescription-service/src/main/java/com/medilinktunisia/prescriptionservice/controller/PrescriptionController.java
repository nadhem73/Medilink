package com.medilinktunisia.prescriptionservice.controller;

import com.medilinktunisia.prescriptionservice.dto.PickupCodeResponse;
import com.medilinktunisia.prescriptionservice.dto.PrescriptionCreateRequest;
import com.medilinktunisia.prescriptionservice.dto.PrescriptionResponse;
import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
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
        return prescriptionService.getPrescriptionByConsultation(consultationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatient(patientId));
    }

    @GetMapping
    public ResponseEntity<List<PrescriptionResponse>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionCreateRequest body) {
        Long doctorId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, doctorId, body));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PrescriptionResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody PrescriptionController.StatusRequest body) {
        return ResponseEntity.ok(prescriptionService.updateStatus(id, PrescriptionStatus.valueOf(body.status())));
    }

    private record StatusRequest(String status) {}

    @PutMapping("/{id}/assign-pharmacy")
    public ResponseEntity<PrescriptionResponse> assignPharmacy(
            @PathVariable Long id,
            @RequestBody AssignPharmacyRequest body) {
        return ResponseEntity.ok(prescriptionService.assignToPharmacy(id, body.pharmacyId()));
    }

    private record AssignPharmacyRequest(Long pharmacyId) {}

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<PrescriptionResponse>> getByPharmacy(
            @PathVariable Long pharmacyId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPharmacy(pharmacyId));
    }

    @PostMapping("/{id}/pickup-code")
    public ResponseEntity<PickupCodeResponse> storePickupCode(
            @PathVariable Long id,
            @RequestBody StoreCodeRequest body) {
        return ResponseEntity.ok(prescriptionService.storePickupCode(id, body.code()));
    }

    private record StoreCodeRequest(String code) {}

    @GetMapping("/{id}/pickup-code")
    public ResponseEntity<PickupCodeResponse> getPickupCode(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPickupCode(id));
    }

    @PostMapping("/{id}/validate-pickup")
    public ResponseEntity<PrescriptionResponse> validatePickup(
            @PathVariable Long id,
            @RequestBody ValidatePickupRequest body) {
        return ResponseEntity.ok(prescriptionService.validatePickupCode(id, body.code()));
    }

    private record ValidatePickupRequest(String code) {}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelPrescription(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long doctorId = (Long) request.getAttribute("userId");
        prescriptionService.cancelPrescription(id, doctorId);
        return ResponseEntity.noContent().build();
    }
}
