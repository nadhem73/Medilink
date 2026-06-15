package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.dto.request.ForgotPasswordRequest;
import com.medilinktunisia.authservice.dto.request.ResetPasswordRequest;
import com.medilinktunisia.authservice.model.entity.Doctor;
import com.medilinktunisia.authservice.model.entity.PasswordResetToken;
import com.medilinktunisia.authservice.model.entity.User;
import com.medilinktunisia.authservice.repository.DoctorRepository;
import com.medilinktunisia.authservice.repository.PasswordResetTokenRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Réinitialisation de mot de passe (« mot de passe oublié »).
 * <ul>
 *   <li>Le patient s'identifie par l'<b>email</b> de son compte.</li>
 *   <li>Le médecin s'identifie par son <b>numéro d'ordre</b>.</li>
 * </ul>
 * Pour ne pas révéler l'existence d'un compte, la demande renvoie toujours un
 * message générique ; l'email n'est envoyé que si un compte correspond.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.reset-token-expiration-minutes:60}")
    private long expirationMinutes;

    /** Crée un jeton et envoie l'email de réinitialisation si le compte existe. */
    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        Optional<User> userOpt = resolveUser(request);

        if (userOpt.isEmpty()) {
            log.info("Demande de réinitialisation sans compte correspondant (role={})", request.getRole());
            return; // réponse générique côté contrôleur
        }

        User user = userOpt.get();

        // Un seul jeton actif à la fois.
        tokenRepository.invalidateActiveTokens(user.getId());

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUserId(user.getId());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        tokenRepository.save(token);

        String resetLink = frontendUrl + "/auth/reset-password?token=" + token.getToken();
        String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
        emailService.sendPasswordResetEmail(user.getEmail(), displayName, resetLink);
    }

    /** Résout l'utilisateur selon le rôle et l'identifiant fourni. */
    private Optional<User> resolveUser(ForgotPasswordRequest request) {
        String role = request.getRole() == null ? "" : request.getRole().trim().toLowerCase();

        return switch (role) {
            case "patient" -> {
                String email = request.getEmail();
                yield (email == null || email.isBlank())
                        ? Optional.empty()
                        : userRepository.findByEmail(email.trim());
            }
            case "doctor" -> {
                String license = request.getLicenseNumber();
                yield (license == null || license.isBlank())
                        ? Optional.empty()
                        : doctorRepository.findByLicenseNumber(license.trim()).map(d -> (User) d);
            }
            default -> Optional.empty();
        };
    }

    /** Applique le nouveau mot de passe à partir d'un jeton valide. */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadCredentialsException("Lien de réinitialisation invalide."));

        if (!token.isUsable()) {
            throw new BadCredentialsException("Lien de réinitialisation expiré ou déjà utilisé.");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Compte introuvable."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Mot de passe réinitialisé pour l'utilisateur {}", user.getId());
    }
}
