package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.dto.*;
import com.medilinktunisia.pharmacyservice.service.MedicationStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/alerts/rupture")
    public ResponseEntity<List<StockRuptureAlert>> getRuptureAlerts(
            @RequestParam(defaultValue = "20") int seuil) {
        return ResponseEntity.ok(medicationStockService.getRuptureAlerts(seuil));
    }

    @GetMapping("/alerts/perimes")
    public ResponseEntity<List<MedicationStockDto>> getPerimesAlerts(
            @RequestParam(defaultValue = "30") int jours) {
        return ResponseEntity.ok(medicationStockService.getPerimesAlerts(jours));
    }

    @PostMapping
    public ResponseEntity<MedicationStockDto> createStock(@RequestBody StockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicationStockService.createStock(request));
    }

    @GetMapping("/medicament/{medicamentId}/lots")
    public ResponseEntity<List<MedicationStockDto>> getLots(@PathVariable Long medicamentId) {
        return ResponseEntity.ok(medicationStockService.getLotsByMedicament(medicamentId));
    }

    @PostMapping("/dispenser")
    public ResponseEntity<DispensationResult> dispenserStock(@RequestBody @Valid DispensationRequest request) {
        return ResponseEntity.ok(medicationStockService.dispenserStock(request.getItems()));
    }
}
