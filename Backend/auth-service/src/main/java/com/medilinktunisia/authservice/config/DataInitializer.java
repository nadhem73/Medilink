package com.medilinktunisia.authservice.config;

import com.medilinktunisia.authservice.model.entity.Role;
import com.medilinktunisia.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Initialiser les rôles s'ils n'existent pas
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName.getDescription())
                        .build();
                roleRepository.save(role);
                log.info("Rôle créé: {}", roleName);
            }
        }
        log.info("Initialisation des rôles terminée");
    }
}
