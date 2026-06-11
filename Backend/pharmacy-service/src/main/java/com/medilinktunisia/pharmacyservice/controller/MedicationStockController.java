package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.model.dto.StockCreateRequest;
import com.medilinktunisia.pharmacyservice.model.dto.StockDto;
import com.medilinktunisia.pharmacyservice.model.dto.StockUpdateRequest;
import com.medilinktunisia.pharmacyservice.service.MedicationStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des stocks de médicaments
 */
@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MedicationStockController {

    private final MedicationStockService stockService;

    /**
     * Ajouter un médicament au stock
     * POST /api/pharmacy/stocks
     */
    @PostMapping
    public ResponseEntity<StockDto> addMedicationToStock(
            @Valid @RequestBody StockCreateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to add medication to stock");
        StockDto stock = stockService.addMedicationToStock(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }

    /**
     * Mettre à jour le stock
     * PUT /api/pharmacy/stocks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockDto> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to update stock: {}", id);
        StockDto stock = stockService.updateStock(id, request, currentUserId);
        return ResponseEntity.ok(stock);
    }

    /**
     * Augmenter la quantité en stock
     * PATCH /api/pharmacy/stocks/{id}/increase
     */
    @PatchMapping("/{id}/increase")
    public ResponseEntity<StockDto> increaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to increase stock: {}", id);
        Integer quantity = request.get("quantity");
        StockDto stock = stockService.increaseStock(id, quantity, currentUserId);
        return ResponseEntity.ok(stock);
    }

    /**
     * Diminuer la quantité en stock
     * PATCH /api/pharmacy/stocks/{id}/decrease
     */
    @PatchMapping("/{id}/decrease")
    public ResponseEntity<StockDto> decreaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to decrease stock: {}", id);
        Integer quantity = request.get("quantity");
        StockDto stock = stockService.decreaseStock(id, quantity, currentUserId);
        return ResponseEntity.ok(stock);
    }

    /**
     * Obtenir un stock par ID
     * GET /api/pharmacy/stocks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockDto> getStockById(@PathVariable Long id) {
        log.info("REST request to get stock: {}", id);
        StockDto stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    /**
     * Obtenir tous les stocks d'une pharmacie
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}
     */
    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<Page<StockDto>> getPharmacyStock(
            @PathVariable Long pharmacyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "medication.name") String sortBy) {
        log.info("REST request to get stock for pharmacy: {}", pharmacyId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<StockDto> stocks = stockService.getPharmacyStock(pharmacyId, pageable);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Obtenir les stocks faibles (nécessitant réapprovisionnement)
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}/low
     */
    @GetMapping("/pharmacy/{pharmacyId}/low")
    public ResponseEntity<List<StockDto>> getLowStock(@PathVariable Long pharmacyId) {
        log.info("REST request to get low stock for pharmacy: {}", pharmacyId);
        List<StockDto> stocks = stockService.getLowStock(pharmacyId);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Obtenir les stocks critiques
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}/critical
     */
    @GetMapping("/pharmacy/{pharmacyId}/critical")
    public ResponseEntity<List<StockDto>> getCriticalStock(@PathVariable Long pharmacyId) {
        log.info("REST request to get critical stock for pharmacy: {}", pharmacyId);
        List<StockDto> stocks = stockService.getCriticalStock(pharmacyId);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Obtenir les stocks en rupture
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}/out-of-stock
     */
    @GetMapping("/pharmacy/{pharmacyId}/out-of-stock")
    public ResponseEntity<List<StockDto>> getOutOfStock(@PathVariable Long pharmacyId) {
        log.info("REST request to get out of stock for pharmacy: {}", pharmacyId);
        List<StockDto> stocks = stockService.getOutOfStock(pharmacyId);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Obtenir les médicaments arrivant à expiration
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}/expiring
     */
    @GetMapping("/pharmacy/{pharmacyId}/expiring")
    public ResponseEntity<List<StockDto>> getExpiringStock(@PathVariable Long pharmacyId) {
        log.info("REST request to get expiring stock for pharmacy: {}", pharmacyId);
        List<StockDto> stocks = stockService.getExpiringStock(pharmacyId);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Obtenir les médicaments expirés
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}/expired
     */
    @GetMapping("/pharmacy/{pharmacyId}/expired")
    public ResponseEntity<List<StockDto>> getExpiredStock(@PathVariable Long pharmacyId) {
        log.info("REST request to get expired stock for pharmacy: {}", pharmacyId);
        List<StockDto> stocks = stockService.getExpiredStock(pharmacyId);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Rechercher dans le stock disponible
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}/search?term=...
     */
    @GetMapping("/pharmacy/{pharmacyId}/search")
    public ResponseEntity<Page<StockDto>> searchAvailableStock(
            @PathVariable Long pharmacyId,
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to search stock for pharmacy: {}", pharmacyId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StockDto> stocks = stockService.searchAvailableStock(pharmacyId, term, pageable);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Vérifier la disponibilité d'un médicament
     * GET /api/pharmacy/stocks/check-availability?pharmacyId=...&medicationId=...&quantity=...
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Boolean>> checkStockAvailability(
            @RequestParam Long pharmacyId,
            @RequestParam Long medicationId,
            @RequestParam Integer quantity) {
        log.info("REST request to check stock availability");
        
        boolean available = stockService.checkStockAvailability(pharmacyId, medicationId, quantity);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * Obtenir la valeur totale du stock
     * GET /api/pharmacy/stocks/pharmacy/{pharmacyId}/total-value
     */
    @GetMapping("/pharmacy/{pharmacyId}/total-value")
    public ResponseEntity<Map<String, Double>> getTotalStockValue(@PathVariable Long pharmacyId) {
        log.info("REST request to get total stock value for pharmacy: {}", pharmacyId);
        
        Double value = stockService.getTotalStockValue(pharmacyId);
        return ResponseEntity.ok(Map.of("totalValue", value));
    }

    /**
     * Supprimer un médicament du stock
     * DELETE /api/pharmacy/stocks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to delete stock: {}", id);
        stockService.deleteStock(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
