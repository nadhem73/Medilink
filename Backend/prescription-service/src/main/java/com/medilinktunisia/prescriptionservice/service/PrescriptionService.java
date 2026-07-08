package com.medilinktunisia.prescriptionservice.service;

import com.medilinktunisia.prescriptionservice.client.DoctorServiceClient;
import com.medilinktunisia.prescriptionservice.client.PharmacyServiceClient;
import com.medilinktunisia.prescriptionservice.dto.*;
import com.medilinktunisia.prescriptionservice.model.entity.PickupCode;
import com.medilinktunisia.prescriptionservice.model.entity.Prescription;
import com.medilinktunisia.prescriptionservice.model.entity.PrescriptionItem;
import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import com.medilinktunisia.prescriptionservice.repository.PickupCodeRepository;
import com.medilinktunisia.prescriptionservice.repository.PrescriptionItemRepository;
import com.medilinktunisia.prescriptionservice.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PickupCodeRepository pickupCodeRepository;
    private final PharmacyServiceClient pharmacyClient;
    private final DoctorServiceClient doctorClient;
    private final RestTemplate restTemplate;

    @Value("${n8n.webhook.base-url:http://localhost:5678/webhook}")
    private String n8nWebhookBaseUrl;

    private static final java.util.Map<String, String> N8N_WEBHOOK_PATHS = java.util.Map.of(
            "prescription-created", "iYlm5Sv4hR97rixn/webhook/prescription-created",
            "prescription-prepared", "ScrkfsBPpbxh1227/webhook/prescription-prepared"
    );

    @Transactional(noRollbackFor = Exception.class)
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

        PrescriptionResponse response = toDto(saved);
        notifyN8n("prescription-created", response);
        return response;
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));
        return toDto(prescription);
    }

    @Transactional(readOnly = true)
    public Optional<PrescriptionResponse> getPrescriptionByConsultation(Long consultationId) {
        return prescriptionRepository.findByConsultationId(consultationId)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getAllPrescriptions() {
        return prescriptionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
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
    public PrescriptionResponse updateStatus(Long id, PrescriptionStatus newStatus) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));

        if (prescription.getStatus() == PrescriptionStatus.ANNULEE
                || prescription.getStatus() == PrescriptionStatus.DISPENSEE) {
            throw new RuntimeException("Cannot change status of a " +
                    prescription.getStatus().name().toLowerCase() + " prescription");
        }

        if (newStatus == PrescriptionStatus.EN_PREPARATION
                && prescription.getStatus() != PrescriptionStatus.SOUMISE) {
            throw new RuntimeException("Only submitted prescriptions can be put in preparation");
        }

        if (newStatus == PrescriptionStatus.PREPAREE
                && prescription.getStatus() != PrescriptionStatus.EN_PREPARATION) {
            throw new RuntimeException("Only prescriptions in preparation can be marked as prepared");
        }

        if (newStatus == PrescriptionStatus.RETIREE
                && prescription.getStatus() != PrescriptionStatus.PREPAREE) {
            throw new RuntimeException("Only prepared prescriptions can be picked up");
        }

        if (newStatus == PrescriptionStatus.DISPENSEE
                && prescription.getStatus() != PrescriptionStatus.RETIREE) {
            throw new RuntimeException("Only picked-up prescriptions can be dispensed");
        }

        prescription.setStatus(newStatus);
        PrescriptionResponse response = toDto(prescriptionRepository.save(prescription));

        if (newStatus == PrescriptionStatus.SOUMISE) {
            notifyN8n("prescription-created", response);
        } else if (newStatus == PrescriptionStatus.PREPAREE) {
            String code = generatePickupCode(id);
            response.setPickupCode(code);
            notifyN8n("prescription-prepared", response);
        }

        return response;
    }

    @Transactional
    public PrescriptionResponse assignToPharmacy(Long prescriptionId, Long pharmacyId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        if (prescription.getPharmacieId() != null) {
            throw new RuntimeException("Pharmacy already assigned to this prescription");
        }
        prescription.setPharmacieId(pharmacyId);
        return toDto(prescriptionRepository.save(prescription));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByPharmacy(Long pharmacyId) {
        return prescriptionRepository.findByPharmacieIdOrderByCreatedAtDesc(pharmacyId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    private String generatePickupCode(Long prescriptionId) {
        if (pickupCodeRepository.existsByPrescriptionId(prescriptionId)) {
            return pickupCodeRepository.findByPrescriptionId(prescriptionId).get().getCode();
        }
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        PickupCode pickupCode = new PickupCode();
        pickupCode.setPrescriptionId(prescriptionId);
        pickupCode.setCode(code);
        pickupCodeRepository.save(pickupCode);
        return code;
    }

    public PickupCodeResponse storePickupCode(Long prescriptionId, String code) {
        if (pickupCodeRepository.existsByPrescriptionId(prescriptionId)) {
            throw new RuntimeException("Pickup code already exists for this prescription");
        }
        PickupCode pickupCode = new PickupCode();
        pickupCode.setPrescriptionId(prescriptionId);
        pickupCode.setCode(code);
        PickupCode saved = pickupCodeRepository.save(pickupCode);
        return PickupCodeResponse.builder()
                .id(saved.getId())
                .prescriptionId(saved.getPrescriptionId())
                .code(saved.getCode())
                .used(saved.isUsed())
                .build();
    }

    @Transactional(readOnly = true)
    public PickupCodeResponse getPickupCode(Long prescriptionId) {
        PickupCode pickupCode = pickupCodeRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new RuntimeException("No pickup code found for prescription: " + prescriptionId));
        return PickupCodeResponse.builder()
                .id(pickupCode.getId())
                .prescriptionId(pickupCode.getPrescriptionId())
                .code(pickupCode.getCode())
                .used(pickupCode.isUsed())
                .build();
    }

    @Transactional
    public PrescriptionResponse validatePickupCode(Long prescriptionId, String code) {
        PickupCode pickupCode = pickupCodeRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new RuntimeException("No pickup code found for this prescription"));

        if (pickupCode.isUsed()) {
            throw new RuntimeException("Pickup code has already been used");
        }

        if (!pickupCode.getCode().equals(code)) {
            throw new RuntimeException("Invalid pickup code");
        }

        pickupCode.setUsed(true);
        pickupCodeRepository.save(pickupCode);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        prescription.setStatus(PrescriptionStatus.RETIREE);
        return toDto(prescriptionRepository.save(prescription));
    }

    private void notifyN8n(String event, PrescriptionResponse data) {
        try {
            String chatId = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> resp = restTemplate.getForObject(
                        "http://localhost:8081/api/auth/patients/" + data.getPatientId() + "/telegram",
                        Map.class);
                if (resp != null) {
                    chatId = resp.get("telegramChatId");
                }
            } catch (Exception e) {
                log.warn("Could not fetch patient telegram chat ID: {}", e.getMessage());
            }

            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("id", data.getId());
            payload.put("patientId", data.getPatientId());
            payload.put("doctorId", data.getDoctorId());
            payload.put("status", data.getStatus());
            payload.put("patientTelegramChatId", chatId != null && !chatId.isEmpty() ? chatId : null);
            payload.put("pickupCode", data.getPickupCode());
            payload.put("baseUrl", "http://localhost:8765");

            String webhookPath = N8N_WEBHOOK_PATHS.getOrDefault(event, event);
            restTemplate.postForEntity(
                    n8nWebhookBaseUrl + "/" + webhookPath,
                    payload,
                    Void.class);
            log.info("n8n notified: {} -> {} for prescription {} (chatId={})", event, webhookPath, data.getId(), chatId);
        } catch (Exception e) {
            log.warn("Failed to notify n8n ({}): {}", event, e.getMessage());
        }
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
