package com.medilinktunisia.laboratoryservice.controller;

import com.medilinktunisia.laboratoryservice.model.dto.AnalysisRequestCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisRequestDto;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisRequestUpdateRequest;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisRequest;
import com.medilinktunisia.laboratoryservice.service.AnalysisRequestService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des demandes d'analyses
 */
@RestController
@RequestMapping("/analysis-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalysisRequestController {

    private final AnalysisRequestService requestService;

    /**
     * Créer une nouvelle demande d'analyse
     * POST /api/laboratory/analysis-requests
     */
    @PostMapping
    public ResponseEntity<AnalysisRequestDto> createRequest(
            @Valid @RequestBody AnalysisRequestCreateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to create analysis request");
        AnalysisRequestDto analysisRequest = requestService.createRequest(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(analysisRequest);
    }

    /**
     * Mettre à jour une demande d'analyse
     * PUT /api/laboratory/analysis-requests/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnalysisRequestDto> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody AnalysisRequestUpdateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to update analysis request: {}", id);
        AnalysisRequestDto analysisRequest = requestService.updateRequest(id, request, currentUserId);
        return ResponseEntity.ok(analysisRequest);
    }

    /**
     * Mettre à jour le statut d'une demande
     * PATCH /api/laboratory/analysis-requests/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AnalysisRequestDto> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to update analysis request status: {}", id);
        AnalysisRequest.RequestStatus status = AnalysisRequest.RequestStatus.valueOf(request.get("status"));
        AnalysisRequestDto analysisRequest = requestService.updateRequestStatus(id, status, currentUserId);
        return ResponseEntity.ok(analysisRequest);
    }

    /**
     * Marquer une demande comme payée
     * PATCH /api/laboratory/analysis-requests/{id}/mark-paid
     */
    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<AnalysisRequestDto> markAsPaid(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to mark analysis request as paid: {}", id);
        BigDecimal paidAmount = request.get("paidAmount");
        AnalysisRequestDto analysisRequest = requestService.markAsPaid(id, paidAmount, currentUserId);
        return ResponseEntity.ok(analysisRequest);
    }

    /**
     * Annuler une demande
     * PATCH /api/laboratory/analysis-requests/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AnalysisRequestDto> cancelRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to cancel analysis request: {}", id);
        AnalysisRequestDto analysisRequest = requestService.cancelRequest(id, currentUserId);
        return ResponseEntity.ok(analysisRequest);
    }

    /**
     * Notifier le patient
     * POST /api/laboratory/analysis-requests/{id}/notify-patient
     */
    @PostMapping("/{id}/notify-patient")
    public ResponseEntity<Void> notifyPatient(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to notify patient for request: {}", id);
        requestService.notifyPatient(id, currentUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Notifier le médecin
     * POST /api/laboratory/analysis-requests/{id}/notify-doctor
     */
    @PostMapping("/{id}/notify-doctor")
    public ResponseEntity<Void> notifyDoctor(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to notify doctor for request: {}", id);
        requestService.notifyDoctor(id, currentUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtenir une demande par ID
     * GET /api/laboratory/analysis-requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisRequestDto> getRequestById(@PathVariable Long id) {
        log.info("REST request to get analysis request: {}", id);
        AnalysisRequestDto analysisRequest = requestService.getRequestById(id);
        return ResponseEntity.ok(analysisRequest);
    }

    /**
     * Obtenir une demande par numéro
     * GET /api/laboratory/analysis-requests/number/{requestNumber}
     */
    @GetMapping("/number/{requestNumber}")
    public ResponseEntity<AnalysisRequestDto> getRequestByNumber(@PathVariable String requestNumber) {
        log.info("REST request to get analysis request by number: {}", requestNumber);
        AnalysisRequestDto analysisRequest = requestService.getRequestByNumber(requestNumber);
        return ResponseEntity.ok(analysisRequest);
    }

    /**
     * Obtenir toutes les demandes d'un laboratoire
     * GET /api/laboratory/analysis-requests/laboratory/{laboratoryId}
     */
    @GetMapping("/laboratory/{laboratoryId}")
    public ResponseEntity<Page<AnalysisRequestDto>> getLaboratoryRequests(
            @PathVariable Long laboratoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("REST request to get requests for laboratory: {}", laboratoryId);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AnalysisRequestDto> requests = requestService.getLaboratoryRequests(laboratoryId, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * Obtenir les demandes en attente d'un laboratoire
     * GET /api/laboratory/analysis-requests/laboratory/{laboratoryId}/pending
     */
    @GetMapping("/laboratory/{laboratoryId}/pending")
    public ResponseEntity<List<AnalysisRequestDto>> getPendingRequests(@PathVariable Long laboratoryId) {
        log.info("REST request to get pending requests for laboratory: {}", laboratoryId);
        List<AnalysisRequestDto> requests = requestService.getPendingRequests(laboratoryId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Obtenir les demandes par statut
     * GET /api/laboratory/analysis-requests/laboratory/{laboratoryId}/status/{status}
     */
    @GetMapping("/laboratory/{laboratoryId}/status/{status}")
    public ResponseEntity<List<AnalysisRequestDto>> getRequestsByStatus(
            @PathVariable Long laboratoryId,
            @PathVariable AnalysisRequest.RequestStatus status) {
        log.info("REST request to get requests by status for laboratory: {}", laboratoryId);
        List<AnalysisRequestDto> requests = requestService.getRequestsByStatus(laboratoryId, status);
        return ResponseEntity.ok(requests);
    }

    /**
     * Obtenir l'historique des analyses d'un patient
     * GET /api/laboratory/analysis-requests/patient/{patientId}/history
     */
    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<Page<AnalysisRequestDto>> getPatientHistory(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get patient history: {}", patientId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalysisRequestDto> requests = requestService.getPatientHistory(patientId, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * Calculer le montant total d'une demande
     * GET /api/laboratory/analysis-requests/{id}/total-amount
     */
    @GetMapping("/{id}/total-amount")
    public ResponseEntity<Map<String, BigDecimal>> calculateTotalAmount(@PathVariable Long id) {
        log.info("REST request to calculate total amount for request: {}", id);
        BigDecimal totalAmount = requestService.calculateTotalAmount(id);
        return ResponseEntity.ok(Map.of("totalAmount", totalAmount));
    }

    /**
     * Supprimer une demande
     * DELETE /api/laboratory/analysis-requests/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("REST request to delete analysis request: {}", id);
        requestService.deleteRequest(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
