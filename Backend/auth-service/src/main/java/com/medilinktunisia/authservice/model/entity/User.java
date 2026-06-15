package com.medilinktunisia.authservice.model.entity;

import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Utilisateur (classe abstraite du diagramme de classe).
 * Contient les attributs communs à tous les types d'utilisateurs.
 * <p>
 * Stratégie d'héritage JOINED : une table {@code users} pour les champs communs
 * et une table par sous-type (ex. {@code patients}) reliées par la clé primaire.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_phone", columnList = "phone", unique = true),
        @Index(name = "idx_users_status", columnList = "status")
})
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Mot de passe haché (BCrypt) — correspond à passwordHash du diagramme. */
    @Column(name = "password_hash", nullable = false)
    private String password;

    /** prenom dans le diagramme. */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /** nom dans le diagramme. */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /** telephone dans le diagramme. */
    @Column(length = 20, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified = false;

    /** dateInscription dans le diagramme. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) {
            this.status = UserStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
