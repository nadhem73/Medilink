package com.medilinktunisia.laboratoryservice.controller;

import com.medilinktunisia.laboratoryservice.model.dto.AnalysisResultCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisResultDto;
import com.medilinktunisia.laboratoryservice.service.AnalysisResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des résultats d'analyses
 */
@RestController
@RequestMapping("/analysis-results")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalysisResultController {

    private final AnalysisResultService resultService;

    /**
     * Créer un nouveau résultat d'analyse
     * POST /api/laboratory/analysis-results
     */
    @PostMapping
    public ResponseEntity<AnalysisResultDto> createResult(
            @Valid @RequestBody AnalysisResultCreateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to create analysis result");
        AnalysisResultDto result = resultService.createResult(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Upload d'un fichier PDF de résultat
     * POST /api/laboratory/analysis-results/{id}/upload-pdf
     */
    @PostMapping(value = "/{id}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResultDto> uploadPdfResult(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to upload PDF result for result ID: {}", id);
        AnalysisResultDto result = resultService.uploadPdfResult(id, file, currentUserId);
        return ResponseEntity.ok(result);
    }

    /**
     * Valider un résultat d'analyse
     * PATCH /api/laboratory/analysis-results/{id}/validate
     */
    @PatchMapping("/{id}/validate")
    public ResponseEntity<AnalysisResultDto> validateResult(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to validate result ID: {}", id);
        String validatedBy = request.get("validatedBy");
        AnalysisResultDto result = resultService.validateResult(id, validatedBy, currentUserId);
        return ResponseEntity.ok(result);
    }

    /**
     * Mettre à jour un résultat
     * PUT /api/laboratory/analysis-results/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnalysisResultDto> updateResult(
            @PathVariable Long id,
            @Valid @RequestBody AnalysisResultCreateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to update result ID: {}", id);
        AnalysisResultDto result = resultService.updateResult(id, request, currentUserId);
        return ResponseEntity.ok(result);
    }

    /**
     * Obtenir un résultat par ID
     * GET /api/laboratory/analysis-results/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResultDto> getResultById(@PathVariable Long id) {
        log.info("REST request to get result: {}", id);
        AnalysisResultDto result = resultService.getResultById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Obtenir tous les résultats d'une demande
     * GET /api/laboratory/analysis-results/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<AnalysisResultDto>> getResultsByRequest(@PathVariable Long requestId) {
        log.info("REST request to get results for request: {}", requestId);
        List<AnalysisResultDto> results = resultService.getResultsByRequest(requestId);
        return ResponseEntity.ok(results);
    }

    /**
     * Obtenir les résultats validés d'une demande
     * GET /api/laboratory/analysis-results/request/{requestId}/validated
     */
    @GetMapping("/request/{requestId}/validated")
    public ResponseEntity<List<AnalysisResultDto>> getValidatedResultsByRequest(@PathVariable Long requestId) {
        log.info("REST request to get validated results for request: {}", requestId);
        List<AnalysisResultDto> results = resultService.getValidatedResultsByRequest(requestId);
        return ResponseEntity.ok(results);
    }

    /**
     * Obtenir les résultats non validés d'une demande
     * GET /api/laboratory/analysis-results/request/{requestId}/unvalidated
     */
    @GetMapping("/request/{requestId}/unvalidated")
    public ResponseEntity<List<AnalysisResultDto>> getUnvalidatedResultsByRequest(@PathVariable Long requestId) {
        log.info("REST request to get unvalidated results for request: {}", requestId);
        List<AnalysisResultDto> results = resultService.getUnvalidatedResultsByRequest(requestId);
        return ResponseEntity.ok(results);
    }

    /**
     * Supprimer un résultat
     * DELETE /api/laboratory/analysis-results/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to delete result: {}", id);
        resultService.deleteResult(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
