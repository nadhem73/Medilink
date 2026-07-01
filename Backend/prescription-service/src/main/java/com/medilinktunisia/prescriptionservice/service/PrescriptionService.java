package com.medilinktunisia.prescriptionservice.service;

import com.medilinktunisia.prescriptionservice.client.DoctorServiceClient;
import com.medilinktunisia.prescriptionservice.client.PharmacyServiceClient;
import com.medilinktunisia.prescriptionservice.dto.*;
import com.medilinktunisia.prescriptionservice.model.entity.Prescription;
import com.medilinktunisia.prescriptionservice.model.entity.PrescriptionItem;
import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import com.medilinktunisia.prescriptionservice.repository.PrescriptionItemRepository;
import com.medilinktunisia.prescriptionservice.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PharmacyServiceClient pharmacyClient;
    private final DoctorServiceClient doctorClient;

    @Transactional
    public PrescriptionResponse createPrescription(Long doctorId, PrescriptionCreateRequest request) {
        List<Long> medicamentIds = request.getItems().stream()
                .map(PrescriptionItemRequest::getMedicamentId)
                .toList();

        Map<Long, Integer> stockMap = pharmacyClient.checkStock(medicamentIds);

        List<StockCheckResult> outOfStock = new ArrayList<>();
        for (PrescriptionItemRequest item : request.getItems()) {
            Integer stock = stockMap.get(item.getMedicamentId());
            if (stock == null || stock <= 0) {
                outOfStock.add(StockCheckResult.builder()
                        .medicamentId(item.getMedicamentId())
                        .medicamentName(item.getMedicamentName())
                        .inStock(false)
                        .totalStock(stock != null ? stock : 0)
                        .build());
            }
        }

        if (!outOfStock.isEmpty()) {
            StringBuilder sb = new StringBuilder("Stock insuffisant pour : ");
            for (StockCheckResult r : outOfStock) {
                sb.append(r.getMedicamentName()).append(", ");
            }
            throw new RuntimeException(sb.substring(0, sb.length() - 2));
        }

        Prescription prescription = new Prescription();
        prescription.setConsultationId(request.getConsultationId());
        prescription.setPatientId(request.getPatientId());
        prescription.setDoctorId(doctorId);
        prescription.setStatus(PrescriptionStatus.SOUMISE);
        prescription.setNotes(request.getNotes());
        prescription.setItems(new ArrayList<>());

        Prescription saved = prescriptionRepository.save(prescription);

        for (PrescriptionItemRequest itemReq : request.getItems()) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(saved);
            item.setMedicamentId(itemReq.getMedicamentId());
            item.setMedicamentName(itemReq.getMedicamentName());
            item.setDosage(itemReq.getDosage());
            item.setForme(itemReq.getForme());
            item.setPosologie(itemReq.getPosologie());
            item.setDureeTraitement(itemReq.getDureeTraitement());
            item.setVoieAdministration(itemReq.getVoieAdministration());
            item.setInstructions(itemReq.getInstructions());
            saved.getItems().add(item);
        }

        prescriptionRepository.save(saved);

        try {
            doctorClient.linkPrescriptionToConsultation(request.getConsultationId(), saved.getId());
        } catch (Exception e) {
            log.warn("Could not link prescription to consultation: {}", e.getMessage());
        }

        return toDto(saved);
    }

    public PrescriptionResponse getPrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));
        return toDto(prescription);
    }

    public PrescriptionResponse getPrescriptionByConsultation(Long consultationId) {
        Prescription prescription = prescriptionRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new RuntimeException("No prescription found for consultation: " + consultationId));
        return toDto(prescription);
    }

    public List<PrescriptionResponse> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public PrescriptionResponse updatePrescription(Long id, Long doctorId, PrescriptionCreateRequest request) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));

        if (!prescription.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized: prescription belongs to another doctor");
        }

        if (prescription.getStatus() != PrescriptionStatus.BROUILLON
                && prescription.getStatus() != PrescriptionStatus.SOUMISE) {
            throw new RuntimeException("Cannot modify a dispensed or cancelled prescription");
        }

        prescription.getItems().clear();
        if (request.getNotes() != null) {
            prescription.setNotes(request.getNotes());
        }

        for (PrescriptionItemRequest itemReq : request.getItems()) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setMedicamentId(itemReq.getMedicamentId());
            item.setMedicamentName(itemReq.getMedicamentName());
            item.setDosage(itemReq.getDosage());
            item.setForme(itemReq.getForme());
            item.setPosologie(itemReq.getPosologie());
            item.setDureeTraitement(itemReq.getDureeTraitement());
            item.setVoieAdministration(itemReq.getVoieAdministration());
            item.setInstructions(itemReq.getInstructions());
            prescription.getItems().add(item);
        }

        return toDto(prescriptionRepository.save(prescription));
    }

    @Transactional
    public void cancelPrescription(Long id, Long doctorId) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));

        if (!prescription.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized: prescription belongs to another doctor");
        }

        if (prescription.getStatus() == PrescriptionStatus.DISPENSEE) {
            throw new RuntimeException("Cannot cancel a dispensed prescription");
        }

        prescription.setStatus(PrescriptionStatus.ANNULEE);
        prescriptionRepository.save(prescription);
    }

    private PrescriptionResponse toDto(Prescription p) {
        List<PrescriptionItemResponse> items = p.getItems().stream()
                .map(i -> PrescriptionItemResponse.builder()
                        .id(i.getId())
                        .medicamentId(i.getMedicamentId())
                        .medicamentName(i.getMedicamentName())
                        .dosage(i.getDosage())
                        .forme(i.getForme())
                        .posologie(i.getPosologie())
                        .dureeTraitement(i.getDureeTraitement())
                        .voieAdministration(i.getVoieAdministration())
                        .instructions(i.getInstructions())
                        .quantitePrescrite(i.getQuantitePrescrite())
                        .build())
                .toList();

        return PrescriptionResponse.builder()
                .id(p.getId())
                .consultationId(p.getConsultationId())
                .patientId(p.getPatientId())
                .doctorId(p.getDoctorId())
                .pharmacieId(p.getPharmacieId())
                .status(p.getStatus().name())
                .notes(p.getNotes())
                .items(items)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
