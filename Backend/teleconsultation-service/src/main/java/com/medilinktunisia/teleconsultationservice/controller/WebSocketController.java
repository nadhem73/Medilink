package com.medilinktunisia.teleconsultationservice.controller;

import com.medilinktunisia.teleconsultationservice.model.dto.ChatMessage;
import com.medilinktunisia.teleconsultationservice.model.dto.WebRTCSignal;
import com.medilinktunisia.teleconsultationservice.service.ChatService;
import com.medilinktunisia.teleconsultationservice.websocket.WebRTCSignalingHandler;
import com.medilinktunisia.teleconsultationservice.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

/**
 * Contrôleur WebSocket pour la gestion des téléconsultations
 * Gère les messages chat, les signaux WebRTC et les événements de session
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;
    private final WebRTCSignalingHandler webRTCHandler;
    private final WebSocketSessionManager sessionManager;

    /**
     * Envoyer un message de chat dans une consultation
     */
    @MessageMapping("/consultation/{consultationId}/message")
    public void sendMessage(
            @DestinationVariable Long consultationId,
            @Payload ChatMessage message,
            Principal principal) {
        
        log.info("Chat message received for consultation {}: from={}", 
                consultationId, message.getSender());
        
        // Convert consultationId to String for service call
        String sessionId = String.valueOf(consultationId);
        
        // Note: In a real implementation, you'd need to map ChatMessage to ChatMessageDto
        // For now, we'll just broadcast the message
        messagingTemplate.convertAndSend(
                "/topic/consultation/" + consultationId,
                message
        );
    }

    /**
     * Envoyer une offre WebRTC
     */
    @MessageMapping("/consultation/{consultationId}/webrtc/offer")
    public void sendWebRTCOffer(
            @DestinationVariable Long consultationId,
            @Payload WebRTCSignal offer,
            Principal principal) {
        
        log.info("WebRTC offer from user {} to user {} in consultation {}", 
                offer.getFromUserId(), offer.getToUserId(), consultationId);
        
        webRTCHandler.sendOffer(consultationId, offer.getFromUserId(), 
                offer.getToUserId(), offer);
    }

    /**
     * Envoyer une réponse WebRTC
     */
    @MessageMapping("/consultation/{consultationId}/webrtc/answer")
    public void sendWebRTCAnswer(
            @DestinationVariable Long consultationId,
            @Payload WebRTCSignal answer,
            Principal principal) {
        
        log.info("WebRTC answer from user {} to user {} in consultation {}", 
                answer.getFromUserId(), answer.getToUserId(), consultationId);
        
        webRTCHandler.sendAnswer(consultationId, answer.getFromUserId(), 
                answer.getToUserId(), answer);
    }

    /**
     * Envoyer un candidat ICE
     */
    @MessageMapping("/consultation/{consultationId}/webrtc/candidate")
    public void sendIceCandidate(
            @DestinationVariable Long consultationId,
            @Payload WebRTCSignal candidate,
            Principal principal) {
        
        log.info("ICE candidate from user {} to user {} in consultation {}", 
                candidate.getFromUserId(), candidate.getToUserId(), consultationId);
        
        webRTCHandler.sendIceCandidate(consultationId, candidate.getFromUserId(), 
                candidate.getToUserId(), candidate);
    }

    /**
     * Gérer les contrôles média (mute/unmute audio/video)
     */
    @MessageMapping("/consultation/{consultationId}/control")
    public void handleMediaControl(
            @DestinationVariable Long consultationId,
            @Payload WebRTCSignal control,
            Principal principal) {
        
        log.info("Media control '{}' from user {} in consultation {}", 
                control.getType(), control.getFromUserId(), consultationId);
        
        webRTCHandler.broadcastControlSignal(consultationId, 
                control.getFromUserId(), control.getType(), control.getData());
    }

    /**
     * Terminer l'appel
     */
    @MessageMapping("/consultation/{consultationId}/end-call")
    public void endCall(
            @DestinationVariable Long consultationId,
            @Payload Long userId,
            Principal principal) {
        
        log.info("Call ended by user {} in consultation {}", userId, consultationId);
        
        webRTCHandler.notifyCallEnded(consultationId, userId);
    }

    /**
     * Obtenir la liste des participants actifs
     */
    @MessageMapping("/consultation/{consultationId}/participants")
    public void getActiveParticipants(
            @DestinationVariable Long consultationId,
            Principal principal) {
        
        List<WebSocketSessionManager.SessionInfo> participants = 
                sessionManager.getConsultationSessions(consultationId);
        
        log.info("Returning {} active participants for consultation {}", 
                participants.size(), consultationId);
        
        messagingTemplate.convertAndSend(
                "/topic/consultation/" + consultationId + "/participants",
                participants
        );
    }

    /**
     * Envoyer une notification de typing (en train d'écrire)
     */
    @MessageMapping("/consultation/{consultationId}/typing")
    public void handleTyping(
            @DestinationVariable Long consultationId,
            @Payload TypingNotification notification,
            Principal principal) {
        
        log.debug("Typing notification from {} in consultation {}", 
                notification.username(), consultationId);
        
        messagingTemplate.convertAndSend(
                "/topic/consultation/" + consultationId + "/typing",
                notification
        );
    }

    /**
     * Helper record pour les notifications de typing
     */
    public record TypingNotification(String username, Long userId, boolean isTyping) {}
}
