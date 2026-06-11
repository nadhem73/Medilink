package com.medilinktunisia.laboratoryservice.service;

import com.medilinktunisia.laboratoryservice.exception.LaboratoryNotFoundException;
import com.medilinktunisia.laboratoryservice.exception.UnauthorizedAccessException;
import com.medilinktunisia.laboratoryservice.model.dto.LaboratoryCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.LaboratoryDto;
import com.medilinktunisia.laboratoryservice.model.dto.LaboratoryUpdateRequest;
import com.medilinktunisia.laboratoryservice.model.entity.Laboratory;
import com.medilinktunisia.laboratoryservice.repository.LaboratoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des laboratoires
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LaboratoryService {

    private final LaboratoryRepository laboratoryRepository;

    public LaboratoryDto createLaboratory(LaboratoryCreateRequest request) {
        log.info("Creating new laboratory: {}", request.getName());

        if (laboratoryRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("Un laboratoire avec ce numéro de licence existe déjà");
        }

        if (laboratoryRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("Cet utilisateur a déjà un laboratoire enregistré");
        }

        Laboratory laboratory = Laboratory.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .licenseNumber(request.getLicenseNumber())
                .directorName(request.getDirectorName())
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
                .accreditations(request.getAccreditations())
                .specialties(request.getSpecialties())
                .homeCollection(request.getHomeCollection() != null ? request.getHomeCollection() : true)
                .urgentAnalysisAvailable(request.getUrgentAnalysisAvailable() != null ? 
                        request.getUrgentAnalysisAvailable() : true)
                .status(Laboratory.LaboratoryStatus.PENDING_APPROVAL)
                .description(request.getDescription())
                .website(request.getWebsite())
                .averageTurnaroundTimeHours(request.getAverageTurnaroundTimeHours())
                .build();

        laboratory = laboratoryRepository.save(laboratory);
        log.info("Laboratory created successfully with ID: {}", laboratory.getId());

        return mapToDto(laboratory);
    }

    public LaboratoryDto updateLaboratory(Long laboratoryId, LaboratoryUpdateRequest request, Long currentUserId) {
        log.info("Updating laboratory ID: {}", laboratoryId);

        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> new LaboratoryNotFoundException("Laboratoire non trouvé"));

        if (!laboratory.getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier ce laboratoire");
        }

        if (request.getName() != null) laboratory.setName(request.getName());
        if (request.getDirectorName() != null) laboratory.setDirectorName(request.getDirectorName());
        if (request.getAddress() != null) laboratory.setAddress(request.getAddress());
        if (request.getCity() != null) laboratory.setCity(request.getCity());
        if (request.getZipCode() != null) laboratory.setZipCode(request.getZipCode());
        if (request.getRegion() != null) laboratory.setRegion(request.getRegion());
        if (request.getPhone() != null) laboratory.setPhone(request.getPhone());
        if (request.getAlternativePhone() != null) laboratory.setAlternativePhone(request.getAlternativePhone());
        if (request.getEmail() != null) laboratory.setEmail(request.getEmail());
        if (request.getLatitude() != null) laboratory.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) laboratory.setLongitude(request.getLongitude());
        if (request.getOpeningHours() != null) laboratory.setOpeningHours(request.getOpeningHours());
        if (request.getAccreditations() != null) laboratory.setAccreditations(request.getAccreditations());
        if (request.getSpecialties() != null) laboratory.setSpecialties(request.getSpecialties());
        if (request.getHomeCollection() != null) laboratory.setHomeCollection(request.getHomeCollection());
        if (request.getUrgentAnalysisAvailable() != null) 
            laboratory.setUrgentAnalysisAvailable(request.getUrgentAnalysisAvailable());
        if (request.getDescription() != null) laboratory.setDescription(request.getDescription());
        if (request.getWebsite() != null) laboratory.setWebsite(request.getWebsite());
        if (request.getAverageTurnaroundTimeHours() != null) 
            laboratory.setAverageTurnaroundTimeHours(request.getAverageTurnaroundTimeHours());

        laboratory = laboratoryRepository.save(laboratory);
        log.info("Laboratory updated successfully");

        return mapToDto(laboratory);
    }

    @Transactional(readOnly = true)
    public LaboratoryDto getLaboratoryById(Long id) {
        Laboratory laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new LaboratoryNotFoundException("Laboratoire non trouvé"));
        return mapToDto(laboratory);
    }

    @Transactional(readOnly = true)
    public LaboratoryDto getLaboratoryByUserId(Long userId) {
        Laboratory laboratory = laboratoryRepository.findByUserId(userId)
                .orElseThrow(() -> new LaboratoryNotFoundException("Aucun laboratoire trouvé pour cet utilisateur"));
        return mapToDto(laboratory);
    }

    @Transactional(readOnly = true)
    public Page<LaboratoryDto> getAllLaboratories(Pageable pageable) {
        return laboratoryRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<LaboratoryDto> searchLaboratoriesByName(String name, Pageable pageable) {
        return laboratoryRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<LaboratoryDto> getLaboratoriesByCity(String city) {
        return laboratoryRepository.findByCity(city).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LaboratoryDto> getActiveLaboratoriesByCity(String city) {
        return laboratoryRepository.findByCityAndStatus(city, Laboratory.LaboratoryStatus.ACTIVE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LaboratoryDto> getLaboratoriesNearby(double latitude, double longitude, double radiusKm) {
        return laboratoryRepository.findLaboratoriesWithinRadius(latitude, longitude, radiusKm).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LaboratoryDto> getHomeCollectionLaboratories(String city) {
        return laboratoryRepository.findHomeCollectionLaboratoriesInCity(city).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LaboratoryDto> getUrgentAnalysisLaboratories() {
        return laboratoryRepository.findUrgentAnalysisLaboratories().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public LaboratoryDto updateLaboratoryStatus(Long laboratoryId, Laboratory.LaboratoryStatus status) {
        log.info("Updating laboratory status to: {}", status);

        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> new LaboratoryNotFoundException("Laboratoire non trouvé"));

        laboratory.setStatus(status);
        laboratory = laboratoryRepository.save(laboratory);

        log.info("Laboratory status updated successfully");
        return mapToDto(laboratory);
    }

    public void deleteLaboratory(Long laboratoryId, Long currentUserId) {
        log.info("Deleting laboratory ID: {}", laboratoryId);

        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> new LaboratoryNotFoundException("Laboratoire non trouvé"));

        if (!laboratory.getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à supprimer ce laboratoire");
        }

        laboratoryRepository.delete(laboratory);
        log.info("Laboratory deleted successfully");
    }

    @Transactional(readOnly = true)
    public long countActiveLaboratories() {
        return laboratoryRepository.countActiveLaboratories();
    }

    private LaboratoryDto mapToDto(Laboratory laboratory) {
        return LaboratoryDto.builder()
                .id(laboratory.getId())
                .userId(laboratory.getUserId())
                .name(laboratory.getName())
                .licenseNumber(laboratory.getLicenseNumber())
                .directorName(laboratory.getDirectorName())
                .address(laboratory.getAddress())
                .city(laboratory.getCity())
                .zipCode(laboratory.getZipCode())
                .region(laboratory.getRegion())
                .phone(laboratory.getPhone())
                .alternativePhone(laboratory.getAlternativePhone())
                .email(laboratory.getEmail())
                .latitude(laboratory.getLatitude())
                .longitude(laboratory.getLongitude())
                .openingHours(laboratory.getOpeningHours())
                .accreditations(laboratory.getAccreditations())
                .specialties(laboratory.getSpecialties())
                .homeCollection(laboratory.getHomeCollection())
                .urgentAnalysisAvailable(laboratory.getUrgentAnalysisAvailable())
                .status(laboratory.getStatus())
                .description(laboratory.getDescription())
                .website(laboratory.getWebsite())
                .totalAnalyses(laboratory.getTotalAnalyses())
                .totalPatients(laboratory.getTotalPatients())
                .rating(laboratory.getRating())
                .reviewsCount(laboratory.getReviewsCount())
                .averageTurnaroundTimeHours(laboratory.getAverageTurnaroundTimeHours())
                .createdAt(laboratory.getCreatedAt())
                .updatedAt(laboratory.getUpdatedAt())
                .build();
    }
}
