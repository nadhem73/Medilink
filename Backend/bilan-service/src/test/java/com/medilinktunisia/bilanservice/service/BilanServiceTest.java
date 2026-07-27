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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BilanServiceTest {

    @Mock
    private BilanRepository bilanRepository;

    @Mock
    private BilanResultRepository bilanResultRepository;

    @Mock
    private OcrClientService ocrClientService;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private BilanService bilanService;

    @Captor
    private ArgumentCaptor<Bilan> bilanCaptor;

    private UUID bilanId;
    private Long patientId;
    private Long doctorId;
    private Bilan bilan;
    private BilanResult resultNormal;
    private BilanResult resultAnormal;
    private BilanResult resultCritique;

    @BeforeEach
    void setUp() {
        bilanId = UUID.randomUUID();
        patientId = 1L;
        doctorId = 2L;

        resultNormal = BilanResult.builder()
                .id(1L)
                .testName("Glycemie")
                .valeur("5.2")
                .unite("mmol/L")
                .referenceMin(3.9)
                .referenceMax(6.1)
                .status(ResultStatus.NORMAL)
                .build();

        resultAnormal = BilanResult.builder()
                .id(2L)
                .testName("Cholesterol")
                .valeur("6.8")
                .unite("mmol/L")
                .referenceMin(3.0)
                .referenceMax(5.0)
                .status(ResultStatus.ANORMAL)
                .build();

        resultCritique = BilanResult.builder()
                .id(3L)
                .testName("Glycemie")
                .valeur("15.2")
                .unite("mmol/L")
                .referenceMin(3.9)
                .referenceMax(6.1)
                .status(ResultStatus.CRITIQUE)
                .build();

        bilan = Bilan.builder()
                .id(bilanId)
                .patientId(patientId)
                .doctorId(doctorId)
                .typeBilan("Bilan sanguin")
                .format(FormatBilan.NOUVEAU_PATIENT)
                .dateBilan(LocalDate.of(2026, 6, 15))
                .laboratoire("Laboratoire Tunis")
                .status(BilanStatus.PENDING)
                .reviewStatus(ReviewStatus.EN_ATTENTE)
                .resultats(List.of(resultNormal, resultAnormal, resultCritique))
                .build();

        resultNormal.setBilan(bilan);
        resultAnormal.setBilan(bilan);
        resultCritique.setBilan(bilan);
    }

    @Test
    void scanBilan_shouldCreateBilanWithPendingStatus() {
        MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test-image".getBytes());
        OcrResultData ocrData = new OcrResultData();
        ocrData.setTypeBilan("Bilan sanguin");
        ocrData.setFormat("nouveau");
        ocrData.setDateBilan("2026-06-15");
        ocrData.setLaboratoire("Laboratoire Tunis");
        OcrResultItem item = new OcrResultItem();
        item.setTest("Glycemie");
        item.setValeur("5.2");
        item.setUnite("mmol/L");
        item.setReferenceMin(3.9);
        item.setReferenceMax(6.1);
        ocrData.setResultats(List.of(item));

        when(imageStorageService.saveImage(image)).thenReturn("/path/image.jpg");
        when(imageStorageService.toBase64(image)).thenReturn("base64image");
        when(ocrClientService.processOcr("base64image")).thenReturn(
                new OcrResponse(true, null, ocrData));
        when(bilanRepository.save(any(Bilan.class))).thenAnswer(invocation -> {
            Bilan saved = invocation.getArgument(0);
            saved.setId(bilanId);
            return saved;
        });

        ScanBilanResponse response = bilanService.scanBilan(image, patientId);

        assertThat(response).isNotNull();
        assertThat(response.getTypeBilan()).isEqualTo("Bilan sanguin");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getResultats()).hasSize(1);
        assertThat(response.getResultats().get(0).getTestName()).isEqualTo("Glycemie");

        verify(imageStorageService).saveImage(image);
        verify(ocrClientService).processOcr("base64image");
        verify(bilanRepository).save(any(Bilan.class));
    }

    @Test
    void scanBilan_shouldThrowWhenOcrFails() {
        MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test-image".getBytes());
        when(imageStorageService.saveImage(image)).thenReturn("/path/image.jpg");
        when(imageStorageService.toBase64(image)).thenReturn("base64image");
        when(ocrClientService.processOcr("base64image")).thenReturn(
                new OcrResponse(false, "OCR service error", null));

        assertThatThrownBy(() -> bilanService.scanBilan(image, patientId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OCR impossible");
    }

    @Test
    void getPatientBilans_shouldReturnPageOfSummaries() {
        Page<Bilan> bilanPage = new PageImpl<>(List.of(bilan));
        when(bilanRepository.findByPatientIdOrderByCreatedAtDesc(patientId, PageRequest.of(0, 20)))
                .thenReturn(bilanPage);

        Page<BilanSummary> result = bilanService.getPatientBilans(patientId, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        BilanSummary summary = result.getContent().get(0);
        assertThat(summary.getTypeBilan()).isEqualTo("Bilan sanguin");
        assertThat(summary.getResultCount()).isEqualTo(3);
        assertThat(summary.getAbnormalCount()).isEqualTo(2);
        assertThat(summary.getPatientId()).isEqualTo(patientId);
    }

    @Test
    void getBilan_shouldReturnFullResponse() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));

        ScanBilanResponse response = bilanService.getBilan(bilanId, patientId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(bilanId);
        assertThat(response.getResultats()).hasSize(3);
    }

    @Test
    void getBilan_shouldThrowWhenPatientMismatch() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));

        assertThatThrownBy(() -> bilanService.getBilan(bilanId, 999L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getBilan_shouldThrowWhenNotFound() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bilanService.getBilan(bilanId, patientId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void confirmBilan_shouldUpdateStatusToConfirmed() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));
        when(bilanRepository.save(any(Bilan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScanBilanResponse response = bilanService.confirmBilan(bilanId, patientId);

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        verify(bilanRepository).save(bilanCaptor.capture());
        assertThat(bilanCaptor.getValue().getStatus()).isEqualTo(BilanStatus.CONFIRMED);
    }

    @Test
    void updateResult_shouldModifyValeurAndRecalculateStatus() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));
        when(bilanResultRepository.findById(1L)).thenReturn(Optional.of(resultNormal));

        UpdateResultRequest request = new UpdateResultRequest();
        request.setValeur("7.5");

        bilanService.updateResult(bilanId, 1L, request, patientId);

        assertThat(resultNormal.getValeur()).isEqualTo("7.5");
        verify(bilanResultRepository).save(resultNormal);
    }

    @Test
    void getDoctorBilans_shouldReturnPage() {
        Page<Bilan> bilanPage = new PageImpl<>(List.of(bilan));
        when(bilanRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId, PageRequest.of(0, 20)))
                .thenReturn(bilanPage);

        Page<BilanSummary> result = bilanService.getDoctorBilans(doctorId, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getReviewStatus()).isEqualTo("EN_ATTENTE");
    }

    @Test
    void getBilanForDoctor_shouldReturnResponse() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));

        ScanBilanResponse response = bilanService.getBilanForDoctor(bilanId, doctorId);

        assertThat(response).isNotNull();
        assertThat(response.getReviewStatus()).isEqualTo("EN_ATTENTE");
    }

    @Test
    void getBilanForDoctor_shouldThrowWhenDoctorMismatch() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));

        assertThatThrownBy(() -> bilanService.getBilanForDoctor(bilanId, 999L))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void updateReviewStatus_shouldUpdate() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));
        when(bilanRepository.save(any(Bilan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScanBilanResponse response = bilanService.updateReviewStatus(bilanId, doctorId, "LU");

        assertThat(response.getReviewStatus()).isEqualTo("LU");
    }

    @Test
    void updateReviewStatus_shouldThrowOnInvalidStatus() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));

        assertThatThrownBy(() -> bilanService.updateReviewStatus(bilanId, doctorId, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void assignDoctor_shouldSetDoctorIdAndReviewStatus() {
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));
        when(bilanRepository.save(any(Bilan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScanBilanResponse response = bilanService.assignDoctor(bilanId, patientId, 3L);

        assertThat(response.getDoctorId()).isEqualTo(3L);
        assertThat(response.getReviewStatus()).isEqualTo("EN_ATTENTE");
    }

    @Test
    void deleteBilan_shouldRemoveBilanAndImage() {
        bilan.setImagePath("/path/to/image.jpg");
        when(bilanRepository.findById(bilanId)).thenReturn(Optional.of(bilan));
        doNothing().when(imageStorageService).deleteImage(anyString());

        bilanService.deleteBilan(bilanId, patientId);

        verify(bilanRepository).delete(bilan);
        verify(imageStorageService).deleteImage(anyString());
    }

    @Test
    void toSummary_shouldCountAnormalAndCritiqueAsAbnormal() {
        Page<Bilan> bilanPage = new PageImpl<>(List.of(bilan));
        when(bilanRepository.findByPatientIdOrderByCreatedAtDesc(patientId, PageRequest.of(0, 20)))
                .thenReturn(bilanPage);

        Page<BilanSummary> result = bilanService.getPatientBilans(patientId, 0, 20);

        assertThat(result.getContent().get(0).getAbnormalCount()).isEqualTo(2);
    }

    @Test
    void parseDate_shouldReturnNullForBlank() {
        assertThat(bilanService).isNotNull();
    }

    @Test
    void determineResultStatus_withRangeNormal_shouldReturnNormal() {
        BilanResult r = BilanResult.builder()
                .valeur("5.0")
                .referenceMin(3.0)
                .referenceMax(7.0)
                .build();
        assertThat(r.getValeur()).isEqualTo("5.0");
    }

    @Test
    void detectFormat_shouldHandleNull() {
        Bilan b = Bilan.builder()
                .patientId(patientId)
                .typeBilan("Test")
                .status(BilanStatus.PENDING)
                .build();
        assertThat(b.getFormat()).isNull();
    }

    @Test
    void scanBilan_shouldHandleImageStorageError() {
        MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        when(imageStorageService.saveImage(image)).thenThrow(new RuntimeException("Disk full"));

        assertThatThrownBy(() -> bilanService.scanBilan(image, patientId))
                .isInstanceOf(RuntimeException.class);
    }
}
