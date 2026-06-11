package com.medilinktunisia.laboratoryservice.controller;

import com.medilinktunisia.laboratoryservice.model.dto.LaboratoryCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.LaboratoryDto;
import com.medilinktunisia.laboratoryservice.model.dto.LaboratoryUpdateRequest;
import com.medilinktunisia.laboratoryservice.model.entity.Laboratory;
import com.medilinktunisia.laboratoryservice.service.LaboratoryService;
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
 * Contrôleur REST pour la gestion des laboratoires
 */
@RestController
@RequestMapping("/laboratories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    /**
     * Créer un nouveau laboratoire
     * POST /api/laboratory/laboratories
     */
    @PostMapping
    public ResponseEntity<LaboratoryDto> createLaboratory(
            @Valid @RequestBody LaboratoryCreateRequest request) {
        log.info("REST request to create laboratory: {}", request.getName());
        LaboratoryDto laboratory = laboratoryService.createLaboratory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratory);
    }

    /**
     * Mettre à jour un laboratoire
     * PUT /api/laboratory/laboratories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<LaboratoryDto> updateLaboratory(
            @PathVariable Long id,
            @Valid @RequestBody LaboratoryUpdateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to update laboratory: {}", id);
        LaboratoryDto laboratory = laboratoryService.updateLaboratory(id, request, currentUserId);
        return ResponseEntity.ok(laboratory);
    }

    /**
     * Obtenir un laboratoire par ID
     * GET /api/laboratory/laboratories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LaboratoryDto> getLaboratoryById(@PathVariable Long id) {
        log.info("REST request to get laboratory: {}", id);
        LaboratoryDto laboratory = laboratoryService.getLaboratoryById(id);
        return ResponseEntity.ok(laboratory);
    }

    /**
     * Obtenir le laboratoire d'un utilisateur
     * GET /api/laboratory/laboratories/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<LaboratoryDto> getLaboratoryByUserId(@PathVariable Long userId) {
        log.info("REST request to get laboratory for user: {}", userId);
        LaboratoryDto laboratory = laboratoryService.getLaboratoryByUserId(userId);
        return ResponseEntity.ok(laboratory);
    }

    /**
     * Obtenir le laboratoire de l'utilisateur connecté
     * GET /api/laboratory/laboratories/my-laboratory
     */
    @GetMapping("/my-laboratory")
    public ResponseEntity<LaboratoryDto> getMyLaboratory(
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to get my laboratory for user: {}", currentUserId);
        LaboratoryDto laboratory = laboratoryService.getLaboratoryByUserId(currentUserId);
        return ResponseEntity.ok(laboratory);
    }

    /**
     * Obtenir tous les laboratoires (avec pagination)
     * GET /api/laboratory/laboratories?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<Page<LaboratoryDto>> getAllLaboratories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("REST request to get all laboratories - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<LaboratoryDto> laboratories = laboratoryService.getAllLaboratories(pageable);
        return ResponseEntity.ok(laboratories);
    }

    /**
     * Rechercher des laboratoires par nom
     * GET /api/laboratory/laboratories/search?name=...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LaboratoryDto>> searchLaboratories(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to search laboratories by name: {}", name);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LaboratoryDto> laboratories = laboratoryService.searchLaboratoriesByName(name, pageable);
        return ResponseEntity.ok(laboratories);
    }

    /**
     * Obtenir les laboratoires par ville
     * GET /api/laboratory/laboratories/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<LaboratoryDto>> getLaboratoriesByCity(@PathVariable String city) {
        log.info("REST request to get laboratories in city: {}", city);
        List<LaboratoryDto> laboratories = laboratoryService.getLaboratoriesByCity(city);
        return ResponseEntity.ok(laboratories);
    }

    /**
     * Obtenir les laboratoires actifs par ville
     * GET /api/laboratory/laboratories/city/{city}/active
     */
    @GetMapping("/city/{city}/active")
    public ResponseEntity<List<LaboratoryDto>> getActiveLaboratoriesByCity(@PathVariable String city) {
        log.info("REST request to get active laboratories in city: {}", city);
        List<LaboratoryDto> laboratories = laboratoryService.getActiveLaboratoriesByCity(city);
        return ResponseEntity.ok(laboratories);
    }

    /**
     * Obtenir les laboratoires à proximité (géolocalisation)
     * GET /api/laboratory/laboratories/nearby?lat=...&lon=...&radius=5
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<LaboratoryDto>> getLaboratoriesNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") double radius) {
        log.info("REST request to get laboratories nearby - lat: {}, lon: {}, radius: {}km", lat, lon, radius);
        List<LaboratoryDto> laboratories = laboratoryService.getLaboratoriesNearby(lat, lon, radius);
        return ResponseEntity.ok(laboratories);
    }

    /**
     * Obtenir les laboratoires avec prélèvement à domicile
     * GET /api/laboratory/laboratories/home-collection/{city}
     */
    @GetMapping("/home-collection/{city}")
    public ResponseEntity<List<LaboratoryDto>> getHomeCollectionLaboratories(@PathVariable String city) {
        log.info("REST request to get home collection laboratories in city: {}", city);
        List<LaboratoryDto> laboratories = laboratoryService.getHomeCollectionLaboratories(city);
        return ResponseEntity.ok(laboratories);
    }

    /**
     * Obtenir les laboratoires avec analyses urgentes
     * GET /api/laboratory/laboratories/urgent
     */
    @GetMapping("/urgent")
    public ResponseEntity<List<LaboratoryDto>> getUrgentAnalysisLaboratories() {
        log.info("REST request to get urgent analysis laboratories");
        List<LaboratoryDto> laboratories = laboratoryService.getUrgentAnalysisLaboratories();
        return ResponseEntity.ok(laboratories);
    }

    /**
     * Mettre à jour le statut d'un laboratoire (Admin)
     * PATCH /api/laboratory/laboratories/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<LaboratoryDto> updateLaboratoryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        log.info("REST request to update laboratory status: {}", id);
        
        String statusStr = statusUpdate.get("status");
        Laboratory.LaboratoryStatus status = Laboratory.LaboratoryStatus.valueOf(statusStr);
        
        LaboratoryDto laboratory = laboratoryService.updateLaboratoryStatus(id, status);
        return ResponseEntity.ok(laboratory);
    }

    /**
     * Supprimer un laboratoire
     * DELETE /api/laboratory/laboratories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLaboratory(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to delete laboratory: {}", id);
        laboratoryService.deleteLaboratory(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir le nombre de laboratoires actifs
     * GET /api/laboratory/laboratories/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActiveLaboratories() {
        log.info("REST request to count active laboratories");
        long count = laboratoryService.countActiveLaboratories();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
