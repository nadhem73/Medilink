package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.dto.MedicalRecordDto;
import com.medilinktunisia.patientservice.dto.MedicalRecordRequest;
import com.medilinktunisia.patientservice.model.MedicalRecord;
import com.medilinktunisia.patientservice.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository repository;

    /**
     * Crée le dossier médical d'un patient (appelé par l'auth-service à l'inscription).
     * Idempotent : ne recrée pas si un dossier existe déjà pour cet utilisateur.
     */
    public void createMedicalRecord(MedicalRecordRequest request) {
        if (request.getUserId() == null || repository.existsByUserId(request.getUserId())) {
            log.warn("Medical record already exists for userId={}, skipping creation", request.getUserId());
            return;
        }
        log.info("Creating medical record for userId={}", request.getUserId());
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
        log.debug("Fetching medical record for userId={}", userId);
        return repository.findByUserId(userId)
                .map(this::toDto)
                .orElseGet(() -> {
                    log.info("No medical record found for userId={}, returning empty", userId);
                    return MedicalRecordDto.builder().userId(userId).build();
                });
    }

    /** Met à jour les champs non-nuls du dossier médical d'un patient. */
    public MedicalRecordDto updateMedicalRecord(Long userId, MedicalRecordRequest request) {
        MedicalRecord record = repository.findByUserId(userId)
                .orElseGet(() -> {
                    MedicalRecord newRecord = new MedicalRecord();
                    newRecord.setUserId(userId);
                    return newRecord;
                });

        if (request.getBloodGroup() != null) record.setBloodGroup(request.getBloodGroup());
        if (request.getHeight() != null) record.setHeight(request.getHeight());
        if (request.getWeight() != null) record.setWeight(request.getWeight());
        if (request.getAllergies() != null) record.setAllergies(request.getAllergies());
        if (request.getChronicDiseases() != null) record.setChronicDiseases(request.getChronicDiseases());
        if (request.getCurrentTreatments() != null) record.setCurrentTreatments(request.getCurrentTreatments());
        if (request.getEmergencyContactName() != null) record.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) record.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getInsuranceCompany() != null) record.setInsuranceCompany(request.getInsuranceCompany());
        if (request.getInsuranceNumber() != null) record.setInsuranceNumber(request.getInsuranceNumber());

        repository.save(record);
        return toDto(record);
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
