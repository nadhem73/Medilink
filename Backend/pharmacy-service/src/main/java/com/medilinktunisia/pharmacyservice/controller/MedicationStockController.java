package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.dto.MedicationStockDto;
import com.medilinktunisia.pharmacyservice.service.MedicationStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class MedicationStockController {

    private final MedicationStockService medicationStockService;

    @GetMapping("/medicament/{medicamentId}/total")
    public ResponseEntity<Map<String, Object>> getTotalStock(@PathVariable Long medicamentId) {
        Integer total = medicationStockService.getTotalStock(medicamentId);
        return ResponseEntity.ok(Map.of(
                "medicamentId", medicamentId,
                "totalStock", total,
                "inStock", total > 0
        ));
    }

    @GetMapping("/medicament/{medicamentId}/lots")
    public ResponseEntity<List<MedicationStockDto>> getLots(@PathVariable Long medicamentId) {
        return ResponseEntity.ok(medicationStockService.getLotsByMedicament(medicamentId));
    }
}
