package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.dto.MedicalRecordDto;
import com.medilinktunisia.patientservice.dto.MedicalRecordRequest;
import com.medilinktunisia.patientservice.service.MedicalRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordController {

    private final MedicalRecordService service;

    /**
     * Endpoint INTERNE (service-à-service) : création du dossier médical
     * lors de l'inscription d'un patient via l'auth-service.
     */
    @PostMapping("/internal/medical-record")
    public ResponseEntity<Void> createMedicalRecord(@RequestBody MedicalRecordRequest request) {
        log.info("POST /api/patients/internal/medical-record - userId={}", request.getUserId());
        service.createMedicalRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Dossier médical du patient connecté (affiché dans le panel patient).
     * L'identifiant utilisateur est extrait du JWT par le filtre de sécurité.
     */
    @GetMapping("/me/medical-record")
    public ResponseEntity<MedicalRecordDto> getMyMedicalRecord(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("GET /api/patients/me/medical-record - userId={}", userId);
        return ResponseEntity.ok(service.getByUserId(userId));
    }
}
