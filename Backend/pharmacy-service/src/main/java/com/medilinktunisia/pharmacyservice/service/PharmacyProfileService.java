package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.dto.PharmacyProfileDto;
import com.medilinktunisia.pharmacyservice.dto.PharmacyProfileRequest;
import com.medilinktunisia.pharmacyservice.model.PharmacyProfile;
import com.medilinktunisia.pharmacyservice.repository.PharmacyProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PharmacyProfileService {

    private final PharmacyProfileRepository repository;

    /**
     * Crée le profil opérationnel d'une pharmacie (appelé par l'auth-service à la
     * création du compte). Idempotent : ne recrée pas si un profil existe déjà.
     */
    public void createPharmacyProfile(PharmacyProfileRequest request) {
        if (request.getUserId() == null || repository.existsByUserId(request.getUserId())) {
            log.warn("createPharmacyProfile skipped - userId null or already exists: {}", request.getUserId());
            return;
        }
        log.info("Creating pharmacy profile for userId: {}", request.getUserId());
        PharmacyProfile profile = new PharmacyProfile();
        profile.setUserId(request.getUserId());
        profile.setOpen(request.getOpen() != null ? request.getOpen() : Boolean.TRUE);
        profile.setNightDuty(request.getNightDuty() != null ? request.getNightDuty() : Boolean.FALSE);
        profile.setStockAlertThreshold(request.getStockAlertThreshold() != null ? request.getStockAlertThreshold() : 10);
        profile.setDebutMatin(request.getDebutMatin() != null ? request.getDebutMatin() : "08:00");
        profile.setFinMatin(request.getFinMatin() != null ? request.getFinMatin() : "13:00");
        profile.setDebutApresMidi(request.getDebutApresMidi() != null ? request.getDebutApresMidi() : "15:00");
        profile.setFinApresMidi(request.getFinApresMidi() != null ? request.getFinApresMidi() : "19:00");
        repository.save(profile);
    }

    /** Profil de la pharmacie connectée ; vide si aucun n'existe encore. */
    public PharmacyProfileDto getByUserId(Long userId) {
        log.debug("Fetching pharmacy profile for userId: {}", userId);
        return repository.findByUserId(userId)
                .map(this::toDto)
                .orElseGet(() -> PharmacyProfileDto.builder().userId(userId).open(Boolean.TRUE).build());
    }

    /** Tous les profils pharmacies. */
    public List<PharmacyProfileDto> getAllProfiles() {
        log.debug("Fetching all pharmacy profiles");
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Met à jour le profil de la pharmacie connectée (panel pharmacie).
     * Crée le profil s'il n'existait pas encore.
     */
    public PharmacyProfileDto updateByUserId(Long userId, PharmacyProfileRequest request) {
        log.info("Updating pharmacy profile for userId: {}", userId);
        PharmacyProfile profile = repository.findByUserId(userId)
                .orElseGet(() -> {
                    PharmacyProfile p = new PharmacyProfile();
                    p.setUserId(userId);
                    return p;
                });
        if (request.getOpen() != null) {
            profile.setOpen(request.getOpen());
        }
        if (request.getNightDuty() != null) {
            profile.setNightDuty(request.getNightDuty());
        }
        if (request.getStockAlertThreshold() != null) {
            profile.setStockAlertThreshold(request.getStockAlertThreshold());
        }
        if (request.getDebutMatin() != null) profile.setDebutMatin(request.getDebutMatin());
        if (request.getFinMatin() != null) profile.setFinMatin(request.getFinMatin());
        if (request.getDebutApresMidi() != null) profile.setDebutApresMidi(request.getDebutApresMidi());
        if (request.getFinApresMidi() != null) profile.setFinApresMidi(request.getFinApresMidi());
        return toDto(repository.save(profile));
    }

    private PharmacyProfileDto toDto(PharmacyProfile p) {
        return PharmacyProfileDto.builder()
                .userId(p.getUserId())
                .open(p.getOpen())
                .nightDuty(p.getNightDuty())
                .stockAlertThreshold(p.getStockAlertThreshold())
                .debutMatin(p.getDebutMatin())
                .finMatin(p.getFinMatin())
                .debutApresMidi(p.getDebutApresMidi())
                .finApresMidi(p.getFinApresMidi())
                .build();
    }
}
