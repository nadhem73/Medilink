package com.medilinktunisia.pharmacyservice.controller;

import com.medilinktunisia.pharmacyservice.dto.PharmacyProfileDto;
import com.medilinktunisia.pharmacyservice.dto.PharmacyProfileRequest;
import com.medilinktunisia.pharmacyservice.service.PharmacyProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints du profil opérationnel pharmacie.
 * Le context-path {@code /api/pharmacy} est ajouté en préfixe (voir application.yml).
 */
@RestController
@RequestMapping("/pharmacy-profiles")
@RequiredArgsConstructor
@Slf4j
public class PharmacyProfileController {

    private final PharmacyProfileService service;

    /**
     * Endpoint INTERNE (service-à-service) : création du profil pharmacie
     * lors de la création du compte via l'auth-service.
     */
    @PostMapping("/internal")
    public ResponseEntity<Void> createPharmacyProfile(@RequestBody PharmacyProfileRequest request) {
        log.info("POST /pharmacy-profiles/internal - Creating pharmacy profile for userId: {}", request.getUserId());
        service.createPharmacyProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Profil de la pharmacie connectée (affiché dans le panel pharmacie).
     * L'identifiant utilisateur est extrait du JWT par le filtre de sécurité.
     */
    @GetMapping("/me")
    public ResponseEntity<PharmacyProfileDto> getMyPharmacyProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("GET /pharmacy-profiles/me - Fetching profile for userId: {}", userId);
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    /**
     * Liste tous les profils pharmacies (statut, garde, horaires).
     */
    @GetMapping("/all")
    public ResponseEntity<List<PharmacyProfileDto>> getAllPharmacyProfiles() {
        log.info("GET /pharmacy-profiles/all - Fetching all pharmacy profiles");
        return ResponseEntity.ok(service.getAllProfiles());
    }

    /**
     * Mise à jour du profil de la pharmacie connectée depuis le panel pharmacie.
     */
    @PutMapping("/me")
    public ResponseEntity<PharmacyProfileDto> updateMyPharmacyProfile(
            HttpServletRequest request,
            @RequestBody PharmacyProfileRequest body) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("PUT /pharmacy-profiles/me - Updating profile for userId: {}", userId);
        return ResponseEntity.ok(service.updateByUserId(userId, body));
    }
}
