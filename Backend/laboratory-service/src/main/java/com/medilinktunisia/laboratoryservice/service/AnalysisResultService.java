package com.medilinktunisia.laboratoryservice.service;

import com.medilinktunisia.laboratoryservice.exception.AnalysisRequestNotFoundException;
import com.medilinktunisia.laboratoryservice.exception.AnalysisResultNotFoundException;
import com.medilinktunisia.laboratoryservice.exception.AnalysisTypeNotFoundException;
import com.medilinktunisia.laboratoryservice.exception.UnauthorizedAccessException;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisResultCreateRequest;
import com.medilinktunisia.laboratoryservice.model.dto.AnalysisResultDto;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisRequest;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisResult;
import com.medilinktunisia.laboratoryservice.model.entity.AnalysisType;
import com.medilinktunisia.laboratoryservice.repository.AnalysisRequestRepository;
import com.medilinktunisia.laboratoryservice.repository.AnalysisResultRepository;
import com.medilinktunisia.laboratoryservice.repository.AnalysisTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des résultats d'analyses
 * Selon cahier des charges Section 6.5 - Upload des résultats au format PDF
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalysisResultService {

    private final AnalysisResultRepository resultRepository;
    private final AnalysisRequestRepository requestRepository;
    private final AnalysisTypeRepository analysisTypeRepository;
    private final FileUploadService fileUploadService;

    /**
     * Créer un nouveau résultat d'analyse
     */
    public AnalysisResultDto createResult(AnalysisResultCreateRequest request, Long currentUserId) {
        log.info("Creating analysis result for request: {}", request.getRequestId());

        // Vérifier que la demande existe
        AnalysisRequest analysisRequest = requestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new AnalysisRequestNotFoundException("Demande d'analyse non trouvée"));

        // Vérifier l'autorisation
        if (!analysisRequest.getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à ajouter des résultats pour cette demande");
        }

        // Vérifier que le type d'analyse existe
        AnalysisType analysisType = analysisTypeRepository.findById(request.getAnalysisTypeId())
                .orElseThrow(() -> new AnalysisTypeNotFoundException("Type d'analyse non trouvé"));

        // Créer le résultat
        AnalysisResult result = AnalysisResult.builder()
                .request(analysisRequest)
                .analysisType(analysisType)
                .result(request.getResult())
                .unit(request.getUnit())
                .referenceRange(request.getReferenceRange())
                .resultStatus(request.getResultStatus())
                .interpretation(request.getInterpretation())
                .comments(request.getComments())
                .performedBy(request.getPerformedBy())
                .performedAt(request.getPerformedAt() != null ? request.getPerformedAt() : LocalDateTime.now())
                .validated(false)
                .build();

        result = resultRepository.save(result);
        log.info("Analysis result created successfully with ID: {}", result.getId());

        return mapToDto(result);
    }

    /**
     * Upload d'un fichier PDF de résultat
     */
    public AnalysisResultDto uploadPdfResult(Long resultId, MultipartFile file, Long currentUserId) {
        log.info("Uploading PDF result for result ID: {}", resultId);

        AnalysisResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new AnalysisResultNotFoundException("Résultat d'analyse non trouvé"));

        // Vérifier l'autorisation
        if (!result.getRequest().getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à uploader des résultats pour cette demande");
        }

        // Supprimer l'ancien fichier si existant
        if (result.getPdfFilePath() != null) {
            fileUploadService.deleteFile(result.getPdfFilePath());
        }

        // Upload du nouveau fichier
        String filePath = fileUploadService.uploadPdfResult(file);
        result.setPdfFileName(file.getOriginalFilename());
        result.setPdfFilePath(filePath);
        result.setPdfFileSize(file.getSize());

        result = resultRepository.save(result);
        log.info("PDF result uploaded successfully");

        return mapToDto(result);
    }

    /**
     * Valider un résultat d'analyse
     */
    public AnalysisResultDto validateResult(Long resultId, String validatedBy, Long currentUserId) {
        log.info("Validating result ID: {}", resultId);

        AnalysisResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new AnalysisResultNotFoundException("Résultat d'analyse non trouvé"));

        // Vérifier l'autorisation
        if (!result.getRequest().getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à valider ce résultat");
        }

        result.setValidated(true);
        result.setValidatedBy(validatedBy);
        result.setValidatedAt(LocalDateTime.now());

        result = resultRepository.save(result);
        log.info("Result validated successfully");

        // Vérifier si tous les résultats de la demande sont validés
        checkAndUpdateRequestStatus(result.getRequest());

        return mapToDto(result);
    }

    /**
     * Mettre à jour un résultat
     */
    public AnalysisResultDto updateResult(Long resultId, AnalysisResultCreateRequest updateRequest, Long currentUserId) {
        log.info("Updating result ID: {}", resultId);

        AnalysisResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new AnalysisResultNotFoundException("Résultat d'analyse non trouvé"));

        // Vérifier l'autorisation
        if (!result.getRequest().getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier ce résultat");
        }

        // Mettre à jour les champs
        if (updateRequest.getResult() != null) {
            result.setResult(updateRequest.getResult());
        }
        if (updateRequest.getUnit() != null) {
            result.setUnit(updateRequest.getUnit());
        }
        if (updateRequest.getReferenceRange() != null) {
            result.setReferenceRange(updateRequest.getReferenceRange());
        }
        if (updateRequest.getResultStatus() != null) {
            result.setResultStatus(updateRequest.getResultStatus());
        }
        if (updateRequest.getInterpretation() != null) {
            result.setInterpretation(updateRequest.getInterpretation());
        }
        if (updateRequest.getComments() != null) {
            result.setComments(updateRequest.getComments());
        }
        if (updateRequest.getPerformedBy() != null) {
            result.setPerformedBy(updateRequest.getPerformedBy());
        }
        if (updateRequest.getPerformedAt() != null) {
            result.setPerformedAt(updateRequest.getPerformedAt());
        }

        result = resultRepository.save(result);
        log.info("Result updated successfully");

        return mapToDto(result);
    }

    /**
     * Obtenir un résultat par ID
     */
    @Transactional(readOnly = true)
    public AnalysisResultDto getResultById(Long id) {
        AnalysisResult result = resultRepository.findById(id)
                .orElseThrow(() -> new AnalysisResultNotFoundException("Résultat d'analyse non trouvé"));
        return mapToDto(result);
    }

    /**
     * Obtenir tous les résultats d'une demande
     */
    @Transactional(readOnly = true)
    public List<AnalysisResultDto> getResultsByRequest(Long requestId) {
        return resultRepository.findByRequestId(requestId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les résultats validés d'une demande
     */
    @Transactional(readOnly = true)
    public List<AnalysisResultDto> getValidatedResultsByRequest(Long requestId) {
        return resultRepository.findValidatedResultsByRequest(requestId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les résultats non validés d'une demande
     */
    @Transactional(readOnly = true)
    public List<AnalysisResultDto> getUnvalidatedResultsByRequest(Long requestId) {
        return resultRepository.findUnvalidatedResultsByRequest(requestId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer un résultat
     */
    public void deleteResult(Long resultId, Long currentUserId) {
        log.info("Deleting result ID: {}", resultId);

        AnalysisResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new AnalysisResultNotFoundException("Résultat d'analyse non trouvé"));

        // Vérifier l'autorisation
        if (!result.getRequest().getLaboratory().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à supprimer ce résultat");
        }

        // Supprimer le fichier PDF si existant
        if (result.getPdfFilePath() != null) {
            fileUploadService.deleteFile(result.getPdfFilePath());
        }

        resultRepository.delete(result);
        log.info("Result deleted successfully");
    }

    /**
     * Vérifier et mettre à jour le statut de la demande
     */
    private void checkAndUpdateRequestStatus(AnalysisRequest request) {
        List<AnalysisResult> unvalidatedResults = resultRepository.findUnvalidatedResultsByRequest(request.getId());
        
        // Si tous les résultats sont validés, marquer la demande comme READY
        if (unvalidatedResults.isEmpty() && !request.getResults().isEmpty()) {
            request.setStatus(AnalysisRequest.RequestStatus.READY);
            request.setActualResultDate(LocalDateTime.now());
            requestRepository.save(request);
            log.info("Request {} status updated to READY", request.getId());
        }
    }

    /**
     * Mapper AnalysisResult vers AnalysisResultDto
     */
    private AnalysisResultDto mapToDto(AnalysisResult result) {
        return AnalysisResultDto.builder()
                .id(result.getId())
                .requestId(result.getRequest().getId())
                .requestNumber(result.getRequest().getRequestNumber())
                .analysisTypeId(result.getAnalysisType().getId())
                .analysisTypeName(result.getAnalysisType().getName())
                .analysisTypeCode(result.getAnalysisType().getAnalysisCode())
                .result(result.getResult())
                .unit(result.getUnit())
                .referenceRange(result.getReferenceRange())
                .resultStatus(result.getResultStatus())
                .pdfFileName(result.getPdfFileName())
                .pdfFilePath(result.getPdfFilePath())
                .pdfFileSize(result.getPdfFileSize())
                .interpretation(result.getInterpretation())
                .comments(result.getComments())
                .validated(result.getValidated())
                .validatedBy(result.getValidatedBy())
                .validatedAt(result.getValidatedAt())
                .performedBy(result.getPerformedBy())
                .performedAt(result.getPerformedAt())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }
}
