package com.medilinktunisia.authservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Journal des événements de sécurité (connexions, rafraîchissements, JWT expirés).
 * Lu par le monitoring-service pour alimenter le tableau de bord sécurité de l'admin.
 */
@Entity
@Table(name = "security_events", indexes = {
        @Index(name = "idx_security_events_created_at", columnList = "created_at"),
        @Index(name = "idx_security_events_type", columnList = "type"),
        @Index(name = "idx_security_events_ip", columnList = "ip_address")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEvent {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String TOKEN_REFRESH = "TOKEN_REFRESH";
    public static final String JWT_EXPIRED = "JWT_EXPIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String type;

    /** Email ou identifiant saisi (CIN, numéro de licence). */
    @Column(length = 150)
    private String identifier;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(length = 255)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
