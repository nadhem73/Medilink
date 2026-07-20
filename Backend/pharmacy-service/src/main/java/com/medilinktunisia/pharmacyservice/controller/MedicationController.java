package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.dto.MedicamentRequest;
import com.medilinktunisia.pharmacyservice.dto.MedicationDto;
import com.medilinktunisia.pharmacyservice.service.MedicationImageResolver;
import com.medilinktunisia.pharmacyservice.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/medicaments")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;
    private final MedicationImageResolver medicationImageResolver;

    @GetMapping
    public ResponseEntity<Page<MedicationDto>> getAllMedicaments(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(medicationService.getAllMedicaments(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MedicationDto>> searchMedicaments(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(medicationService.searchByName(name, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationDto> getMedicament(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getById(id));
    }

    @PostMapping("/stock-check")
    public ResponseEntity<Map<Long, Integer>> checkStock(@RequestBody List<Long> medicamentIds) {
        return ResponseEntity.ok(medicationService.getStockForMedicaments(medicamentIds));
    }

    @PostMapping
    public ResponseEntity<MedicationDto> createMedicament(@RequestBody MedicamentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicationService.createMedicament(request));
    }

    @PostMapping("/resolve-images")
    public ResponseEntity<Map<String, String>> resolveImages() {
        CompletableFuture.runAsync(() -> medicationImageResolver.resolveAll());
        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "message", "Résolution d'images en arrière-plan."
        ));
    }

    @DeleteMapping("/reset-images")
    public ResponseEntity<Map<String, String>> resetImages() {
        medicationImageResolver.resetAll();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Toutes les image_url ont été réinitialisées."
        ));
    }
}
