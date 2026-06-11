package com.medilinktunisia.teleconsultationservice.websocket;

import com.medilinktunisia.teleconsultationservice.model.dto.ChatMessage;
import com.medilinktunisia.teleconsultationservice.model.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

/**
 * Gère les événements WebSocket (connexion, déconnexion)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketSessionManager sessionManager;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long consultationId = (Long) headerAccessor.getSessionAttributes().get("consultationId");

        log.info("New WebSocket connection - SessionId: {}, Username: {}, UserId: {}, ConsultationId: {}",
                sessionId, username, userId, consultationId);

        if (consultationId != null && userId != null && username != null) {
            sessionManager.addSession(consultationId, sessionId, userId, username);

            // Notifier les autres participants de la connexion
            ChatMessage message = ChatMessage.builder()
                    .type(MessageType.JOIN)
                    .sender(username)
                    .senderId(userId)
                    .content(username + " a rejoint la consultation")
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/consultation/" + consultationId, message);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("WebSocket disconnection - SessionId: {}", sessionId);

        WebSocketSessionManager.SessionInfo sessionInfo = sessionManager.removeSession(sessionId);

        if (sessionInfo != null) {
            log.info("User {} disconnected from consultation {}", 
                    sessionInfo.getUsername(), sessionInfo.getConsultationId());

            // Notifier les autres participants de la déconnexion
            ChatMessage message = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(sessionInfo.getUsername())
                    .senderId(sessionInfo.getUserId())
                    .content(sessionInfo.getUsername() + " a quitté la consultation")
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/consultation/" + sessionInfo.getConsultationId(), 
                    message
            );
        }
    }
}
