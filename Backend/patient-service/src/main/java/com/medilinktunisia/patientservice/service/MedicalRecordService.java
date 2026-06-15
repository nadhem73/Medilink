package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.dto.MedicalRecordDto;
import com.medilinktunisia.patientservice.dto.MedicalRecordRequest;
import com.medilinktunisia.patientservice.model.MedicalRecord;
import com.medilinktunisia.patientservice.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository repository;

    /**
     * Crée le dossier médical d'un patient (appelé par l'auth-service à l'inscription).
     * Idempotent : ne recrée pas si un dossier existe déjà pour cet utilisateur.
     */
    public void createMedicalRecord(MedicalRecordRequest request) {
        if (request.getUserId() == null || repository.existsByUserId(request.getUserId())) {
            return;
        }
        MedicalRecord record = new MedicalRecord();
        record.setUserId(request.getUserId());
        record.setBloodGroup(request.getBloodGroup());
        record.setHeight(request.getHeight());
        record.setWeight(request.getWeight());
        record.setAllergies(request.getAllergies());
        record.setChronicDiseases(request.getChronicDiseases());
        record.setCurrentTreatments(request.getCurrentTreatments());
        record.setEmergencyContactName(request.getEmergencyContactName());
        record.setEmergencyContactPhone(request.getEmergencyContactPhone());
        record.setInsuranceCompany(request.getInsuranceCompany());
        record.setInsuranceNumber(request.getInsuranceNumber());
        repository.save(record);
    }

    /** Dossier médical de l'utilisateur connecté ; vide si aucun n'existe encore. */
    public MedicalRecordDto getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(this::toDto)
                .orElseGet(() -> MedicalRecordDto.builder().userId(userId).build());
    }

    private MedicalRecordDto toDto(MedicalRecord r) {
        return MedicalRecordDto.builder()
                .userId(r.getUserId())
                .bloodGroup(r.getBloodGroup())
                .height(r.getHeight())
                .weight(r.getWeight())
                .allergies(r.getAllergies())
                .chronicDiseases(r.getChronicDiseases())
                .currentTreatments(r.getCurrentTreatments())
                .emergencyContactName(r.getEmergencyContactName())
                .emergencyContactPhone(r.getEmergencyContactPhone())
                .insuranceCompany(r.getInsuranceCompany())
                .insuranceNumber(r.getInsuranceNumber())
                .build();
    }
}
