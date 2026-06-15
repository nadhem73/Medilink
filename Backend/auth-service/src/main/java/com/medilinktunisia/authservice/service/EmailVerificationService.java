package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.model.entity.EmailVerificationOtp;
import com.medilinktunisia.authservice.model.entity.User;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import com.medilinktunisia.authservice.repository.EmailVerificationOtpRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationOtpRepository otpRepository;
    private final EmailService emailService;

    /**
     * Génère un OTP à 6 chiffres, l'enregistre en base et l'envoie par mail.
     */
    @Transactional
    public void requestEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("L'adresse e-mail est déjà vérifiée.");
        }

        // Invalide les anciens codes OTP actifs
        otpRepository.invalidateActiveOtps(user.getId());

        // Génère un OTP à 6 chiffres
        String code = String.format("%06d", new Random().nextInt(1000000));

        EmailVerificationOtp otp = new EmailVerificationOtp();
        otp.setCode(code);
        otp.setUserId(user.getId());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // Valide 10 minutes
        otpRepository.save(otp);

        String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
        emailService.sendEmailVerificationOtp(user.getEmail(), displayName, code);
        log.info("Code OTP généré pour l'utilisateur ID: {}", user.getId());
    }

    /**
     * Valide le code OTP saisi. Si correct, active le compte.
     */
    @Transactional
    public User verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));

        if (user.isEmailVerified()) {
            return user;
        }

        EmailVerificationOtp otp = otpRepository.findByUserIdAndCode(user.getId(), code.trim())
                .orElseThrow(() -> new BadCredentialsException("Code de vérification incorrect ou expiré."));

        if (!otp.isUsable()) {
            throw new BadCredentialsException("Le code de vérification a expiré ou a déjà été utilisé.");
        }

        // Marquer l'OTP comme utilisé
        otp.setUsed(true);
        otpRepository.save(otp);

        // Valider l'email de l'utilisateur
        user.setEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        User savedUser = userRepository.save(user);

        log.info("L'e-mail de l'utilisateur ID {} a été vérifié avec succès. Son statut est désormais {}", 
                user.getId(), user.getStatus());
        
        return savedUser;
    }
}
