package com.medilinktunisia.laboratoryservice.service;

import com.medilinktunisia.laboratoryservice.exception.AnalysisRequestNotFoundException;
import com.medilinktunisia.laboratoryservice.exception.AnalysisTypeNotFoundException;
import com.medilinktunisia.laboratoryservice.exception.LaboratoryNotFoundException;
import com.medilinktunisia.laboratoryservice.exception.UnauthorizedAccessException;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisItemDto;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisRequestCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisRequestDto;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisRequestUpdateRequest;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisItem;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisRequest;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisType;
import com.medilinktunisia.laboratoryservice.model.entity.Laboratory;
import com.medilinktunisia.laboratoryservice.repository.AnalysisRequestRepository;
import com.medilinktunisia.laboratoryservice.repository.AnalysisTypeRepository;
import com.medilinktunisia.laboratoryservice.repository.LaboratoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des demandes d'analyses
 * Selon cahier des charges Section 6.5
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalysisRequestService {

    private final AnalysisRequestRepository requestRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final AnalysisTypeRepository analysisTypeRepository;

    /**
     * Créer une nouvelle demande d'analyse
     */
    public AnalysisRequestDto createRequest(AnalysisRequestCreateRequest request, Long currentUserId) {
        log.info("Creating analysis request for laboratory: {}", request.getLaboratoryId());

        // Vérifier que le laboratoire existe
        Laboratory laboratory = laboratoryRepository.findById(request.getLaboratoryId())
                .orElseThrow(() -> new LaboratoryNotFoundException("Laboratoire non trouvé"));

        // Générer le numéro de demande unique
        String requestNumber = generateRequestNumber();

        // Créer la demande
        AnalysisRequest analysisRequest = AnalysisRequest.builder()
                .requestNumber(requestNumber)
                .laboratory(laboratory)
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .prescriptionId(request.getPrescriptionId())
                .patientName(request.getPatientName())
                .patientPhone(request.getPatientPhone())
                .patientEmail(request.getPatientEmail())
                .requestDate(request.getRequestDate())
                .collectionDate(request.getCollectionDate())
                .expectedResultDate(request.getExpectedResultDate())
                .status(AnalysisRequest.RequestStatus.PENDING)
                .priority(request.getPriority())
                .collectionType(request.getCollectionType())
                .collectionAddress(request.getCollectionAddress())
                .clinicalInfo(request.getClinicalInfo())
                .instructions(request.getInstructions())
                .assignedTechnician(request.getAssignedTechnician())
                .build();

        // Ajouter les items (analyses demandées)
        for (AnalysisRequestCreateRequest.AnalysisItemCreateRequest itemRequest : request.getItems()) {
            AnalysisType analysisType = analysisTypeRepository.findById(itemRequest.getAnalysisTypeId())
                    .orElseThrow(() -> new AnalysisTypeNotFoundException("Type d'analyse non trouvé"));

            AnalysisItem item = AnalysisItem.builder()
                    .analysisType(analysisType)
                    .price(analysisType.getBasePrice())
                    .notes(itemRequest.getNotes())
                    .status(AnalysisItem.ItemStatus.PENDING)
                    .build();

            analysisRequest.addItem(item);
        }

        // Calculer le montant total
        analysisRequest.calculateTotalAmount();

        analysisRequest = requestRepository.save(analysisRequest);
        log.info("Analysis request created successfully with number: {}", requestNumber);

        return mapToDto(analysisRequest);
    }

    /**
     * Mettre à jour le statut d'une demande
     */
    public AnalysisRequestDto updateRequestStatus(Long requestId, AnalysisRequest.RequestStatus newStatus, Long currentUserId) {
        log.info("Updating request {} status to {}", requestId, newStatus);

        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation (laboratoire owner)
        if (!request.getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier cette demande");
        }

        request.setStatus(newStatus);

        // Mettre à jour les dates selon le statut
        if (newStatus == AnalysisRequest.RequestStatus.COMPLETED || 
            newStatus == AnalysisRequest.RequestStatus.READY) {
            request.setActualResultDate(LocalDateTime.now());
        }

        request = requestRepository.save(request);
        log.info("Request status updated successfully");

        return mapToDto(request);
    }

    /**
     * Mettre à jour une demande d'analyse
     */
    public AnalysisRequestDto updateRequest(Long requestId, AnalysisRequestUpdateRequest updateRequest, Long currentUserId) {
        log.info("Updating analysis request: {}", requestId);

        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation
        if (!request.getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier cette demande");
        }

        // Mettre à jour les champs
        if (updateRequest.getPatientPhone() != null) {
            request.setPatientPhone(updateRequest.getPatientPhone());
        }
        if (updateRequest.getPatientEmail() != null) {
            request.setPatientEmail(updateRequest.getPatientEmail());
        }
        if (updateRequest.getCollectionDate() != null) {
            request.setCollectionDate(updateRequest.getCollectionDate());
        }
        if (updateRequest.getExpectedResultDate() != null) {
            request.setExpectedResultDate(updateRequest.getExpectedResultDate());
        }
        if (updateRequest.getStatus() != null) {
            request.setStatus(updateRequest.getStatus());
            if (updateRequest.getStatus() == AnalysisRequest.RequestStatus.COMPLETED ||
                updateRequest.getStatus() == AnalysisRequest.RequestStatus.READY) {
                request.setActualResultDate(LocalDateTime.now());
            }
        }
        if (updateRequest.getPriority() != null) {
            request.setPriority(updateRequest.getPriority());
        }
        if (updateRequest.getCollectionAddress() != null) {
            request.setCollectionAddress(updateRequest.getCollectionAddress());
        }
        if (updateRequest.getClinicalInfo() != null) {
            request.setClinicalInfo(updateRequest.getClinicalInfo());
        }
        if (updateRequest.getLaboratoryNotes() != null) {
            request.setLaboratoryNotes(updateRequest.getLaboratoryNotes());
        }
        if (updateRequest.getInstructions() != null) {
            request.setInstructions(updateRequest.getInstructions());
        }
        if (updateRequest.getAssignedTechnician() != null) {
            request.setAssignedTechnician(updateRequest.getAssignedTechnician());
        }
        if (updateRequest.getPatientNotified() != null) {
            request.setPatientNotified(updateRequest.getPatientNotified());
        }
        if (updateRequest.getDoctorNotified() != null) {
            request.setDoctorNotified(updateRequest.getDoctorNotified());
        }

        request = requestRepository.save(request);
        log.info("Request updated successfully");

        return mapToDto(request);
    }

    /**
     * Obtenir une demande par ID
     */
    @Transactional(readOnly = true)
    public AnalysisRequestDto getRequestById(Long id) {
        AnalysisRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));
        return mapToDto(request);
    }

    /**
     * Obtenir une demande par numéro
     */
    @Transactional(readOnly = true)
    public AnalysisRequestDto getRequestByNumber(String requestNumber) {
        AnalysisRequest request = requestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));
        return mapToDto(request);
    }

    /**
     * Obtenir toutes les demandes d'un laboratoire
     */
    @Transactional(readOnly = true)
    public Page<AnalysisRequestDto> getLaboratoryRequests(Long laboratoryId, Pageable pageable) {
        return requestRepository.findByLaboratoryId(laboratoryId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Obtenir les demandes en attente d'un laboratoire
     */
    @Transactional(readOnly = true)
    public List<AnalysisRequestDto> getPendingRequests(Long laboratoryId) {
        return requestRepository.findPendingRequests(laboratoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir l'historique des analyses d'un patient
     */
    @Transactional(readOnly = true)
    public Page<AnalysisRequestDto> getPatientHistory(Long patientId, Pageable pageable) {
        return requestRepository.findPatientAnalysisHistory(patientId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Obtenir les demandes par statut
     */
    @Transactional(readOnly = true)
    public List<AnalysisRequestDto> getRequestsByStatus(Long laboratoryId, AnalysisRequest.RequestStatus status) {
        return requestRepository.findByLaboratoryIdAndStatus(laboratoryId, status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Calculer le montant total d'une demande
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmount(Long requestId) {
        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));
        
        return request.getTotalAmount();
    }

    /**
     * Marquer une demande comme payée
     */
    public AnalysisRequestDto markAsPaid(Long requestId, BigDecimal paidAmount, Long currentUserId) {
        log.info("Marking request {} as paid", requestId);

        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation
        if (!request.getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier cette demande");
        }

        request.setPaidAmount(paidAmount);
        request.setPaid(true);

        request = requestRepository.save(request);
        log.info("Request marked as paid successfully");

        return mapToDto(request);
    }

    /**
     * Notifier le patient (marquer comme notifié)
     */
    public void notifyPatient(Long requestId, Long currentUserId) {
        log.info("Notifying patient for request: {}", requestId);

        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation
        if (!request.getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier cette demande");
        }

        request.setPatientNotified(true);
        requestRepository.save(request);

        // TODO: Intégrer avec un service de notification (email/SMS)
        log.info("Patient notified for request: {}", requestId);
    }

    /**
     * Notifier le médecin (marquer comme notifié)
     */
    public void notifyDoctor(Long requestId, Long currentUserId) {
        log.info("Notifying doctor for request: {}", requestId);

        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation
        if (!request.getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier cette demande");
        }

        request.setDoctorNotified(true);
        requestRepository.save(request);

        // TODO: Intégrer avec un service de notification
        log.info("Doctor notified for request: {}", requestId);
    }

    /**
     * Annuler une demande
     */
    public AnalysisRequestDto cancelRequest(Long requestId, Long currentUserId) {
        log.info("Cancelling request: {}", requestId);

        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation
        if (!request.getLaboratory().getUserId().equals(currentUserId) && 
            !request.getPatientId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à annuler cette demande");
        }

        request.setStatus(AnalysisRequest.RequestStatus.CANCELLED);
        request = requestRepository.save(request);

        log.info("Request cancelled successfully");
        return mapToDto(request);
    }

    /**
     * Supprimer une demande
     */
    public void deleteRequest(Long requestId, Long currentUserId) {
        log.info("Deleting request: {}", requestId);

        AnalysisRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation
        if (!request.getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à supprimer cette demande");
        }

        requestRepository.delete(request);
        log.info("Request deleted successfully");
    }

    /**
     * Générer un numéro de demande unique
     */
    private String generateRequestNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "REQ-" + timestamp;
    }

    /**
     * Mapper AnalysisRequest vers AnalysisRequestDto
     */
    private AnalysisRequestDto mapToDto(AnalysisRequest request) {
        List<AnalysisItemDto> itemDtos = request.getItems().stream()
                .map(item -> AnalysisItemDto.builder()
                        .id(item.getId())
                        .analysisTypeId(item.getAnalysisType().getId())
                        .analysisTypeName(item.getAnalysisType().getName())
                        .analysisTypeCode(item.getAnalysisType().getAnalysisCode())
                        .price(item.getPrice())
                        .notes(item.getNotes())
                        .status(item.getStatus())
                        .build())
                .collect(Collectors.toList());

        return AnalysisRequestDto.builder()
                .id(request.getId())
                .requestNumber(request.getRequestNumber())
                .laboratoryId(request.getLaboratory().getId())
                .laboratoryName(request.getLaboratory().getName())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .prescriptionId(request.getPrescriptionId())
                .patientName(request.getPatientName())
                .patientPhone(request.getPatientPhone())
                .patientEmail(request.getPatientEmail())
                .requestDate(request.getRequestDate())
                .collectionDate(request.getCollectionDate())
                .expectedResultDate(request.getExpectedResultDate())
                .actualResultDate(request.getActualResultDate())
                .status(request.getStatus())
                .priority(request.getPriority())
                .collectionType(request.getCollectionType())
                .collectionAddress(request.getCollectionAddress())
                .totalAmount(request.getTotalAmount())
                .paidAmount(request.getPaidAmount())
                .paid(request.getPaid())
                .items(itemDtos)
                .clinicalInfo(request.getClinicalInfo())
                .laboratoryNotes(request.getLaboratoryNotes())
                .instructions(request.getInstructions())
                .assignedTechnician(request.getAssignedTechnician())
                .patientNotified(request.getPatientNotified())
                .doctorNotified(request.getDoctorNotified())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
