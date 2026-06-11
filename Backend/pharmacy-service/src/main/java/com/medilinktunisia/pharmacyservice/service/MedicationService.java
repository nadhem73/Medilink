package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.exception.MedicationNotFoundException;
import com.medilinktunisia.pharmacyservice.model.dto.MedicationDto;
import com.medilinktunisia.pharmacyservice.model.entity.Medication;
import com.medilinktunisia.pharmacyservice.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des médicaments
 * Référentiel centralisé des médicaments
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MedicationService {

    private final MedicationRepository medicationRepository;

    /**
     * Récupérer tous les médicaments
     */
    public Page<MedicationDto> getAllMedications(Pageable pageable) {
        return medicationRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * Récupérer un médicament par ID
     */
    public MedicationDto getMedicationById(Long id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new MedicationNotFoundException(id));
        return convertToDto(medication);
    }

    /**
     * Récupérer un médicament par code
     */
    public MedicationDto getMedicationByCode(String code) {
        Medication medication = medicationRepository.findByMedicationCode(code)
                .orElseThrow(() -> new MedicationNotFoundException("Médicament non trouvé avec le code: " + code));
        return convertToDto(medication);
    }

    /**
     * Rechercher des médicaments
     */
    public Page<MedicationDto> searchMedications(String searchTerm, Pageable pageable) {
        return medicationRepository.searchMedications(searchTerm, pageable)
                .map(this::convertToDto);
    }

    /**
     * Récupérer les médicaments par catégorie
     */
    public Page<MedicationDto> getMedicationsByCategory(Medication.MedicationCategory category, Pageable pageable) {
        return medicationRepository.findByCategory(category, pageable)
                .map(this::convertToDto);
    }

    /**
     * Récupérer les médicaments disponibles sans ordonnance
     */
    public List<MedicationDto> getOverTheCounterMedications() {
        return medicationRepository.findOverTheCounterMedications()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Conversion Entity -> DTO
     */
    private MedicationDto convertToDto(Medication medication) {
        return MedicationDto.builder()
                .id(medication.getId())
                .medicationCode(medication.getMedicationCode())
                .name(medication.getName())
                .scientificName(medication.getScientificName())
                .manufacturer(medication.getManufacturer())
                .category(medication.getCategory())
                .form(medication.getForm())
                .dosage(medication.getDosage())
                .dosageUnit(medication.getDosageUnit())
                .price(medication.getPrice())
                .subsidizedPrice(medication.getSubsidizedPrice())
                .reimbursable(medication.getReimbursable())
                .reimbursementRate(medication.getReimbursementRate())
                .requiresPrescription(medication.getRequiresPrescription())
                .prescriptionType(medication.getPrescriptionType())
                .indications(medication.getIndications())
                .contraindications(medication.getContraindications())
                .sideEffects(medication.getSideEffects())
                .dosageInstructions(medication.getDosageInstructions())
                .precautions(medication.getPrecautions())
                .activeIngredient(medication.getActiveIngredient())
                .composition(medication.getComposition())
                .packaging(medication.getPackaging())
                .status(medication.getStatus())
                .active(medication.getActive())
                .createdAt(medication.getCreatedAt())
                .updatedAt(medication.getUpdatedAt())
                .build();
    }
}
