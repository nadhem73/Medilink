package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.model.entity.SecurityEvent;
import com.medilinktunisia.authservice.repository.SecurityEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Enregistre les événements de sécurité. Ne doit jamais faire échouer
 * la requête métier : toute erreur d'écriture est simplement journalisée.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityEventService {

    private final SecurityEventRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String type, String identifier, HttpServletRequest request, String detail) {
        try {
            repository.save(SecurityEvent.builder()
                    .type(type)
                    .identifier(truncate(identifier, 150))
                    .ipAddress(truncate(clientIp(request), 64))
                    .detail(truncate(detail, 255))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Could not record security event {}: {}", type, e.getMessage());
        }
    }

    /** IP réelle du client, y compris derrière l'API Gateway (X-Forwarded-For). */
    public static String clientIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp.trim();
        return request.getRemoteAddr();
    }

    private static String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
