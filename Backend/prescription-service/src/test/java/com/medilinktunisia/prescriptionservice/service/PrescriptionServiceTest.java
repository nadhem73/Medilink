package com.medilinktunisia.prescriptionservice.service;

import com.medilinktunisia.prescriptionservice.client.DoctorServiceClient;
import com.medilinktunisia.prescriptionservice.client.PharmacyServiceClient;
import com.medilinktunisia.prescriptionservice.dto.*;
import com.medilinktunisia.prescriptionservice.model.entity.Prescription;
import com.medilinktunisia.prescriptionservice.model.entity.PrescriptionItem;
import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import com.medilinktunisia.prescriptionservice.repository.PrescriptionItemRepository;
import com.medilinktunisia.prescriptionservice.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PrescriptionItemRepository prescriptionItemRepository;

    @Mock
    private PharmacyServiceClient pharmacyClient;

    @Mock
    private DoctorServiceClient doctorClient;

    @InjectMocks
    private PrescriptionService prescriptionService;

    @Captor
    private ArgumentCaptor<Prescription> prescriptionCaptor;

    private final Long doctorId = 1L;
    private final Long patientId = 10L;
    private final Long consultationId = 100L;

    private PrescriptionCreateRequest createRequest() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicamentId(1L);
        item.setMedicamentName("DOLIPRANE");
        item.setDosage("500mg");
        item.setForme("Comprimé");
        item.setPosologie("1 comprimé 3 fois par jour");
        item.setDureeTraitement(7);
        item.setVoieAdministration("Orale");
        item.setInstructions("Après le repas");

        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setConsultationId(consultationId);
        request.setPatientId(patientId);
        request.setNotes("Prescription de test");
        request.setItems(List.of(item));
        return request;
    }

    private Prescription createPrescriptionEntity(Long id, PrescriptionStatus status) {
        Prescription p = new Prescription();
        p.setId(id);
        p.setConsultationId(consultationId);
        p.setPatientId(patientId);
        p.setDoctorId(doctorId);
        p.setStatus(status);
        p.setNotes("Prescription de test");

        PrescriptionItem item = new PrescriptionItem();
        item.setId(1L);
        item.setPrescription(p);
        item.setMedicamentId(1L);
        item.setMedicamentName("DOLIPRANE");
        item.setDosage("500mg");
        item.setPosologie("1 comprimé 3 fois par jour");
        item.setDureeTraitement(7);
        item.setVoieAdministration("Orale");
        item.setInstructions("Après le repas");
        p.setItems(new java.util.ArrayList<>(List.of(item)));

        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return p;
    }

    @Test
    void createPrescription_success() {
        PrescriptionCreateRequest request = createRequest();
        when(pharmacyClient.checkStock(anyList())).thenReturn(Map.of(1L, 100));
        when(prescriptionRepository.save(any())).thenAnswer(invocation -> {
            Prescription p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });
        doNothing().when(doctorClient).linkPrescriptionToConsultation(anyLong(), anyLong());

        PrescriptionResponse result = prescriptionService.createPrescription(doctorId, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("SOUMISE");
        assertThat(result.getConsultationId()).isEqualTo(consultationId);
        assertThat(result.getPatientId()).isEqualTo(patientId);
        assertThat(result.getDoctorId()).isEqualTo(doctorId);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getMedicamentName()).isEqualTo("DOLIPRANE");
        verify(prescriptionRepository, times(2)).save(any());
        verify(doctorClient).linkPrescriptionToConsultation(consultationId, 1L);
    }

    @Test
    void createPrescription_insufficientStock_throwsException() {
        PrescriptionCreateRequest request = createRequest();
        when(pharmacyClient.checkStock(anyList())).thenReturn(Map.of(1L, 0));

        assertThatThrownBy(() -> prescriptionService.createPrescription(doctorId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuffisant pour");
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void createPrescription_feignException_doesNotRollback() {
        PrescriptionCreateRequest request = createRequest();
        when(pharmacyClient.checkStock(anyList())).thenReturn(Map.of(1L, 100));
        when(prescriptionRepository.save(any())).thenAnswer(invocation -> {
            Prescription p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });
        doThrow(new RuntimeException("Feign error"))
                .when(doctorClient).linkPrescriptionToConsultation(anyLong(), anyLong());

        PrescriptionResponse result = prescriptionService.createPrescription(doctorId, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("SOUMISE");
        verify(prescriptionRepository, times(2)).save(any());
    }

    @Test
    void getPrescription_found_returnsDto() {
        Prescription p = createPrescriptionEntity(1L, PrescriptionStatus.SOUMISE);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));

        PrescriptionResponse result = prescriptionService.getPrescription(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("SOUMISE");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void getPrescription_notFound_throwsException() {
        when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.getPrescription(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Prescription not found");
    }

    @Test
    void getPrescriptionByConsultation_found_returnsDto() {
        Prescription p = createPrescriptionEntity(1L, PrescriptionStatus.SOUMISE);
        when(prescriptionRepository.findByConsultationId(consultationId)).thenReturn(Optional.of(p));

        java.util.Optional<PrescriptionResponse> result = prescriptionService.getPrescriptionByConsultation(consultationId);

        assertThat(result).isPresent();
        assertThat(result.get().getConsultationId()).isEqualTo(consultationId);
    }

    @Test
    void getPrescriptionByConsultation_notFound_returnsEmpty() {
        when(prescriptionRepository.findByConsultationId(99L)).thenReturn(Optional.empty());

        java.util.Optional<PrescriptionResponse> result = prescriptionService.getPrescriptionByConsultation(99L);
        assertThat(result).isEmpty();
    }

    @Test
    void getPrescriptionsByPatient_returnsList() {
        Prescription p1 = createPrescriptionEntity(1L, PrescriptionStatus.SOUMISE);
        Prescription p2 = createPrescriptionEntity(2L, PrescriptionStatus.DISPENSEE);
        when(prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId))
                .thenReturn(List.of(p1, p2));

        List<PrescriptionResponse> results = prescriptionService.getPrescriptionsByPatient(patientId);

        assertThat(results).hasSize(2);
    }

    @Test
    void getPrescriptionsByPatient_empty_returnsEmptyList() {
        when(prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(99L))
                .thenReturn(List.of());

        List<PrescriptionResponse> results = prescriptionService.getPrescriptionsByPatient(99L);

        assertThat(results).isEmpty();
    }

    @Test
    void updatePrescription_success() {
        Prescription existing = createPrescriptionEntity(1L, PrescriptionStatus.SOUMISE);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(prescriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PrescriptionCreateRequest request = createRequest();
        request.setNotes("Updated notes");

        PrescriptionResponse result = prescriptionService.updatePrescription(1L, doctorId, request);

        assertThat(result.getNotes()).isEqualTo("Updated notes");
        verify(prescriptionRepository).save(any());
    }

    @Test
    void updatePrescription_unauthorized_throwsException() {
        Prescription existing = createPrescriptionEntity(1L, PrescriptionStatus.SOUMISE);
        existing.setDoctorId(99L);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> prescriptionService.updatePrescription(1L, doctorId, createRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void updatePrescription_dispensed_throwsException() {
        Prescription existing = createPrescriptionEntity(1L, PrescriptionStatus.DISPENSEE);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> prescriptionService.updatePrescription(1L, doctorId, createRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot modify");
    }

    @Test
    void updatePrescription_cancelled_throwsException() {
        Prescription existing = createPrescriptionEntity(1L, PrescriptionStatus.ANNULEE);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> prescriptionService.updatePrescription(1L, doctorId, createRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot modify");
    }

    @Test
    void cancelPrescription_success() {
        Prescription existing = createPrescriptionEntity(1L, PrescriptionStatus.SOUMISE);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(prescriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        prescriptionService.cancelPrescription(1L, doctorId);

        verify(prescriptionRepository).save(prescriptionCaptor.capture());
        assertThat(prescriptionCaptor.getValue().getStatus()).isEqualTo(PrescriptionStatus.ANNULEE);
    }

    @Test
    void cancelPrescription_unauthorized_throwsException() {
        Prescription existing = createPrescriptionEntity(1L, PrescriptionStatus.SOUMISE);
        existing.setDoctorId(99L);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> prescriptionService.cancelPrescription(1L, doctorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void cancelPrescription_dispensed_throwsException() {
        Prescription existing = createPrescriptionEntity(1L, PrescriptionStatus.DISPENSEE);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> prescriptionService.cancelPrescription(1L, doctorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot cancel a dispensed prescription");
    }

    @Test
    void cancelPrescription_notFound_throwsException() {
        when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.cancelPrescription(99L, doctorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Prescription not found");
    }
}
