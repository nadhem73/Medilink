package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.model.dto.MedicationDto;
import com.medilinktunisia.pharmacyservice.model.entity.Medication;
import com.medilinktunisia.pharmacyservice.service.MedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des médicaments
 * Référentiel centralisé des médicaments
 */
@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
@Slf4j
public class MedicationController {

    private final MedicationService medicationService;

    /**
     * Récupérer tous les médicaments (paginé)
     * GET /api/pharmacy/medications
     */
    @GetMapping
    public ResponseEntity<Page<MedicationDto>> getAllMedications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<MedicationDto> medications = medicationService.getAllMedications(pageable);
        return ResponseEntity.ok(medications);
    }

    /**
     * Récupérer un médicament par ID
     * GET /api/pharmacy/medications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicationDto> getMedicationById(@PathVariable Long id) {
        MedicationDto medication = medicationService.getMedicationById(id);
        return ResponseEntity.ok(medication);
    }

    /**
     * Récupérer un médicament par code
     * GET /api/pharmacy/medications/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<MedicationDto> getMedicationByCode(@PathVariable String code) {
        MedicationDto medication = medicationService.getMedicationByCode(code);
        return ResponseEntity.ok(medication);
    }

    /**
     * Rechercher des médicaments
     * GET /api/pharmacy/medications/search?q=...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<MedicationDto>> searchMedications(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MedicationDto> medications = medicationService.searchMedications(q, pageable);
        return ResponseEntity.ok(medications);
    }

    /**
     * Récupérer les médicaments par catégorie
     * GET /api/pharmacy/medications/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<MedicationDto>> getMedicationsByCategory(
            @PathVariable Medication.MedicationCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MedicationDto> medications = medicationService.getMedicationsByCategory(category, pageable);
        return ResponseEntity.ok(medications);
    }

    /**
     * Récupérer les médicaments sans ordonnance
     * GET /api/pharmacy/medications/over-the-counter
     */
    @GetMapping("/over-the-counter")
    public ResponseEntity<List<MedicationDto>> getOverTheCounterMedications() {
        List<MedicationDto> medications = medicationService.getOverTheCounterMedications();
        return ResponseEntity.ok(medications);
    }
}
