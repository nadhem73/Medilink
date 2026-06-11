package com.medilinktunisia.teleconsultationservice.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Intercepteur pour l'authentification des connexions WebSocket
 * Extrait le token JWT et l'utilisateur du message de connexion
 */
@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extraire les informations d'authentification
            String username = accessor.getFirstNativeHeader("username");
            String userIdStr = accessor.getFirstNativeHeader("userId");
            String consultationIdStr = accessor.getFirstNativeHeader("consultationId");

            log.info("WebSocket CONNECT - Username: {}, UserId: {}, ConsultationId: {}", 
                    username, userIdStr, consultationIdStr);

            if (username != null && userIdStr != null && consultationIdStr != null) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    Long consultationId = Long.parseLong(consultationIdStr);

                    // Stocker dans les attributs de session
                    accessor.getSessionAttributes().put("username", username);
                    accessor.getSessionAttributes().put("userId", userId);
                    accessor.getSessionAttributes().put("consultationId", consultationId);

                    log.info("WebSocket authentication successful for user: {}", username);
                } catch (NumberFormatException e) {
                    log.error("Invalid userId or consultationId format", e);
                }
            } else {
                log.warn("Missing authentication headers in WebSocket CONNECT");
            }
        }

        // Propager l'authentification Spring Security si disponible
        if (accessor != null && accessor.getUser() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                accessor.setUser(authentication);
            }
        }

        return message;
    }
}
