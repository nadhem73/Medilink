package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.exception.PatientNotFoundException;
import com.medilinktunisia.patientservice.model.dto.AllergyCreateRequest;
import com.medilinktunisia.patientservice.model.dto.AllergyDto;
import com.medilinktunisia.patientservice.model.entity.Patient;
import com.medilinktunisia.patientservice.model.entity.PatientAllergy;
import com.medilinktunisia.patientservice.repository.PatientAllergyRepository;
import com.medilinktunisia.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllergyService {

    private final PatientAllergyRepository allergyRepository;
    private final PatientRepository patientRepository;

    /**
     * Ajouter une allergie
     */
    @Transactional
    public AllergyDto addAllergy(Long patientId, AllergyCreateRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        PatientAllergy allergy = PatientAllergy.builder()
                .patient(patient)
                .allergenName(request.getAllergenName())
                .allergenType(request.getAllergenType())
                .reactionDescription(request.getReactionDescription())
                .severityLevel(request.getSeverityLevel())
                .firstReactionDate(request.getFirstReactionDate())
                .diagnosedDate(request.getDiagnosedDate())
                .diagnosedByDoctorId(request.getDiagnosedByDoctorId())
                .treatmentGiven(request.getTreatmentGiven())
                .additionalNotes(request.getAdditionalNotes())
                .isActive(true)
                .build();

        PatientAllergy savedAllergy = allergyRepository.save(allergy);
        log.info("Allergie ajoutée pour le patient: {}", patientId);

        return AllergyDto.fromEntity(savedAllergy);
    }

    /**
     * Récupérer toutes les allergies d'un patient
     */
    @Transactional(readOnly = true)
    public List<AllergyDto> getAllergiesByPatientId(Long patientId) {
        return allergyRepository.findByPatientIdAndIsActiveTrue(patientId).stream()
                .map(AllergyDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les allergies critiques d'un patient
     */
    @Transactional(readOnly = true)
    public List<AllergyDto> getCriticalAllergies(Long patientId) {
        return allergyRepository.findCriticalAllergies(patientId).stream()
                .map(AllergyDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer (désactiver) une allergie
     */
    @Transactional
    public void deleteAllergy(Long allergyId) {
        PatientAllergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new RuntimeException("Allergie non trouvée"));
        
        allergy.setIsActive(false);
        allergyRepository.save(allergy);
        log.info("Allergie désactivée: {}", allergyId);
    }
}
