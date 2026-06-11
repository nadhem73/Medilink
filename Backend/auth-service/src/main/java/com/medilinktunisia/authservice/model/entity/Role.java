package com.medilinktunisia.authservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@lombok.EqualsAndHashCode(exclude = {"users"})
@lombok.ToString(exclude = {"users"})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;

    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // Enum pour les types de rôles
    public enum RoleName {
        PATIENT("Patient - Bénéficiaire des soins"),
        DOCTOR("Médecin - Professionnel de santé"),
        PHARMACIST("Pharmacien - Gestion des médicaments"),
        LABORATORY("Laboratoire - Analyses médicales"),
        AMBULANCE("Ambulance - Services d'urgence"),
        ADMIN("Administrateur - Gestion de la plateforme");

        private final String description;

        RoleName(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
