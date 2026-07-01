package com.medilinktunisia.doctorservice.service;

import com.medilinktunisia.doctorservice.dto.ConsultationRequest;
import com.medilinktunisia.doctorservice.dto.ConsultationResponse;
import com.medilinktunisia.doctorservice.model.Consultation;
import com.medilinktunisia.doctorservice.model.ConsultationStatus;
import com.medilinktunisia.doctorservice.model.ConsultationType;
import com.medilinktunisia.doctorservice.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository repository;

    public List<ConsultationResponse> getTodayConsultations(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        return repository.findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(doctorId, start, end)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ConsultationResponse> getConsultationsByStatus(Long doctorId, String status) {
        ConsultationStatus consultationStatus;
        try {
            consultationStatus = ConsultationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        return repository.findByDoctorIdAndStatusOrderByStartTimeDesc(doctorId, consultationStatus)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ConsultationResponse> getAllConsultations(Long doctorId) {
        return repository.findByDoctorIdOrderByStartTimeDesc(doctorId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ConsultationResponse getConsultation(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Consultation not found: " + id));
    }

    public ConsultationResponse startConsultation(Long doctorId, ConsultationRequest request) {
        Consultation consultation = new Consultation();
        consultation.setPatientId(request.getPatientId());
        consultation.setDoctorId(doctorId);
        consultation.setAppointmentId(request.getAppointmentId());
        consultation.setStartTime(LocalDateTime.now());
        consultation.setStatus(ConsultationStatus.IN_PROGRESS);

        ConsultationType type;
        try {
            type = request.getType() != null
                    ? ConsultationType.valueOf(request.getType().toUpperCase())
                    : ConsultationType.PRESENTIEL;
        } catch (IllegalArgumentException e) {
            type = ConsultationType.PRESENTIEL;
        }
        consultation.setType(type);
        consultation.setReason(request.getReason());

        return toDto(repository.save(consultation));
    }

    public ConsultationResponse updateConsultation(Long id, Long doctorId, ConsultationRequest request) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found: " + id));

        if (!consultation.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized: consultation belongs to another doctor");
        }

        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw new RuntimeException("Cannot modify a completed consultation");
        }

        if (request.getReason() != null) consultation.setReason(request.getReason());
        if (request.getDiagnosis() != null) consultation.setDiagnosis(request.getDiagnosis());
        if (request.getObservations() != null) consultation.setObservations(request.getObservations());
        if (request.getBloodPressure() != null) consultation.setBloodPressure(request.getBloodPressure());
        if (request.getPulse() != null) consultation.setPulse(request.getPulse());
        if (request.getTemperature() != null) consultation.setTemperature(request.getTemperature());
        if (request.getWeight() != null) consultation.setWeight(request.getWeight());
        if (request.getHeight() != null) {
            consultation.setHeight(request.getHeight());
            if (request.getWeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal heightMeters = request.getHeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                BigDecimal bmi = request.getWeight().divide(heightMeters.multiply(heightMeters), 1, RoundingMode.HALF_UP);
                consultation.setBmi(bmi);
            }
        }
        if (request.getRequestedExams() != null) consultation.setRequestedExams(request.getRequestedExams());
        if (request.getFollowUpDate() != null) consultation.setFollowUpDate(request.getFollowUpDate());

        return toDto(repository.save(consultation));
    }

    public ConsultationResponse completeConsultation(Long id, Long doctorId, ConsultationRequest request) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found: " + id));

        if (!consultation.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized: consultation belongs to another doctor");
        }

        if (request.getDiagnosis() != null) consultation.setDiagnosis(request.getDiagnosis());
        if (request.getObservations() != null) consultation.setObservations(request.getObservations());
        if (request.getBloodPressure() != null) consultation.setBloodPressure(request.getBloodPressure());
        if (request.getPulse() != null) consultation.setPulse(request.getPulse());
        if (request.getTemperature() != null) consultation.setTemperature(request.getTemperature());
        if (request.getWeight() != null) consultation.setWeight(request.getWeight());
        if (request.getHeight() != null) {
            consultation.setHeight(request.getHeight());
            if (request.getWeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal heightMeters = request.getHeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                BigDecimal bmi = request.getWeight().divide(heightMeters.multiply(heightMeters), 1, RoundingMode.HALF_UP);
                consultation.setBmi(bmi);
            }
        }
        if (request.getRequestedExams() != null) consultation.setRequestedExams(request.getRequestedExams());
        if (request.getFollowUpDate() != null) consultation.setFollowUpDate(request.getFollowUpDate());

        consultation.setStatus(ConsultationStatus.COMPLETED);
        consultation.setEndTime(LocalDateTime.now());

        return toDto(repository.save(consultation));
    }

    public List<ConsultationResponse> getConsultationsByPatient(Long doctorId, Long patientId) {
        return repository.findByDoctorIdAndPatientIdOrderByStartTimeDesc(doctorId, patientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public void cancelConsultation(Long id, Long doctorId) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found: " + id));

        if (!consultation.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized: consultation belongs to another doctor");
        }

        consultation.setStatus(ConsultationStatus.CANCELLED);
        repository.save(consultation);
    }

    public void linkPrescription(Long consultationId, Long prescriptionId) {
        Consultation consultation = repository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found: " + consultationId));
        consultation.setPrescriptionId(prescriptionId);
        repository.save(consultation);
    }

    private ConsultationResponse toDto(Consultation c) {
        return ConsultationResponse.builder()
                .id(c.getId())
                .patientId(c.getPatientId())
                .doctorId(c.getDoctorId())
                .appointmentId(c.getAppointmentId())
                .startTime(c.getStartTime())
                .endTime(c.getEndTime())
                .status(c.getStatus().name())
                .type(c.getType().name())
                .reason(c.getReason())
                .diagnosis(c.getDiagnosis())
                .observations(c.getObservations())
                .bloodPressure(c.getBloodPressure())
                .pulse(c.getPulse())
                .temperature(c.getTemperature())
                .weight(c.getWeight())
                .height(c.getHeight())
                .bmi(c.getBmi())
                .requestedExams(c.getRequestedExams())
                .followUpDate(c.getFollowUpDate())
                .prescriptionId(c.getPrescriptionId())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
