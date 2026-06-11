package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.model.dto.PharmacyCreateRequest;
import com.medilinktunisia.pharmacyservice.model.dto.PharmacyDto;
import com.medilinktunisia.pharmacyservice.model.dto.PharmacyUpdateRequest;
import com.medilinktunisia.pharmacyservice.model.entity.Pharmacy;
import com.medilinktunisia.pharmacyservice.service.PharmacyService;
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
 * Contrôleur REST pour la gestion des pharmacies
 */
@RestController
@RequestMapping("/pharmacies")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    /**
     * Créer une nouvelle pharmacie
     * POST /api/pharmacy/pharmacies
     */
    @PostMapping
    public ResponseEntity<PharmacyDto> createPharmacy(
            @Valid @RequestBody PharmacyCreateRequest request) {
        log.info("REST request to create pharmacy: {}", request.getName());
        PharmacyDto pharmacy = pharmacyService.createPharmacy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacy);
    }

    /**
     * Mettre à jour une pharmacie
     * PUT /api/pharmacy/pharmacies/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PharmacyDto> updatePharmacy(
            @PathVariable Long id,
            @Valid @RequestBody PharmacyUpdateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to update pharmacy: {}", id);
        PharmacyDto pharmacy = pharmacyService.updatePharmacy(id, request, currentUserId);
        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Obtenir une pharmacie par ID
     * GET /api/pharmacy/pharmacies/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PharmacyDto> getPharmacyById(@PathVariable Long id) {
        log.info("REST request to get pharmacy: {}", id);
        PharmacyDto pharmacy = pharmacyService.getPharmacyById(id);
        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Obtenir la pharmacie d'un utilisateur
     * GET /api/pharmacy/pharmacies/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PharmacyDto> getPharmacyByUserId(@PathVariable Long userId) {
        log.info("REST request to get pharmacy for user: {}", userId);
        PharmacyDto pharmacy = pharmacyService.getPharmacyByUserId(userId);
        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Obtenir la pharmacie de l'utilisateur connecté
     * GET /api/pharmacy/pharmacies/my-pharmacy
     */
    @GetMapping("/my-pharmacy")
    public ResponseEntity<PharmacyDto> getMyPharmacy(
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to get my pharmacy for user: {}", currentUserId);
        PharmacyDto pharmacy = pharmacyService.getPharmacyByUserId(currentUserId);
        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Obtenir toutes les pharmacies (avec pagination)
     * GET /api/pharmacy/pharmacies?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<Page<PharmacyDto>> getAllPharmacies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("REST request to get all pharmacies - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PharmacyDto> pharmacies = pharmacyService.getAllPharmacies(pageable);
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Rechercher des pharmacies par nom
     * GET /api/pharmacy/pharmacies/search?name=...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PharmacyDto>> searchPharmacies(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to search pharmacies by name: {}", name);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PharmacyDto> pharmacies = pharmacyService.searchPharmaciesByName(name, pageable);
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir les pharmacies par ville
     * GET /api/pharmacy/pharmacies/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<PharmacyDto>> getPharmaciesByCity(@PathVariable String city) {
        log.info("REST request to get pharmacies in city: {}", city);
        List<PharmacyDto> pharmacies = pharmacyService.getPharmaciesByCity(city);
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir les pharmacies actives par ville
     * GET /api/pharmacy/pharmacies/city/{city}/active
     */
    @GetMapping("/city/{city}/active")
    public ResponseEntity<List<PharmacyDto>> getActivePharmaciesByCity(@PathVariable String city) {
        log.info("REST request to get active pharmacies in city: {}", city);
        List<PharmacyDto> pharmacies = pharmacyService.getActivePharmaciesByCity(city);
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir les pharmacies de garde (service de nuit)
     * GET /api/pharmacy/pharmacies/night-service
     */
    @GetMapping("/night-service")
    public ResponseEntity<List<PharmacyDto>> getNightServicePharmacies() {
        log.info("REST request to get night service pharmacies");
        List<PharmacyDto> pharmacies = pharmacyService.getNightServicePharmacies();
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir les pharmacies à proximité (géolocalisation)
     * GET /api/pharmacy/pharmacies/nearby?lat=...&lon=...&radius=5
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<PharmacyDto>> getPharmaciesNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") double radius) {
        log.info("REST request to get pharmacies nearby - lat: {}, lon: {}, radius: {}km", lat, lon, radius);
        List<PharmacyDto> pharmacies = pharmacyService.getPharmaciesNearby(lat, lon, radius);
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Obtenir les pharmacies avec livraison à domicile dans une ville
     * GET /api/pharmacy/pharmacies/home-delivery/{city}
     */
    @GetMapping("/home-delivery/{city}")
    public ResponseEntity<List<PharmacyDto>> getHomeDeliveryPharmacies(@PathVariable String city) {
        log.info("REST request to get home delivery pharmacies in city: {}", city);
        List<PharmacyDto> pharmacies = pharmacyService.getHomeDeliveryPharmacies(city);
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Mettre à jour le statut d'une pharmacie (Admin)
     * PATCH /api/pharmacy/pharmacies/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PharmacyDto> updatePharmacyStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        log.info("REST request to update pharmacy status: {}", id);
        
        String statusStr = statusUpdate.get("status");
        Pharmacy.PharmacyStatus status = Pharmacy.PharmacyStatus.valueOf(statusStr);
        
        PharmacyDto pharmacy = pharmacyService.updatePharmacyStatus(id, status);
        return ResponseEntity.ok(pharmacy);
    }

    /**
     * Supprimer une pharmacie
     * DELETE /api/pharmacy/pharmacies/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePharmacy(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to delete pharmacy: {}", id);
        pharmacyService.deletePharmacy(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir le nombre de pharmacies actives
     * GET /api/pharmacy/pharmacies/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActivePharmacies() {
        log.info("REST request to count active pharmacies");
        long count = pharmacyService.countActivePharmacies();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
