package com.medilinktunisia.authservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Jeton de réinitialisation de mot de passe.
 * Généré lors d'une demande « mot de passe oublié », envoyé par email,
 * à usage unique et avec une durée de validité limitée.
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_password_reset_token", columnList = "token", unique = true)
})
@Getter
@Setter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Jeton opaque (UUID) transmis dans le lien de réinitialisation. */
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    /** Utilisateur (User) concerné par la réinitialisation. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Vrai une fois le jeton consommé : empêche toute réutilisation. */
    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /** Le jeton est exploitable s'il n'a pas été utilisé et n'est pas expiré. */
    public boolean isUsable() {
        return !used && expiresAt.isAfter(LocalDateTime.now());
    }
}
