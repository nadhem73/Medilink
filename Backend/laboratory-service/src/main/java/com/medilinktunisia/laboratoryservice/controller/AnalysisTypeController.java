package com.medilinktunisia.laboratoryservice.controller;

import com.medilinktunisia.laboratoryservice.model.dto.AnalysisTypeCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisTypeDto;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisType;
import com.medilinktunisia.laboratoryservice.service.AnalysisTypeService;
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
 * Contrôleur REST pour la gestion des types d'analyses
 */
@RestController
@RequestMapping("/analysis-types")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalysisTypeController {

    private final AnalysisTypeService analysisTypeService;

    /**
     * Créer un nouveau type d'analyse
     * POST /api/laboratory/analysis-types
     */
    @PostMapping
    public ResponseEntity<AnalysisTypeDto> createAnalysisType(
            @Valid @RequestBody AnalysisTypeCreateRequest request) {
        log.info("REST request to create analysis type");
        AnalysisTypeDto analysisType = analysisTypeService.createAnalysisType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(analysisType);
    }

    /**
     * Mettre à jour un type d'analyse
     * PUT /api/laboratory/analysis-types/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnalysisTypeDto> updateAnalysisType(
            @PathVariable Long id,
            @Valid @RequestBody AnalysisTypeCreateRequest request) {
        log.info("REST request to update analysis type: {}", id);
        AnalysisTypeDto analysisType = analysisTypeService.updateAnalysisType(id, request);
        return ResponseEntity.ok(analysisType);
    }

    /**
     * Obtenir un type d'analyse par ID
     * GET /api/laboratory/analysis-types/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisTypeDto> getAnalysisTypeById(@PathVariable Long id) {
        log.info("REST request to get analysis type: {}", id);
        AnalysisTypeDto analysisType = analysisTypeService.getAnalysisTypeById(id);
        return ResponseEntity.ok(analysisType);
    }

    /**
     * Obtenir un type d'analyse par code
     * GET /api/laboratory/analysis-types/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<AnalysisTypeDto> getAnalysisTypeByCode(@PathVariable String code) {
        log.info("REST request to get analysis type by code: {}", code);
        AnalysisTypeDto analysisType = analysisTypeService.getAnalysisTypeByCode(code);
        return ResponseEntity.ok(analysisType);
    }

    /**
     * Obtenir tous les types d'analyses actifs
     * GET /api/laboratory/analysis-types/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<AnalysisTypeDto>> getAllActiveAnalysisTypes() {
        log.info("REST request to get all active analysis types");
        List<AnalysisTypeDto> analysisTypes = analysisTypeService.getAllActiveAnalysisTypes();
        return ResponseEntity.ok(analysisTypes);
    }

    /**
     * Obtenir tous les types d'analyses (avec pagination)
     * GET /api/laboratory/analysis-types
     */
    @GetMapping
    public ResponseEntity<Page<AnalysisTypeDto>> getAllAnalysisTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("REST request to get all analysis types");
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AnalysisTypeDto> analysisTypes = analysisTypeService.getAllAnalysisTypes(pageable);
        return ResponseEntity.ok(analysisTypes);
    }

    /**
     * Obtenir les types d'analyses par catégorie
     * GET /api/laboratory/analysis-types/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<AnalysisTypeDto>> getAnalysisTypesByCategory(
            @PathVariable AnalysisType.AnalysisCategory category) {
        log.info("REST request to get analysis types by category: {}", category);
        List<AnalysisTypeDto> analysisTypes = analysisTypeService.getAnalysisTypesByCategory(category);
        return ResponseEntity.ok(analysisTypes);
    }

    /**
     * Obtenir les types d'analyses actifs par catégorie
     * GET /api/laboratory/analysis-types/category/{category}/active
     */
    @GetMapping("/category/{category}/active")
    public ResponseEntity<List<AnalysisTypeDto>> getActiveAnalysisTypesByCategory(
            @PathVariable AnalysisType.AnalysisCategory category) {
        log.info("REST request to get active analysis types by category: {}", category);
        List<AnalysisTypeDto> analysisTypes = analysisTypeService.getActiveAnalysisTypesByCategory(category);
        return ResponseEntity.ok(analysisTypes);
    }

    /**
     * Rechercher des types d'analyses
     * GET /api/laboratory/analysis-types/search?term=...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AnalysisTypeDto>> searchAnalysisTypes(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to search analysis types: {}", term);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalysisTypeDto> analysisTypes = analysisTypeService.searchAnalysisTypes(term, pageable);
        return ResponseEntity.ok(analysisTypes);
    }

    /**
     * Activer/Désactiver un type d'analyse
     * PATCH /api/laboratory/analysis-types/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<AnalysisTypeDto> toggleAnalysisTypeStatus(@PathVariable Long id) {
        log.info("REST request to toggle analysis type status: {}", id);
        AnalysisTypeDto analysisType = analysisTypeService.toggleAnalysisTypeStatus(id);
        return ResponseEntity.ok(analysisType);
    }

    /**
     * Compter les types d'analyses actifs
     * GET /api/laboratory/analysis-types/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countActiveAnalysisTypes() {
        log.info("REST request to count active analysis types");
        long count = analysisTypeService.countActiveAnalysisTypes();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Supprimer (désactiver) un type d'analyse
     * DELETE /api/laboratory/analysis-types/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysisType(@PathVariable Long id) {
        log.info("REST request to delete analysis type: {}", id);
        analysisTypeService.deleteAnalysisType(id);
        return ResponseEntity.noContent().build();
    }
}
