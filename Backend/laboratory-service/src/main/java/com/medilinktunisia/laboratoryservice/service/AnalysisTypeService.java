package com.medilinktunisia.laboratoryservice.service;

import com.medilinktunisia.laboratoryservice.exception.AnalysisTypeNotFoundException;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisTypeCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisTypeDto;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisType;
import com.medilinktunisia.laboratoryservice.repository.AnalysisTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des types d'analyses
 * Selon cahier des charges Section 6.5 - Catalogue de 14 types d'analyses
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalysisTypeService {

    private final AnalysisTypeRepository analysisTypeRepository;

    /**
     * Créer un nouveau type d'analyse
     */
    public AnalysisTypeDto createAnalysisType(AnalysisTypeCreateRequest request) {
        log.info("Creating analysis type: {}", request.getName());

        // Vérifier l'unicité du code
        if (analysisTypeRepository.findByAnalysisCode(request.getAnalysisCode()).isPresent()) {
            throw new IllegalArgumentException("Un type d'analyse avec ce code existe déjà");
        }

        AnalysisType analysisType = AnalysisType.builder()
                .analysisCode(request.getAnalysisCode())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .basePrice(request.getBasePrice())
                .unit(request.getUnit())
                .referenceRanges(request.getReferenceRange())
                .turnaroundTimeHours(request.getEstimatedDuration())
                .sampleType(request.getSampleType())
                .preparationInstructions(request.getPreparationInstructions())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        analysisType = analysisTypeRepository.save(analysisType);
        log.info("Analysis type created successfully with ID: {}", analysisType.getId());

        return mapToDto(analysisType);
    }

    /**
     * Mettre à jour un type d'analyse
     */
    public AnalysisTypeDto updateAnalysisType(Long id, AnalysisTypeCreateRequest request) {
        log.info("Updating analysis type: {}", id);

        AnalysisType analysisType = analysisTypeRepository.findById(id)
                .orElseThrow(() -> new AnalysisTypeNotFoundException("Type d'analyse non trouvé"));

        // Vérifier l'unicité du code si modifié
        if (!analysisType.getAnalysisCode().equals(request.getAnalysisCode()) &&
            analysisTypeRepository.findByAnalysisCode(request.getAnalysisCode()).isPresent()) {
            throw new IllegalArgumentException("Un type d'analyse avec ce code existe déjà");
        }

        // Mettre à jour les champs
        analysisType.setAnalysisCode(request.getAnalysisCode());
        analysisType.setName(request.getName());
        analysisType.setDescription(request.getDescription());
        analysisType.setCategory(request.getCategory());
        analysisType.setBasePrice(request.getBasePrice());
        analysisType.setUnit(request.getUnit());
        analysisType.setReferenceRanges(request.getReferenceRange());
        analysisType.setTurnaroundTimeHours(request.getEstimatedDuration());
        analysisType.setSampleType(request.getSampleType());
        analysisType.setPreparationInstructions(request.getPreparationInstructions());
        analysisType.setActive(request.getActive() != null ? request.getActive() : true);

        analysisType = analysisTypeRepository.save(analysisType);
        log.info("Analysis type updated successfully");

        return mapToDto(analysisType);
    }

    /**
     * Obtenir un type d'analyse par ID
     */
    @Transactional(readOnly = true)
    public AnalysisTypeDto getAnalysisTypeById(Long id) {
        AnalysisType analysisType = analysisTypeRepository.findById(id)
                .orElseThrow(() -> new AnalysisTypeNotFoundException("Type d'analyse non trouvé"));
        return mapToDto(analysisType);
    }

    /**
     * Obtenir un type d'analyse par code
     */
    @Transactional(readOnly = true)
    public AnalysisTypeDto getAnalysisTypeByCode(String code) {
        AnalysisType analysisType = analysisTypeRepository.findByAnalysisCode(code)
                .orElseThrow(() -> new AnalysisTypeNotFoundException("Type d'analyse non trouvé"));
        return mapToDto(analysisType);
    }

    /**
     * Obtenir tous les types d'analyses actifs
     */
    @Transactional(readOnly = true)
    public List<AnalysisTypeDto> getAllActiveAnalysisTypes() {
        return analysisTypeRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir tous les types d'analyses (avec pagination)
     */
    @Transactional(readOnly = true)
    public Page<AnalysisTypeDto> getAllAnalysisTypes(Pageable pageable) {
        return analysisTypeRepository.findAll(pageable).map(this::mapToDto);
    }

    /**
     * Obtenir les types d'analyses par catégorie
     */
    @Transactional(readOnly = true)
    public List<AnalysisTypeDto> getAnalysisTypesByCategory(AnalysisType.AnalysisCategory category) {
        return analysisTypeRepository.findByCategory(category).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les types d'analyses actifs par catégorie
     */
    @Transactional(readOnly = true)
    public List<AnalysisTypeDto> getActiveAnalysisTypesByCategory(AnalysisType.AnalysisCategory category) {
        return analysisTypeRepository.findActiveByCategory(category).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des types d'analyses
     */
    @Transactional(readOnly = true)
    public Page<AnalysisTypeDto> searchAnalysisTypes(String searchTerm, Pageable pageable) {
        return analysisTypeRepository.searchAnalysisTypes(searchTerm, pageable).map(this::mapToDto);
    }

    /**
     * Activer/Désactiver un type d'analyse
     */
    public AnalysisTypeDto toggleAnalysisTypeStatus(Long id) {
        log.info("Toggling analysis type status: {}", id);

        AnalysisType analysisType = analysisTypeRepository.findById(id)
                .orElseThrow(() -> new AnalysisTypeNotFoundException("Type d'analyse non trouvé"));

        analysisType.setActive(!analysisType.getActive());
        analysisType = analysisTypeRepository.save(analysisType);

        log.info("Analysis type status toggled successfully");
        return mapToDto(analysisType);
    }

    /**
     * Supprimer un type d'analyse
     */
    public void deleteAnalysisType(Long id) {
        log.info("Deleting analysis type: {}", id);

        AnalysisType analysisType = analysisTypeRepository.findById(id)
                .orElseThrow(() -> new AnalysisTypeNotFoundException("Type d'analyse non trouvé"));

        // Désactiver au lieu de supprimer pour préserver l'intégrité des données
        analysisType.setActive(false);
        analysisTypeRepository.save(analysisType);

        log.info("Analysis type deactivated successfully");
    }

    /**
     * Compter les types d'analyses actifs
     */
    @Transactional(readOnly = true)
    public long countActiveAnalysisTypes() {
        return analysisTypeRepository.countActiveAnalysisTypes();
    }

    /**
     * Mapper AnalysisType vers AnalysisTypeDto
     */
    private AnalysisTypeDto mapToDto(AnalysisType analysisType) {
        return AnalysisTypeDto.builder()
                .id(analysisType.getId())
                .analysisCode(analysisType.getAnalysisCode())
                .name(analysisType.getName())
                .description(analysisType.getDescription())
                .category(analysisType.getCategory())
                .basePrice(analysisType.getBasePrice())
                .unit(analysisType.getUnit())
                .referenceRange(analysisType.getReferenceRanges())
                .estimatedDuration(analysisType.getTurnaroundTimeHours())
                .sampleType(analysisType.getSampleType())
                .preparationInstructions(analysisType.getPreparationInstructions())
                .fastingRequired(false) // Not in entity, default to false
                .active(analysisType.getActive())
                .createdAt(analysisType.getCreatedAt())
                .updatedAt(analysisType.getUpdatedAt())
                .build();
    }
}
