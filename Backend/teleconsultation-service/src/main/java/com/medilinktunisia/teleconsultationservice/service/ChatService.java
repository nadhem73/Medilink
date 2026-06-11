package com.medilinktunisia.teleconsultationservice.service;

import com.medilinktunisia.teleconsultationservice.exception.TeleconsultationNotFoundException;
import com.medilinktunisia.teleconsultationservice.model.dto.ChatMessageDto;
import com.medilinktunisia.teleconsultationservice.model.entity.ConsultationMessage;
import com.medilinktunisia.teleconsultationservice.model.entity.Teleconsultation;
import com.medilinktunisia.teleconsultationservice.model.enums.MessageType;
import com.medilinktunisia.teleconsultationservice.model.enums.ParticipantRole;
import com.medilinktunisia.teleconsultationservice.repository.ConsultationMessageRepository;
import com.medilinktunisia.teleconsultationservice.repository.TeleconsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ConsultationMessageRepository consultationMessageRepository;
    private final TeleconsultationRepository teleconsultationRepository;

    @Transactional
    public ChatMessageDto saveMessage(String sessionId, ChatMessageDto messageDto) {
        log.info("Saving chat message for session: {}", sessionId);

        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        ConsultationMessage message = ConsultationMessage.builder()
                .teleconsultation(teleconsultation)
                .senderId(messageDto.getSenderId())
                .senderName(messageDto.getSenderName())
                .content(messageDto.getContent())
                .messageType(messageDto.getType() != null ? messageDto.getType() : MessageType.TEXT)
                .senderRole(ParticipantRole.PATIENT) // Default, should be determined from context
                .build();

        ConsultationMessage savedMessage = consultationMessageRepository.save(message);
        return mapToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getConsultationMessages(String sessionId) {
        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        List<ConsultationMessage> messages = consultationMessageRepository.findByTeleconsultationOrderBySentAtAsc(teleconsultation);
        return messages.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getConsultationMessagesPaginated(String sessionId, int page, int size) {
        Teleconsultation teleconsultation = teleconsultationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TeleconsultationNotFoundException("Session ID", sessionId));

        List<ConsultationMessage> messages = consultationMessageRepository
                .findByTeleconsultationOrderBySentAtDesc(teleconsultation);

        // Simple pagination
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        
        if (start >= messages.size()) {
            return List.of();
        }

        return messages.subList(start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDto mapToDto(ConsultationMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .consultationId(message.getTeleconsultation().getId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .type(message.getMessageType())
                .timestamp(message.getSentAt())
                .fileUrl(message.getFileUrl())
                .build();
    }
}
