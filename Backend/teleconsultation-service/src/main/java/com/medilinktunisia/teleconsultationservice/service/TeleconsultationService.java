package com.medilinktunisia.teleconsultationservice.service;

import com.medilinktunisia.teleconsultationservice.exception.TeleconsultationNotFoundException;
import com.medilinktunisia.teleconsultationservice.exception.UnauthorizedAccessException;
import com.medilinktunisia.teleconsultationservice.model.dto.*;
import com.medilinktunisia.teleconsultationservice.model.entity.Teleconsultation;
import com.medilinktunisia.teleconsultationservice.model.entity.ConsultationParticipant;
import com.medilinktunisia.teleconsultationservice.model.enums.ConsultationStatus;
import com.medilinktunisia.teleconsultationservice.model.enums.ParticipantRole;
import com.medilinktunisia.teleconsultationservice.repository.ConsultationParticipantRepository;
import com.medilinktunisia.teleconsultationservice.repository.TeleconsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeleconsultationService {

    private final TeleconsultationRepository teleconsultationRepository;
    private final ConsultationParticipantRepository participantRepository;

    @Transactional
    public ConsultationDto createConsultation(ConsultationCreateRequest request) {
        log.info("Creating teleconsultation between patient {} and doctor {}", 
                request.getPatientId(), request.getDoctorId());

        Teleconsultation teleconsultation = Teleconsultation.builder()
                .sessionId(UUID.randomUUID().toString())
                .consultationCode("TC" + System.currentTimeMillis())
                .patientId(request.getPatientId())
                .patientName(request.getPatientName())
                .doctorId(request.getDoctorId())
                .doctorName(request.getDoctorName())
                .doctorSpecialty(request.getDoctorSpecialty())
                .scheduledTime(request.getScheduledTime())
                .reason(request.getReason())
                .status(ConsultationStatus.SCHEDULED)
                .build();

        // Add participants
        ConsultationParticipant patientParticipant = ConsultationParticipant.builder()
                .teleconsultation(teleconsultation)
                .userId(request.getPatientId())
                .userName(request.getPatientName())
                .role(ParticipantRole.PATIENT)
                .build();

        ConsultationParticipant doctorParticipant = ConsultationParticipant.builder()
                .teleconsultation(teleconsultation)
                .userId(request.getDoctorId())
                .userName(request.getDoctorName())
                .role(ParticipantRole.DOCTOR)
                .build();

        teleconsultation.addParticipant(patientParticipant);
        teleconsultation.addParticipant(doctorParticipant);

        Teleconsultation savedConsultation = teleconsultationRepository.save(teleconsultation);
        log.info("Teleconsultation created with session ID: {}", savedConsultation.getSessionId());

        return mapToDto(savedConsultation);
    }

    @Transactional
    public ConsultationDto startConsultation(String sessionId, Long userId) {
        log.info("Starting consultation {} by user {}", sessionId, userId);

        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        if (!isParticipant(teleconsultation, userId)) {
            throw new UnauthorizedAccessException("User is not a participant of this consultation");
        }

        if (teleconsultation.getStatus() != ConsultationStatus.SCHEDULED) {
            throw new IllegalStateException("Consultation cannot be started in current status: " + teleconsultation.getStatus());
        }

        teleconsultation.setStatus(ConsultationStatus.IN_PROGRESS);
        teleconsultation.setStartTime(LocalDateTime.now());

        Teleconsultation savedConsultation = teleconsultationRepository.save(teleconsultation);
        log.info("Consultation started: {}", sessionId);

        return mapToDto(savedConsultation);
    }

    @Transactional
    public ConsultationDto endConsultation(String sessionId, Long userId, String notes) {
        log.info("Ending consultation {} by user {}", sessionId, userId);

        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        if (!isParticipant(teleconsultation, userId)) {
            throw new UnauthorizedAccessException("User is not a participant of this consultation");
        }

        if (teleconsultation.getStatus() != ConsultationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Consultation is not in progress");
        }

        teleconsultation.setStatus(ConsultationStatus.COMPLETED);
        teleconsultation.setEndTime(LocalDateTime.now());
        teleconsultation.setNotes(notes);

        // Calculate duration
        if (teleconsultation.getStartTime() != null && teleconsultation.getEndTime() != null) {
            long minutes = java.time.Duration.between(
                    teleconsultation.getStartTime(), 
                    teleconsultation.getEndTime()
            ).toMinutes();
            teleconsultation.setDurationMinutes((int) minutes);
        }

        Teleconsultation savedConsultation = teleconsultationRepository.save(teleconsultation);
        log.info("Consultation ended: {}", sessionId);

        return mapToDto(savedConsultation);
    }

    @Transactional
    public void cancelConsultation(String sessionId, Long userId, String reason) {
        log.info("Cancelling consultation {} by user {}", sessionId, userId);

        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        if (!isParticipant(teleconsultation, userId)) {
            throw new UnauthorizedAccessException("User is not a participant of this consultation");
        }

        if (teleconsultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed consultation");
        }

        teleconsultation.setStatus(ConsultationStatus.CANCELLED);
        teleconsultation.setCancellationReason(reason);
        teleconsultationRepository.save(teleconsultation);

        log.info("Consultation cancelled: {}", sessionId);
    }

    @Transactional(readOnly = true)
    public ConsultationDto getConsultationById(Long id) {
        Teleconsultation teleconsultation = teleconsultationRepository.findById(id)
                .orElseThrow(() -> new TeleconsultationNotFoundException(id));
        return mapToDto(teleconsultation);
    }

    @Transactional(readOnly = true)
    public ConsultationDto getConsultationBySessionId(String sessionId) {
        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));
        return mapToDto(teleconsultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getPatientConsultations(Long patientId) {
        List<Teleconsultation> consultations = teleconsultationRepository.findByPatientIdOrderByScheduledTimeDesc(patientId);
        return consultations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getDoctorConsultations(Long doctorId) {
        List<Teleconsultation> consultations = teleconsultationRepository.findByDoctorIdOrderByScheduledTimeDesc(doctorId);
        return consultations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getUpcomingConsultations(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Teleconsultation> consultations = teleconsultationRepository.findUpcomingConsultationsForUser(userId, now);
        return consultations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void joinSession(String sessionId, Long userId) {
        log.info("User {} joining session {}", userId, sessionId);

        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        if (!isParticipant(teleconsultation, userId)) {
            throw new UnauthorizedAccessException("User is not a participant of this consultation");
        }

        // Update participant join time
        teleconsultation.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .ifPresent(p -> p.setJoinedAt(LocalDateTime.now()));

        teleconsultationRepository.save(teleconsultation);
    }

    @Transactional
    public void leaveSession(String sessionId, Long userId) {
        log.info("User {} leaving session {}", userId, sessionId);

        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        // Update participant leave time
        teleconsultation.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .ifPresent(p -> p.setLeftAt(LocalDateTime.now()));

        teleconsultationRepository.save(teleconsultation);
    }

    private boolean isParticipant(Teleconsultation teleconsultation, Long userId) {
        return teleconsultation.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));
    }

    private ConsultationDto mapToDto(Teleconsultation teleconsultation) {
        List<ParticipantDto> participants = teleconsultation.getParticipants().stream()
                .map(this::mapToParticipantDto)
                .collect(Collectors.toList());

        return ConsultationDto.builder()
                .id(teleconsultation.getId())
                .sessionId(teleconsultation.getSessionId())
                .patientId(teleconsultation.getPatientId())
                .patientName(teleconsultation.getPatientName())
                .doctorId(teleconsultation.getDoctorId())
                .doctorName(teleconsultation.getDoctorName())
                .doctorSpecialty(teleconsultation.getDoctorSpecialty())
                .scheduledTime(teleconsultation.getScheduledTime())
                .startTime(teleconsultation.getStartTime())
                .endTime(teleconsultation.getEndTime())
                .durationMinutes(teleconsultation.getDurationMinutes())
                .reason(teleconsultation.getReason())
                .notes(teleconsultation.getNotes())
                .status(teleconsultation.getStatus())
                .participants(participants)
                .createdAt(teleconsultation.getCreatedAt())
                .updatedAt(teleconsultation.getUpdatedAt())
                .build();
    }

    private ParticipantDto mapToParticipantDto(ConsultationParticipant participant) {
        return ParticipantDto.builder()
                .id(participant.getId())
                .userId(participant.getUserId())
                .userName(participant.getUserName())
                .role(participant.getRole())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .build();
    }
}
