package com.medilinktunisia.bilanservice.service;

import com.medilinktunisia.bilanservice.dto.*;
import com.medilinktunisia.bilanservice.model.entity.Bilan;
import com.medilinktunisia.bilanservice.model.entity.BilanResult;
import com.medilinktunisia.bilanservice.model.enums.BilanStatus;
import com.medilinktunisia.bilanservice.model.enums.FormatBilan;
import com.medilinktunisia.bilanservice.model.enums.ResultStatus;
import com.medilinktunisia.bilanservice.model.enums.ReviewStatus;
import com.medilinktunisia.bilanservice.repository.BilanRepository;
import com.medilinktunisia.bilanservice.repository.BilanResultRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BilanService {

    private final BilanRepository bilanRepository;
    private final BilanResultRepository bilanResultRepository;
    private final OcrClientService ocrClientService;
    private final ImageStorageService imageStorageService;

    @Transactional
    public ScanBilanResponse scanBilan(MultipartFile image, Long patientId) {
        String imagePath = imageStorageService.saveImage(image);

        String base64Image = imageStorageService.toBase64(image);

        OcrResponse ocrResponse = ocrClientService.processOcr(base64Image);

        if (!ocrResponse.isSuccess() || ocrResponse.getData() == null) {
            throw new IllegalArgumentException("OCR impossible: " + ocrResponse.getError());
        }

        OcrResultData ocrData = ocrResponse.getData();

        FormatBilan format = detectFormat(ocrData.getFormat());
        LocalDate dateBilan = parseDate(ocrData.getDateBilan());

        Bilan bilan = Bilan.builder()
                .patientId(patientId)
                .typeBilan(ocrData.getTypeBilan())
                .format(format)
                .dateBilan(dateBilan)
                .laboratoire(ocrData.getLaboratoire())
                .imagePath(imagePath)
                .status(BilanStatus.PENDING)
                .build();

        List<BilanResult> resultats = mapResults(ocrData.getResultats(), bilan);
        bilan.setResultats(resultats);

        bilan = bilanRepository.save(bilan);

        return toScanResponse(bilan);
    }

    public Page<BilanSummary> getPatientBilans(Long patientId, int page, int size) {
        Page<Bilan> bilans = bilanRepository.findByPatientIdOrderByCreatedAtDesc(patientId, PageRequest.of(page, size));
        return bilans.map(this::toSummary);
    }

    public ScanBilanResponse getBilan(UUID id, Long patientId) {
        Bilan bilan = findBilanForPatient(id, patientId);
        return toScanResponse(bilan);
    }

    @Transactional
    public ScanBilanResponse confirmBilan(UUID id, Long patientId) {
        Bilan bilan = findBilanForPatient(id, patientId);
        bilan.setStatus(BilanStatus.CONFIRMED);
        bilan = bilanRepository.save(bilan);
        return toScanResponse(bilan);
    }

    @Transactional
    public ScanBilanResponse updateResult(UUID bilanId, Long resultId, UpdateResultRequest request, Long patientId) {
        Bilan bilan = findBilanForPatient(bilanId, patientId);
        BilanResult result = bilanResultRepository.findById(resultId)
                .orElseThrow(() -> new EntityNotFoundException("Result not found: " + resultId));

        if (!result.getBilan().getId().equals(bilanId)) {
            throw new IllegalArgumentException("Result does not belong to this bilan");
        }

        if (request.getValeur() != null) {
            result.setValeur(request.getValeur());
        }
        if (request.getValeurAncienne() != null) {
            result.setValeurAncienne(request.getValeurAncienne());
        }
        if (request.getReferenceMin() != null) {
            result.setReferenceMin(request.getReferenceMin());
        }
        if (request.getReferenceMax() != null) {
            result.setReferenceMax(request.getReferenceMax());
        }
        if (request.getReferenceText() != null) {
            result.setReferenceText(request.getReferenceText());
        }

        result.setStatus(determineResultStatus(result));
        bilanResultRepository.save(result);

        return toScanResponse(bilan);
    }

    public Page<BilanSummary> getDoctorBilans(Long doctorId, int page, int size) {
        Page<Bilan> bilans = bilanRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId, PageRequest.of(page, size));
        return bilans.map(this::toSummary);
    }

    public ScanBilanResponse getBilanForDoctor(UUID id, Long doctorId) {
        Bilan bilan = findBilanForDoctor(id, doctorId);
        return toScanResponse(bilan);
    }

    @Transactional
    public ScanBilanResponse updateReviewStatus(UUID id, Long doctorId, String reviewStatus) {
        Bilan bilan = findBilanForDoctor(id, doctorId);
        try {
            bilan.setReviewStatus(ReviewStatus.valueOf(reviewStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide. Utilisez: EN_ATTENTE, LU, TRAITE");
        }
        bilan = bilanRepository.save(bilan);
        return toScanResponse(bilan);
    }

    @Transactional
    public ScanBilanResponse assignDoctor(UUID id, Long patientId, Long doctorId) {
        Bilan bilan = findBilanForPatient(id, patientId);
        bilan.setDoctorId(doctorId);
        bilan.setReviewStatus(ReviewStatus.EN_ATTENTE);
        bilan = bilanRepository.save(bilan);
        return toScanResponse(bilan);
    }

    @Transactional
    public void deleteBilan(UUID id, Long patientId) {
        Bilan bilan = findBilanForPatient(id, patientId);
        imageStorageService.deleteImage(bilan.getImagePath());
        bilanRepository.delete(bilan);
    }

    private Bilan findBilanForPatient(UUID id, Long patientId) {
        Bilan bilan = bilanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bilan not found: " + id));
        if (!bilan.getPatientId().equals(patientId)) {
            throw new SecurityException("Access denied to bilan: " + id);
        }
        return bilan;
    }

    private Bilan findBilanForDoctor(UUID id, Long doctorId) {
        Bilan bilan = bilanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bilan not found: " + id));
        if (!doctorId.equals(bilan.getDoctorId())) {
            throw new SecurityException("Access denied to bilan: " + id);
        }
        return bilan;
    }

    private FormatBilan detectFormat(String format) {
        if (format == null) return FormatBilan.NOUVEAU_PATIENT;
        return format.toLowerCase().contains("ancien")
                ? FormatBilan.ANCIEN_PATIENT
                : FormatBilan.NOUVEAU_PATIENT;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateStr);
            return null;
        }
    }

    private List<BilanResult> mapResults(List<OcrResultItem> items, Bilan bilan) {
        if (items == null) return List.of();
        return items.stream().map(item -> {
            Double refMin = item.getReferenceMin();
            Double refMax = item.getReferenceMax();
            LocalDate dateAncienne = null;
            if (item.getDateAncienne() != null && !item.getDateAncienne().isBlank()) {
                try {
                    dateAncienne = LocalDate.parse(item.getDateAncienne(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    log.warn("Could not parse ancienne date: {}", item.getDateAncienne());
                }
            }

            String valeur = item.getValeur();
            if (valeur != null) {
                valeur = valeur.replaceAll("\\s+", "");
            }

            BilanResult result = BilanResult.builder()
                    .bilan(bilan)
                    .testName(item.getTest())
                    .valeur(valeur)
                    .unite(item.getUnite())
                    .referenceMin(refMin)
                    .referenceMax(refMax)
                    .referenceText(item.getReferenceText())
                    .valeurAncienne(item.getValeurAncienne())
                    .dateAncienne(dateAncienne)
                    .confiance(item.getConfiance())
                    .build();

            result.setStatus(determineResultStatus(result));
            return result;
        }).toList();
    }

    private ResultStatus determineResultStatus(BilanResult result) {
        if (result.getValeur() == null || result.getValeur().isBlank()) {
            return ResultStatus.NON_APPLICABLE;
        }

        String valeur = result.getValeur().trim();

        // 1. Try range comparison (referenceMin / referenceMax)
        if (result.getReferenceMin() != null && result.getReferenceMax() != null) {
            return compareWithRange(valeur, result.getReferenceMin(), result.getReferenceMax());
        }

        // 2. Try threshold comparison from referenceText ("> 10", "< 5", etc.)
        if (result.getReferenceText() != null) {
            return compareWithReferenceText(valeur, result.getReferenceText());
        }

        return ResultStatus.NON_APPLICABLE;
    }

    private ResultStatus compareWithRange(String valeur, double refMin, double refMax) {
        Double parsed = parseNumber(valeur);
        if (parsed == null) return ResultStatus.NON_APPLICABLE;
        double v = parsed;
        if (v < refMin * 0.7 || v > refMax * 1.3) return ResultStatus.CRITIQUE;
        if (v < refMin || v > refMax) return ResultStatus.ANORMAL;
        return ResultStatus.NORMAL;
    }

    private ResultStatus compareWithReferenceText(String valeur, String referenceText) {
        String ref = referenceText.trim();

        // Threshold operators: > X, >= X, < X, <= X, ≥ X, ≤ X
        Pattern threshPattern = Pattern.compile("^([<>]=?|≥|≤)\\s*(\\d+[.,]?\\d*)$");
        Matcher refMatcher = threshPattern.matcher(ref);
        if (refMatcher.find()) {
            String operator = refMatcher.group(1);
            double threshold = Double.parseDouble(refMatcher.group(2).replace(",", "."));
            Double parsed = parseNumber(valeur);
            if (parsed == null) return ResultStatus.NON_APPLICABLE;
            double v = parsed;

            boolean abnormal;
            switch (operator) {
                case ">": abnormal = v <= threshold; break;
                case ">=":
                case "≥": abnormal = v < threshold; break;
                case "<": abnormal = v >= threshold; break;
                case "<=":
                case "≤": abnormal = v > threshold; break;
                default: return ResultStatus.NON_APPLICABLE;
            }
            if (abnormal) return ResultStatus.ANORMAL;
            return ResultStatus.NORMAL;
        }

        // Text reference (e.g., "Négatif", "Positif")
        String vLower = valeur.toLowerCase();
        String rLower = ref.toLowerCase();
        if (rLower.contains("négatif") || rLower.contains("negatif")) {
            if (vLower.contains("positif") || vLower.contains("pos")) return ResultStatus.ANORMAL;
            if (vLower.contains("négatif") || vLower.contains("negatif") || vLower.contains("neg")) return ResultStatus.NORMAL;
        }
        if (rLower.contains("positif")) {
            if (vLower.contains("négatif") || vLower.contains("negatif")) return ResultStatus.ANORMAL;
            if (vLower.contains("positif") || vLower.contains("pos")) return ResultStatus.NORMAL;
        }

        // Range as text "X - Y" (shouldn't happen if min/max are set, but handle anyway)
        Pattern rangePattern = Pattern.compile("(\\d+[.,]?\\d*)\\s*-\\s*(\\d+[.,]?\\d*)");
        Matcher rangeMatcher = rangePattern.matcher(ref);
        if (rangeMatcher.find()) {
            double min = Double.parseDouble(rangeMatcher.group(1).replace(",", "."));
            double max = Double.parseDouble(rangeMatcher.group(2).replace(",", "."));
            return compareWithRange(valeur, min, max);
        }

        return ResultStatus.NON_APPLICABLE;
    }

    private Double parseNumber(String valeur) {
        Pattern valPattern = Pattern.compile("^([<>]=?|≥|≤)?\\s*(\\d+[.,]?\\d*)$");
        Matcher matcher = valPattern.matcher(valeur.trim());
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(2).replace(",", "."));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private ScanBilanResponse toScanResponse(Bilan bilan) {
        List<BilanResultDto> resultDtos = bilan.getResultats().stream()
                .map(r -> BilanResultDto.builder()
                        .id(r.getId())
                        .testName(r.getTestName())
                        .valeur(r.getValeur())
                        .unite(r.getUnite())
                        .referenceMin(r.getReferenceMin())
                        .referenceMax(r.getReferenceMax())
                        .referenceText(r.getReferenceText())
                        .valeurAncienne(r.getValeurAncienne())
                        .dateAncienne(r.getDateAncienne())
                        .status(r.getStatus() != null ? r.getStatus().name() : null)
                        .confiance(r.getConfiance())
                        .build())
                .toList();

        return ScanBilanResponse.builder()
                .id(bilan.getId())
                .typeBilan(bilan.getTypeBilan())
                .format(bilan.getFormat() != null ? bilan.getFormat().name() : null)
                .dateBilan(bilan.getDateBilan())
                .laboratoire(bilan.getLaboratoire())
                .status(bilan.getStatus().name())
                .reviewStatus(bilan.getReviewStatus() != null ? bilan.getReviewStatus().name() : null)
                .patientId(bilan.getPatientId())
                .doctorId(bilan.getDoctorId())
                .resultats(resultDtos)
                .build();
    }

    private BilanSummary toSummary(Bilan bilan) {
        long abnormalCount = bilan.getResultats().stream()
                .filter(r -> r.getStatus() == ResultStatus.ANORMAL || r.getStatus() == ResultStatus.CRITIQUE)
                .count();

        return BilanSummary.builder()
                .id(bilan.getId())
                .typeBilan(bilan.getTypeBilan())
                .dateBilan(bilan.getDateBilan())
                .status(bilan.getStatus().name())
                .resultCount(bilan.getResultats().size())
                .abnormalCount((int) abnormalCount)
                .reviewStatus(bilan.getReviewStatus() != null ? bilan.getReviewStatus().name() : null)
                .patientId(bilan.getPatientId())
                .doctorId(bilan.getDoctorId())
                .build();
    }
}
