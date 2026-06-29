package com.medilinktunisia.authservice.controller;

import com.medilinktunisia.authservice.dto.response.PharmacyListDto;
import com.medilinktunisia.authservice.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST dédiés à la pharmacie.
 * Contrôleur séparé de l'AuthController pour isoler le code «Gestion Pharmacie».
 */
@Slf4j
@RestController
@RequestMapping("/api/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;

    /**
     * Liste toutes les pharmacies actives.
     */
    @GetMapping
    public ResponseEntity<List<PharmacyListDto>> getAllPharmacies() {
        log.info("Request to list all active pharmacies");
        return ResponseEntity.ok(pharmacyService.getAllActivePharmacies());
    }
}
