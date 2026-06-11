package com.medilinktunisia.teleconsultationservice.controller;

import com.medilinktunisia.teleconsultationservice.model.dto.ChatMessageDto;
import com.medilinktunisia.teleconsultationservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teleconsultations/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{sessionId}/messages")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<List<ChatMessageDto>> getConsultationMessages(
            @PathVariable String sessionId) {
        log.info("REST request to get chat messages for session: {}", sessionId);
        List<ChatMessageDto> messages = chatService.getConsultationMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{sessionId}/messages/paginated")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<List<ChatMessageDto>> getConsultationMessagesPaginated(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("REST request to get paginated chat messages for session: {}", sessionId);
        List<ChatMessageDto> messages = chatService.getConsultationMessagesPaginated(sessionId, page, size);
        return ResponseEntity.ok(messages);
    }
}
