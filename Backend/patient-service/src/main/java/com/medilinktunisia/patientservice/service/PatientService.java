package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.exception.PatientNotFoundException;
import com.medilinktunisia.patientservice.model.dto.PatientCreateRequest;
import com.medilinktunisia.patientservice.model.dto.PatientDto;
import com.medilinktunisia.patientservice.model.dto.PatientUpdateRequest;
import com.medilinktunisia.patientservice.model.entity.Patient;
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
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Créer un nouveau patient
     */
    @Transactional
    public PatientDto createPatient(PatientCreateRequest request) {
        // Vérifier si le patient existe déjà
        if (patientRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Un profil patient existe déjà pour cet utilisateur");
        }

        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Créer le patient
        Patient patient = Patient.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .emergencyContactRelationship(request.getEmergencyContactRelationship())
                .insuranceNumber(request.getInsuranceNumber())
                .insuranceProvider(request.getInsuranceProvider())
                .isActive(true)
                .build();

        Patient savedPatient = patientRepository.save(patient);
        log.info("Nouveau patient créé: {} {}", savedPatient.getFirstName(), savedPatient.getLastName());

        return PatientDto.fromEntity(savedPatient);
    }

    /**
     * Récupérer un patient par ID
     */
    @Transactional(readOnly = true)
    public PatientDto getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        return PatientDto.fromEntity(patient);
    }

    /**
     * Récupérer un patient par User ID
     */
    @Transactional(readOnly = true)
    public PatientDto getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException("Patient non trouvé pour l'utilisateur: " + userId));
        return PatientDto.fromEntity(patient);
    }

    /**
     * Récupérer tous les patients
     */
    @Transactional(readOnly = true)
    public List<PatientDto> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(PatientDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour un patient
     */
    @Transactional
    public PatientDto updatePatient(Long id, PatientUpdateRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        // Mise à jour des champs
        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            patient.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            patient.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            patient.setCity(request.getCity());
        }
        if (request.getPostalCode() != null) {
            patient.setPostalCode(request.getPostalCode());
        }
        if (request.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getEmergencyContactRelationship() != null) {
            patient.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        }
        if (request.getInsuranceNumber() != null) {
            patient.setInsuranceNumber(request.getInsuranceNumber());
        }
        if (request.getInsuranceProvider() != null) {
            patient.setInsuranceProvider(request.getInsuranceProvider());
        }

        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient mis à jour: {}", updatedPatient.getId());

        return PatientDto.fromEntity(updatedPatient);
    }

    /**
     * Supprimer (désactiver) un patient
     */
    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        
        patient.setIsActive(false);
        patientRepository.save(patient);
        log.info("Patient désactivé: {}", id);
    }

    /**
     * Vérifier si un patient existe
     */
    @Transactional(readOnly = true)
    public boolean patientExists(Long id) {
        return patientRepository.existsById(id);
    }
}
