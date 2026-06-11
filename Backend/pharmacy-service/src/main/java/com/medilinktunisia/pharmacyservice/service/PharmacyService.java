package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.exception.PharmacyNotFoundException;
import com.medilinktunisia.pharmacyservice.exception.UnauthorizedAccessException;
import com.medilinktunisia.pharmacyservice.model.dto.PharmacyCreateRequest;
import com.medilinktunisia.pharmacyservice.model.dto.PharmacyDto;
import com.medilinktunisia.pharmacyservice.model.dto.PharmacyUpdateRequest;
import com.medilinktunisia.pharmacyservice.model.entity.Pharmacy;
import com.medilinktunisia.pharmacyservice.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des pharmacies
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;

    /**
     * Créer une nouvelle pharmacie
     */
    public PharmacyDto createPharmacy(PharmacyCreateRequest request) {
        log.info("Creating new pharmacy: {}", request.getName());

        // Vérifier si la licence existe déjà
        if (pharmacyRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("Une pharmacie avec ce numéro de licence existe déjà");
        }

        // Vérifier si l'utilisateur a déjà une pharmacie
        if (pharmacyRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("Cet utilisateur a déjà une pharmacie enregistrée");
        }

        Pharmacy pharmacy = Pharmacy.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .licenseNumber(request.getLicenseNumber())
                .ownerName(request.getOwnerName())
                .address(request.getAddress())
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .region(request.getRegion())
                .phone(request.getPhone())
                .alternativePhone(request.getAlternativePhone())
                .email(request.getEmail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .openingHours(request.getOpeningHours())
                .homeDelivery(request.getHomeDelivery() != null ? request.getHomeDelivery() : false)
                .electronicPrescriptionEnabled(request.getElectronicPrescriptionEnabled() != null ? 
                        request.getElectronicPrescriptionEnabled() : true)
                .nightService(request.getNightService() != null ? request.getNightService() : false)
                .status(Pharmacy.PharmacyStatus.PENDING_APPROVAL)
                .description(request.getDescription())
                .website(request.getWebsite())
                .build();

        pharmacy = pharmacyRepository.save(pharmacy);
        log.info("Pharmacy created successfully with ID: {}", pharmacy.getId());

        return mapToDto(pharmacy);
    }

    /**
     * Mettre à jour une pharmacie
     */
    public PharmacyDto updatePharmacy(Long pharmacyId, PharmacyUpdateRequest request, Long currentUserId) {
        log.info("Updating pharmacy ID: {}", pharmacyId);

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException("Pharmacie non trouvée"));

        // Vérifier l'autorisation
        if (!pharmacy.getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier cette pharmacie");
        }

        // Mettre à jour les champs
        if (request.getName() != null) pharmacy.setName(request.getName());
        if (request.getOwnerName() != null) pharmacy.setOwnerName(request.getOwnerName());
        if (request.getAddress() != null) pharmacy.setAddress(request.getAddress());
        if (request.getCity() != null) pharmacy.setCity(request.getCity());
        if (request.getZipCode() != null) pharmacy.setZipCode(request.getZipCode());
        if (request.getRegion() != null) pharmacy.setRegion(request.getRegion());
        if (request.getPhone() != null) pharmacy.setPhone(request.getPhone());
        if (request.getAlternativePhone() != null) pharmacy.setAlternativePhone(request.getAlternativePhone());
        if (request.getEmail() != null) pharmacy.setEmail(request.getEmail());
        if (request.getLatitude() != null) pharmacy.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) pharmacy.setLongitude(request.getLongitude());
        if (request.getOpeningHours() != null) pharmacy.setOpeningHours(request.getOpeningHours());
        if (request.getHomeDelivery() != null) pharmacy.setHomeDelivery(request.getHomeDelivery());
        if (request.getElectronicPrescriptionEnabled() != null) 
            pharmacy.setElectronicPrescriptionEnabled(request.getElectronicPrescriptionEnabled());
        if (request.getNightService() != null) pharmacy.setNightService(request.getNightService());
        if (request.getDescription() != null) pharmacy.setDescription(request.getDescription());
        if (request.getWebsite() != null) pharmacy.setWebsite(request.getWebsite());

        pharmacy = pharmacyRepository.save(pharmacy);
        log.info("Pharmacy updated successfully");

        return mapToDto(pharmacy);
    }

    /**
     * Obtenir une pharmacie par ID
     */
    @Transactional(readOnly = true)
    public PharmacyDto getPharmacyById(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new PharmacyNotFoundException("Pharmacie non trouvée"));
        return mapToDto(pharmacy);
    }

    /**
     * Obtenir la pharmacie d'un utilisateur
     */
    @Transactional(readOnly = true)
    public PharmacyDto getPharmacyByUserId(Long userId) {
        Pharmacy pharmacy = pharmacyRepository.findByUserId(userId)
                .orElseThrow(() -> new PharmacyNotFoundException("Aucune pharmacie trouvée pour cet utilisateur"));
        return mapToDto(pharmacy);
    }

    /**
     * Obtenir toutes les pharmacies (avec pagination)
     */
    @Transactional(readOnly = true)
    public Page<PharmacyDto> getAllPharmacies(Pageable pageable) {
        return pharmacyRepository.findAll(pageable).map(this::mapToDto);
    }

    /**
     * Rechercher des pharmacies par nom
     */
    @Transactional(readOnly = true)
    public Page<PharmacyDto> searchPharmaciesByName(String name, Pageable pageable) {
        return pharmacyRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::mapToDto);
    }

    /**
     * Obtenir les pharmacies par ville
     */
    @Transactional(readOnly = true)
    public List<PharmacyDto> getPharmaciesByCity(String city) {
        return pharmacyRepository.findByCity(city).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les pharmacies actives par ville
     */
    @Transactional(readOnly = true)
    public List<PharmacyDto> getActivePharmaciesByCity(String city) {
        return pharmacyRepository.findByCityAndStatus(city, Pharmacy.PharmacyStatus.ACTIVE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les pharmacies de garde (service de nuit)
     */
    @Transactional(readOnly = true)
    public List<PharmacyDto> getNightServicePharmacies() {
        return pharmacyRepository.findNightServicePharmacies().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les pharmacies à proximité (géolocalisation)
     */
    @Transactional(readOnly = true)
    public List<PharmacyDto> getPharmaciesNearby(double latitude, double longitude, double radiusKm) {
        return pharmacyRepository.findPharmaciesWithinRadius(latitude, longitude, radiusKm).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les pharmacies avec livraison à domicile dans une ville
     */
    @Transactional(readOnly = true)
    public List<PharmacyDto> getHomeDeliveryPharmacies(String city) {
        return pharmacyRepository.findHomeDeliveryPharmaciesInCity(city).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Activer/Désactiver une pharmacie (Admin)
     */
    public PharmacyDto updatePharmacyStatus(Long pharmacyId, Pharmacy.PharmacyStatus status) {
        log.info("Updating pharmacy status to: {}", status);

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException("Pharmacie non trouvée"));

        pharmacy.setStatus(status);
        pharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy status updated successfully");
        return mapToDto(pharmacy);
    }

    /**
     * Supprimer une pharmacie
     */
    public void deletePharmacy(Long pharmacyId, Long currentUserId) {
        log.info("Deleting pharmacy ID: {}", pharmacyId);

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException("Pharmacie non trouvée"));

        // Vérifier l'autorisation
        if (!pharmacy.getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à supprimer cette pharmacie");
        }

        pharmacyRepository.delete(pharmacy);
        log.info("Pharmacy deleted successfully");
    }

    /**
     * Obtenir le nombre de pharmacies actives
     */
    @Transactional(readOnly = true)
    public long countActivePharmacies() {
        return pharmacyRepository.countActivePharmacies();
    }

    /**
     * Mapper Pharmacy vers PharmacyDto
     */
    private PharmacyDto mapToDto(Pharmacy pharmacy) {
        return PharmacyDto.builder()
                .id(pharmacy.getId())
                .userId(pharmacy.getUserId())
                .name(pharmacy.getName())
                .licenseNumber(pharmacy.getLicenseNumber())
                .ownerName(pharmacy.getOwnerName())
                .address(pharmacy.getAddress())
                .city(pharmacy.getCity())
                .zipCode(pharmacy.getZipCode())
                .region(pharmacy.getRegion())
                .phone(pharmacy.getPhone())
                .alternativePhone(pharmacy.getAlternativePhone())
                .email(pharmacy.getEmail())
                .latitude(pharmacy.getLatitude())
                .longitude(pharmacy.getLongitude())
                .openingHours(pharmacy.getOpeningHours())
                .homeDelivery(pharmacy.getHomeDelivery())
                .electronicPrescriptionEnabled(pharmacy.getElectronicPrescriptionEnabled())
                .nightService(pharmacy.getNightService())
                .status(pharmacy.getStatus())
                .description(pharmacy.getDescription())
                .website(pharmacy.getWebsite())
                .totalOrders(pharmacy.getTotalOrders())
                .totalMedicationsSold(pharmacy.getTotalMedicationsSold())
                .rating(pharmacy.getRating())
                .reviewsCount(pharmacy.getReviewsCount())
                .createdAt(pharmacy.getCreatedAt())
                .updatedAt(pharmacy.getUpdatedAt())
                .build();
    }
}
