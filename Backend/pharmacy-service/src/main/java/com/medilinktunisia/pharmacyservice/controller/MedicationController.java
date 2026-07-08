package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.dto.MedicationDto;
import com.medilinktunisia.pharmacyservice.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medicaments")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

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
}
