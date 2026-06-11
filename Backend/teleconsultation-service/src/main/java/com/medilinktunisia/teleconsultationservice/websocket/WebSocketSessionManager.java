package com.medilinktunisia.teleconsultationservice.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestionnaire des sessions WebSocket actives
 * Permet de tracker qui est connecté à quelle consultation
 */
@Component
@Slf4j
public class WebSocketSessionManager {

    // Map: sessionId -> SessionInfo
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    
    // Map: consultationId -> Set<sessionId>
    private final Map<Long, Set<String>> consultationSessions = new ConcurrentHashMap<>();

    /**
     * Ajouter une nouvelle session
     */
    public void addSession(Long consultationId, String sessionId, Long userId, String username) {
        SessionInfo sessionInfo = SessionInfo.builder()
                .sessionId(sessionId)
                .consultationId(consultationId)
                .userId(userId)
                .username(username)
                .connectedAt(LocalDateTime.now())
                .build();

        sessions.put(sessionId, sessionInfo);
        
        consultationSessions.computeIfAbsent(consultationId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);

        log.info("Session added - SessionId: {}, User: {}, Consultation: {}", 
                sessionId, username, consultationId);
    }

    /**
     * Retirer une session
     */
    public SessionInfo removeSession(String sessionId) {
        SessionInfo sessionInfo = sessions.remove(sessionId);
        
        if (sessionInfo != null) {
            Long consultationId = sessionInfo.getConsultationId();
            Set<String> sessions = consultationSessions.get(consultationId);
            
            if (sessions != null) {
                sessions.remove(sessionId);
                
                // Nettoyer si plus aucune session pour cette consultation
                if (sessions.isEmpty()) {
                    consultationSessions.remove(consultationId);
                }
            }
            
            log.info("Session removed - SessionId: {}, User: {}, Consultation: {}", 
                    sessionId, sessionInfo.getUsername(), consultationId);
        }
        
        return sessionInfo;
    }

    /**
     * Obtenir toutes les sessions d'une consultation
     */
    public List<SessionInfo> getConsultationSessions(Long consultationId) {
        Set<String> sessionIds = consultationSessions.get(consultationId);
        
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return sessionIds.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir le nombre de participants actifs pour une consultation
     */
    public int getActiveParticipantsCount(Long consultationId) {
        return getConsultationSessions(consultationId).size();
    }

    /**
     * Vérifier si un utilisateur est connecté à une consultation
     */
    public boolean isUserConnected(Long consultationId, Long userId) {
        return getConsultationSessions(consultationId).stream()
                .anyMatch(session -> session.getUserId().equals(userId));
    }

    /**
     * Obtenir les IDs de tous les utilisateurs connectés à une consultation
     */
    public Set<Long> getConnectedUserIds(Long consultationId) {
        return getConsultationSessions(consultationId).stream()
                .map(SessionInfo::getUserId)
                .collect(Collectors.toSet());
    }

    /**
     * Déconnecter toutes les sessions d'une consultation
     */
    public void disconnectAllSessions(Long consultationId) {
        Set<String> sessionIds = consultationSessions.remove(consultationId);
        
        if (sessionIds != null) {
            sessionIds.forEach(sessions::remove);
            log.info("Disconnected all sessions for consultation: {}", consultationId);
        }
    }

    /**
     * Obtenir le nombre total de sessions actives
     */
    public int getTotalActiveSessions() {
        return sessions.size();
    }

    /**
     * Obtenir le nombre total de consultations actives
     */
    public int getActiveConsultationsCount() {
        return consultationSessions.size();
    }

    /**
     * Informations sur une session WebSocket
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionInfo {
        private String sessionId;
        private Long consultationId;
        private Long userId;
        private String username;
        private LocalDateTime connectedAt;
    }
}
