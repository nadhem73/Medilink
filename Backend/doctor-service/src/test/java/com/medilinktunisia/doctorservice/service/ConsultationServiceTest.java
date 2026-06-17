package com.medilinktunisia.doctorservice.service;

import com.medilinktunisia.doctorservice.dto.ConsultationRequest;
import com.medilinktunisia.doctorservice.dto.ConsultationResponse;
import com.medilinktunisia.doctorservice.model.Consultation;
import com.medilinktunisia.doctorservice.model.ConsultationStatus;
import com.medilinktunisia.doctorservice.model.ConsultationType;
import com.medilinktunisia.doctorservice.repository.ConsultationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository repository;

    @InjectMocks
    private ConsultationService service;

    @Captor
    private ArgumentCaptor<Consultation> consultationCaptor;

    private final Long doctorId = 1L;

    private Consultation createConsultation(Long id, ConsultationStatus status) {
        Consultation c = new Consultation();
        c.setId(id);
        c.setPatientId(10L);
        c.setDoctorId(doctorId);
        c.setAppointmentId(100L);
        c.setStartTime(LocalDateTime.now());
        c.setStatus(status);
        c.setType(ConsultationType.PRESENTIEL);
        c.setReason("Headache");
        c.setDiagnosis("Migraine");
        c.setBloodPressure("12/8");
        c.setPulse(72);
        c.setTemperature(new BigDecimal("36.6"));
        c.setWeight(new BigDecimal("70.0"));
        c.setHeight(new BigDecimal("175.0"));
        c.setBmi(new BigDecimal("22.9"));
        return c;
    }

    @Test
    void getTodayConsultations_returnsList() {
        Consultation c = createConsultation(1L, ConsultationStatus.PENDING);
        when(repository.findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(eq(doctorId), any(), any()))
                .thenReturn(List.of(c));

        List<ConsultationResponse> result = service.getTodayConsultations(doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void getConsultationsByStatus_validStatus_returnsFiltered() {
        Consultation c = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        when(repository.findByDoctorIdAndStatusOrderByStartTimeDesc(doctorId, ConsultationStatus.IN_PROGRESS))
                .thenReturn(List.of(c));

        List<ConsultationResponse> result = service.getConsultationsByStatus(doctorId, "IN_PROGRESS");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void getConsultationsByStatus_invalidStatus_returnsEmpty() {
        List<ConsultationResponse> result = service.getConsultationsByStatus(doctorId, "UNKNOWN");

        assertThat(result).isEmpty();
    }

    @Test
    void getAllConsultations_returnsAll() {
        Consultation c1 = createConsultation(1L, ConsultationStatus.PENDING);
        Consultation c2 = createConsultation(2L, ConsultationStatus.COMPLETED);
        when(repository.findByDoctorIdOrderByStartTimeDesc(doctorId)).thenReturn(List.of(c1, c2));

        List<ConsultationResponse> result = service.getAllConsultations(doctorId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getConsultation_found_returnsDto() {
        Consultation c = createConsultation(1L, ConsultationStatus.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        ConsultationResponse result = service.getConsultation(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReason()).isEqualTo("Headache");
        assertThat(result.getDiagnosis()).isEqualTo("Migraine");
    }

    @Test
    void getConsultation_notFound_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConsultation(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Consultation not found");
    }

    @Test
    void startConsultation_savesAndReturnsDto() {
        ConsultationRequest request = new ConsultationRequest();
        request.setPatientId(10L);
        request.setAppointmentId(100L);
        request.setType("PRESENTIEL");
        request.setReason("Follow-up");

        Consultation saved = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        saved.setReason("Follow-up");
        when(repository.save(any())).thenReturn(saved);

        ConsultationResponse result = service.startConsultation(doctorId, request);

        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getReason()).isEqualTo("Follow-up");
        verify(repository).save(any());
    }

    @Test
    void updateConsultation_updatesFields() {
        Consultation existing = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultationRequest request = new ConsultationRequest();
        request.setDiagnosis("Updated diagnosis");
        request.setBloodPressure("13/9");

        ConsultationResponse result = service.updateConsultation(1L, doctorId, request);

        assertThat(result.getDiagnosis()).isEqualTo("Updated diagnosis");
        assertThat(result.getBloodPressure()).isEqualTo("13/9");
    }

    @Test
    void updateConsultation_completed_throwsException() {
        Consultation existing = createConsultation(1L, ConsultationStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        ConsultationRequest request = new ConsultationRequest();

        assertThatThrownBy(() -> service.updateConsultation(1L, doctorId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot modify");
    }

    @Test
    void updateConsultation_wrongDoctor_throwsException() {
        Consultation existing = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        existing.setDoctorId(99L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        ConsultationRequest request = new ConsultationRequest();

        assertThatThrownBy(() -> service.updateConsultation(1L, doctorId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void completeConsultation_completesAndSetsEndTime() {
        Consultation existing = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultationRequest request = new ConsultationRequest();
        request.setDiagnosis("Final diagnosis");
        request.setWeight(new BigDecimal("70.0"));
        request.setHeight(new BigDecimal("175.0"));

        ConsultationResponse result = service.completeConsultation(1L, doctorId, request);

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getBmi()).isEqualTo(new BigDecimal("22.9"));
    }

    @Test
    void completeConsultation_wrongDoctor_throwsException() {
        Consultation existing = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        existing.setDoctorId(99L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        ConsultationRequest request = new ConsultationRequest();

        assertThatThrownBy(() -> service.completeConsultation(1L, 2L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void cancelConsultation_setsStatusCancelled() {
        Consultation existing = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelConsultation(1L, doctorId);

        verify(repository).save(consultationCaptor.capture());
        assertThat(consultationCaptor.getValue().getStatus()).isEqualTo(ConsultationStatus.CANCELLED);
    }

    @Test
    void cancelConsultation_wrongDoctor_throwsException() {
        Consultation existing = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        existing.setDoctorId(99L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.cancelConsultation(1L, doctorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void getTodayConsultations_emptyList_returnsEmpty() {
        when(repository.findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(eq(doctorId), any(), any()))
                .thenReturn(List.of());

        List<ConsultationResponse> result = service.getTodayConsultations(doctorId);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllConsultations_emptyList_returnsEmpty() {
        when(repository.findByDoctorIdOrderByStartTimeDesc(doctorId)).thenReturn(List.of());

        List<ConsultationResponse> result = service.getAllConsultations(doctorId);

        assertThat(result).isEmpty();
    }

    @Test
    void startConsultation_invalidType_fallsBackToPresentiel() {
        ConsultationRequest request = new ConsultationRequest();
        request.setPatientId(10L);
        request.setType("INVALID");

        Consultation saved = createConsultation(1L, ConsultationStatus.IN_PROGRESS);
        saved.setType(ConsultationType.PRESENTIEL);
        when(repository.save(any())).thenReturn(saved);

        ConsultationResponse result = service.startConsultation(doctorId, request);

        assertThat(result.getType()).isEqualTo("PRESENTIEL");
    }
}
