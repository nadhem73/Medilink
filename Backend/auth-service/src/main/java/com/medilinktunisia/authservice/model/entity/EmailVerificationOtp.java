package com.medilinktunisia.authservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Code OTP de validation de l'adresse e-mail d'un utilisateur.
 * Valable 10 minutes, à usage unique.
 */
@Entity
@Table(name = "email_verification_otps", indexes = {
        @Index(name = "idx_email_verification_otp_user_id", columnList = "user_id"),
        @Index(name = "idx_email_verification_otp_code", columnList = "code")
})
@Getter
@Setter
public class EmailVerificationOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isUsable() {
        return !used && expiresAt.isAfter(LocalDateTime.now());
    }
}
