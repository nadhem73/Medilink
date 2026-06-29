package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.dto.response.PharmacyListDto;
import com.medilinktunisia.authservice.model.entity.Pharmacy;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import com.medilinktunisia.authservice.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logique métier propre à la pharmacie.
 * Service dédié (séparé de l'AuthService) pour isoler le code «Gestion Pharmacie».
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;

    /**
     * Liste toutes les pharmacies actives (ex. recherche de pharmacie pour une ordonnance).
     */
    public List<PharmacyListDto> getAllActivePharmacies() {
        return pharmacyRepository.findAll().stream()
                .filter(p -> p.getStatus() == UserStatus.ACTIVE)
                .map(this::toPharmacyListDto)
                .toList();
    }

    private PharmacyListDto toPharmacyListDto(Pharmacy pharmacy) {
        return PharmacyListDto.builder()
                .id(pharmacy.getId())
                .pharmacyName(pharmacy.getPharmacyName())
                .email(pharmacy.getEmail())
                .phone(pharmacy.getPhone())
                .address(pharmacy.getAddress())
                .licenseNumber(pharmacy.getLicenseNumber())
                .openingHours(pharmacy.getOpeningHours())
                .latitude(pharmacy.getLatitude())
                .longitude(pharmacy.getLongitude())
                .build();
    }
}
