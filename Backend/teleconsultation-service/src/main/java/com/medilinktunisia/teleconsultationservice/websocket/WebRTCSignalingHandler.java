package com.medilinktunisia.teleconsultationservice.websocket;

import com.medilinktunisia.teleconsultationservice.model.dto.WebRTCSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

/**
 * Gestionnaire des signaux WebRTC pour la communication vidéo
 * Permet l'échange d'offres/réponses SDP et de candidats ICE
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebRTCSignalingHandler {

    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Envoyer une offre WebRTC à un participant spécifique
     */
    public void sendOffer(Long consultationId, Long fromUserId, Long toUserId, WebRTCSignal offer) {
        log.info("Sending WebRTC offer from user {} to user {} in consultation {}", 
                fromUserId, toUserId, consultationId);
        
        offer.setFromUserId(fromUserId);
        offer.setToUserId(toUserId);
        offer.setType("offer");
        
        messagingTemplate.convertAndSendToUser(
                toUserId.toString(),
                "/queue/webrtc",
                offer
        );
    }

    /**
     * Envoyer une réponse WebRTC à un participant spécifique
     */
    public void sendAnswer(Long consultationId, Long fromUserId, Long toUserId, WebRTCSignal answer) {
        log.info("Sending WebRTC answer from user {} to user {} in consultation {}", 
                fromUserId, toUserId, consultationId);
        
        answer.setFromUserId(fromUserId);
        answer.setToUserId(toUserId);
        answer.setType("answer");
        
        messagingTemplate.convertAndSendToUser(
                toUserId.toString(),
                "/queue/webrtc",
                answer
        );
    }

    /**
     * Envoyer un candidat ICE à un participant spécifique
     */
    public void sendIceCandidate(Long consultationId, Long fromUserId, Long toUserId, WebRTCSignal candidate) {
        log.info("Sending ICE candidate from user {} to user {} in consultation {}", 
                fromUserId, toUserId, consultationId);
        
        candidate.setFromUserId(fromUserId);
        candidate.setToUserId(toUserId);
        candidate.setType("candidate");
        
        messagingTemplate.convertAndSendToUser(
                toUserId.toString(),
                "/queue/webrtc",
                candidate
        );
    }

    /**
     * Diffuser un signal de contrôle (mute, unmute, video on/off)
     */
    public void broadcastControlSignal(Long consultationId, Long fromUserId, String controlType, Object data) {
        log.info("Broadcasting control signal '{}' from user {} in consultation {}", 
                controlType, fromUserId, consultationId);
        
        WebRTCSignal signal = WebRTCSignal.builder()
                .fromUserId(fromUserId)
                .type(controlType)
                .data(data)
                .build();
        
        messagingTemplate.convertAndSend(
                "/topic/consultation/" + consultationId + "/control",
                signal
        );
    }

    /**
     * Notifier la fin d'appel à tous les participants
     */
    public void notifyCallEnded(Long consultationId, Long userId) {
        log.info("Notifying call ended by user {} in consultation {}", userId, consultationId);
        
        WebRTCSignal signal = WebRTCSignal.builder()
                .fromUserId(userId)
                .type("call-ended")
                .build();
        
        messagingTemplate.convertAndSend(
                "/topic/consultation/" + consultationId + "/control",
                signal
        );
    }
}
