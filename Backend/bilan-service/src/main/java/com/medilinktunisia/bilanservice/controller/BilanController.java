package com.medilinktunisia.bilanservice.controller;

import com.medilinktunisia.bilanservice.dto.*;
import com.medilinktunisia.bilanservice.service.BilanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bilans")
@RequiredArgsConstructor
public class BilanController {

    private final BilanService bilanService;

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> scanBilan(
            @RequestParam("image") MultipartFile image,
            Authentication authentication) {
        try {
            Long patientId = getPatientId(authentication);
            ScanBilanResponse response = bilanService.scanBilan(image, patientId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Scan bilan validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Scan bilan failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors du scan : " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<BilanSummary>> getBilans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long patientId = getPatientId(authentication);
        Page<BilanSummary> bilans = bilanService.getPatientBilans(patientId, page, size);
        return ResponseEntity.ok(bilans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBilan(@PathVariable UUID id, Authentication authentication) {
        try {
            Long patientId = getPatientId(authentication);
            ScanBilanResponse response = bilanService.getBilan(id, patientId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Bilan introuvable"));
        } catch (AccessDeniedException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Accès refusé"));
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBilan(@PathVariable UUID id, Authentication authentication) {
        try {
            Long patientId = getPatientId(authentication);
            ScanBilanResponse response = bilanService.confirmBilan(id, patientId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Bilan introuvable"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Accès refusé"));
        }
    }

    @PutMapping("/{id}/assign-doctor")
    public ResponseEntity<?> assignDoctor(
            @PathVariable UUID id,
            @RequestBody AssignDoctorRequest request,
            Authentication authentication) {
        try {
            Long patientId = getPatientId(authentication);
            ScanBilanResponse response = bilanService.assignDoctor(id, patientId, request.getDoctorId());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Bilan introuvable"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Accès refusé"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{bilanId}/results/{resultId}")
    public ResponseEntity<?> updateResult(
            @PathVariable UUID bilanId,
            @PathVariable Long resultId,
            @RequestBody UpdateResultRequest request,
            Authentication authentication) {
        try {
            Long patientId = getPatientId(authentication);
            ScanBilanResponse response = bilanService.updateResult(bilanId, resultId, request, patientId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Résultat introuvable"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Accès refusé"));
        }
    }

    @GetMapping("/doctor")
    public ResponseEntity<Page<BilanSummary>> getDoctorBilans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long doctorId = getUserId(authentication);
        Page<BilanSummary> bilans = bilanService.getDoctorBilans(doctorId, page, size);
        return ResponseEntity.ok(bilans);
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<?> getBilanForDoctor(@PathVariable UUID id, Authentication authentication) {
        try {
            Long doctorId = getUserId(authentication);
            ScanBilanResponse response = bilanService.getBilanForDoctor(id, doctorId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Bilan introuvable"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Accès refusé"));
        }
    }

    @PatchMapping("/doctor/{id}/review")
    public ResponseEntity<?> updateReviewStatus(
            @PathVariable UUID id,
            @RequestBody UpdateReviewStatusRequest request,
            Authentication authentication) {
        try {
            Long doctorId = getUserId(authentication);
            ScanBilanResponse response = bilanService.updateReviewStatus(id, doctorId, request.getReviewStatus());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Bilan introuvable"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Accès refusé"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBilan(@PathVariable UUID id, Authentication authentication) {
        try {
            Long patientId = getPatientId(authentication);
            bilanService.deleteBilan(id, patientId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Bilan introuvable"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Accès refusé"));
        }
    }

    private Long getPatientId(Authentication authentication) {
        return getUserId(authentication);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Non authentifié");
        }
        return (Long) authentication.getPrincipal();
    }
}
