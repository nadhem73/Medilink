package com.medilinktunisia.prescriptionservice.service;

import com.medilinktunisia.prescriptionservice.exception.PrescriptionExpiredException;
import com.medilinktunisia.prescriptionservice.exception.PrescriptionNotFoundException;
import com.medilinktunisia.prescriptionservice.exception.UnauthorizedAccessException;
import com.medilinktunisia.prescriptionservice.model.dto.DispenseRequest;
import com.medilinktunisia.prescriptionservice.model.dto.MedicationItemDto;
import com.medilinktunisia.prescriptionservice.model.dto.PrescriptionCreateRequest;
import com.medilinktunisia.prescriptionservice.model.dto.PrescriptionDto;
import com.medilinktunisia.prescriptionservice.model.entity.Prescription;
import com.medilinktunisia.prescriptionservice.model.entity.PrescriptionMedication;
import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import com.medilinktunisia.prescriptionservice.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Transactional
    public PrescriptionDto createPrescription(PrescriptionCreateRequest request, Long doctorId, String doctorName) {
        log.info("Creating prescription for patient ID: {}", request.getPatientId());

        Prescription prescription = Prescription.builder()
                .patientId(request.getPatientId())
                .patientName(request.getPatientName())
                .doctorId(doctorId)
                .doctorName(doctorName)
                .doctorSpecialty(request.getDoctorSpecialty())
                .diagnosis(request.getDiagnosis())
                .notes(request.getNotes())
                .prescriptionDate(LocalDateTime.now())
                .expiryDate(request.getExpiryDate() != null ? request.getExpiryDate() : LocalDateTime.now().plusDays(30))
                .status(PrescriptionStatus.ACTIVE)
                .build();

        // Ajouter les médicaments
        request.getMedications().forEach(medicationDto -> {
            PrescriptionMedication medication = PrescriptionMedication.builder()
                    .medicationName(medicationDto.getMedicationName())
                    .dosage(medicationDto.getDosage())
                    .frequency(medicationDto.getFrequency())
                    .duration(medicationDto.getDuration())
                    .quantity(medicationDto.getQuantity())
                    .instructions(medicationDto.getInstructions())
                    .build();
            prescription.addMedication(medication);
        });

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescription created successfully with number: {}", savedPrescription.getPrescriptionNumber());

        return mapToDto(savedPrescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionById(Long prescriptionId) {
        log.info("Fetching prescription with ID: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with ID: " + prescriptionId));
        return mapToDto(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionByNumber(String prescriptionNumber) {
        log.info("Fetching prescription with number: {}", prescriptionNumber);
        Prescription prescription = prescriptionRepository.findByPrescriptionNumber(prescriptionNumber)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with number: " + prescriptionNumber));
        return mapToDto(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientPrescriptions(Long patientId) {
        log.info("Fetching prescriptions for patient ID: {}", patientId);
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId);
        return prescriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getDoctorPrescriptions(Long doctorId) {
        log.info("Fetching prescriptions for doctor ID: {}", doctorId);
        List<Prescription> prescriptions = prescriptionRepository.findByDoctorIdOrderByPrescriptionDateDesc(doctorId);
        return prescriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getActivePrescriptionsByPatient(Long patientId) {
        log.info("Fetching active prescriptions for patient ID: {}", patientId);
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndStatus(patientId, PrescriptionStatus.ACTIVE);
        return prescriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionDto dispensePrescription(Long prescriptionId, DispenseRequest request, Long pharmacyId, String pharmacyName) {
        log.info("Dispensing prescription ID: {} by pharmacy ID: {}", prescriptionId, pharmacyId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with ID: " + prescriptionId));

        // Vérifier que l'ordonnance n'est pas expirée
        if (prescription.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new PrescriptionExpiredException("Prescription has expired");
        }

        // Vérifier que l'ordonnance est active
        if (prescription.getStatus() != PrescriptionStatus.ACTIVE) {
            throw new IllegalStateException("Prescription is not active");
        }

        // Marquer comme dispensée
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        prescription.setDispensedDate(LocalDateTime.now());
        prescription.setPharmacyId(pharmacyId);
        prescription.setPharmacyName(pharmacyName);

        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescription dispensed successfully");

        return mapToDto(updatedPrescription);
    }

    @Transactional
    public void cancelPrescription(Long prescriptionId, Long doctorId) {
        log.info("Cancelling prescription ID: {} by doctor ID: {}", prescriptionId, doctorId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with ID: " + prescriptionId));

        // Vérifier que le médecin est le propriétaire
        if (!prescription.getDoctorId().equals(doctorId)) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this prescription");
        }

        // Vérifier que l'ordonnance n'a pas été dispensée
        if (prescription.getStatus() == PrescriptionStatus.DISPENSED) {
            throw new IllegalStateException("Cannot cancel a dispensed prescription");
        }

        prescription.setStatus(PrescriptionStatus.CANCELLED);
        prescriptionRepository.save(prescription);
        log.info("Prescription cancelled successfully");
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> searchPrescriptions(Long patientId, Long doctorId, PrescriptionStatus status, 
                                                      LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Searching prescriptions with filters");
        List<Prescription> prescriptions;

        if (patientId != null && status != null) {
            prescriptions = prescriptionRepository.findByPatientIdAndStatus(patientId, status);
        } else if (patientId != null) {
            prescriptions = prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId);
        } else if (doctorId != null) {
            prescriptions = prescriptionRepository.findByDoctorIdOrderByPrescriptionDateDesc(doctorId);
        } else {
            prescriptions = prescriptionRepository.findAll();
        }

        // Filtrer par dates si nécessaire
        if (startDate != null && endDate != null) {
            prescriptions = prescriptions.stream()
                    .filter(p -> p.getPrescriptionDate().isAfter(startDate) && p.getPrescriptionDate().isBefore(endDate))
                    .collect(Collectors.toList());
        }

        return prescriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PrescriptionDto mapToDto(Prescription prescription) {
        List<MedicationItemDto> medications = prescription.getMedications().stream()
                .map(med -> MedicationItemDto.builder()
                        .id(med.getId())
                        .medicationName(med.getMedicationName())
                        .dosage(med.getDosage())
                        .frequency(med.getFrequency())
                        .duration(med.getDuration())
                        .quantity(med.getQuantity())
                        .instructions(med.getInstructions())
                        .build())
                .collect(Collectors.toList());

        return PrescriptionDto.builder()
                .id(prescription.getId())
                .prescriptionNumber(prescription.getPrescriptionNumber())
                .patientId(prescription.getPatientId())
                .patientName(prescription.getPatientName())
                .doctorId(prescription.getDoctorId())
                .doctorName(prescription.getDoctorName())
                .doctorSpecialty(prescription.getDoctorSpecialty())
                .diagnosis(prescription.getDiagnosis())
                .notes(prescription.getNotes())
                .status(prescription.getStatus())
                .prescriptionDate(prescription.getPrescriptionDate())
                .expiryDate(prescription.getExpiryDate())
                .dispensedDate(prescription.getDispensedDate())
                .pharmacyId(prescription.getPharmacyId())
                .pharmacyName(prescription.getPharmacyName())
                .medications(medications)
                .createdAt(prescription.getCreatedAt())
                .updatedAt(prescription.getUpdatedAt())
                .build();
    }
}
