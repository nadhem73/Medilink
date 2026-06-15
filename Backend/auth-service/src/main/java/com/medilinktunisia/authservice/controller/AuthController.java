package com.medilinktunisia.authservice.controller;

import com.medilinktunisia.authservice.dto.request.ForgotPasswordRequest;
import com.medilinktunisia.authservice.dto.request.LoginRequest;
import com.medilinktunisia.authservice.dto.request.RegisterRequest;
import com.medilinktunisia.authservice.dto.request.OtpVerificationRequest;
import com.medilinktunisia.authservice.dto.request.ResetPasswordRequest;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.dto.response.DoctorListDto;
import com.medilinktunisia.authservice.dto.response.PatientListDto;
import com.medilinktunisia.authservice.dto.response.MessageResponse;
import com.medilinktunisia.authservice.dto.response.UserDto;
import com.medilinktunisia.authservice.service.AuthService;
import com.medilinktunisia.authservice.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /** Auto-inscription d'un patient. */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Inscription réussie ! Vous pouvez maintenant vous connecter.", true));
    }

    /** Connexion : renvoie les tokens JWT et l'utilisateur. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Profil de l'utilisateur authentifié (via le JWT). */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    /**
     * Liste tous les médecins actifs pour l'écran de prise de rendez-vous patient.
     */
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorListDto>> getAllDoctors() {
        return ResponseEntity.ok(authService.getAllActiveDoctors());
    }

    /**
     * Liste tous les patients actifs pour l'écran de l'agenda médecin.
     */
    @GetMapping("/patients")
    public ResponseEntity<List<PatientListDto>> getAllPatients() {
        return ResponseEntity.ok(authService.getAllActivePatients());
    }

    /**
     * Demande l'envoi d'un code OTP par email pour l'utilisateur connecté.
     */
    @PostMapping("/verify-email/request")
    public ResponseEntity<MessageResponse> requestEmailVerification(Authentication authentication) {
        authService.requestEmailVerification(authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Code de vérification envoyé par e-mail.", true));
    }

    /**
     * Valide le code OTP et active l'adresse e-mail. Renvoye l'utilisateur mis à jour.
     */
    @PostMapping("/verify-email/verify")
    public ResponseEntity<UserDto> verifyEmail(
            Authentication authentication,
            @Valid @RequestBody OtpVerificationRequest request) {
        UserDto updatedUser = authService.verifyEmail(authentication.getName(), request.getCode());
        return ResponseEntity.ok(updatedUser);
    }


    /**
     * Demande de réinitialisation : envoie un lien par email si un compte correspond.
     * Patient → email ; médecin → numéro d'ordre. Réponse toujours générique.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(new MessageResponse(
                "Si un compte correspond, un lien de réinitialisation a été envoyé par email.", true));
    }

    /** Définit un nouveau mot de passe à partir du jeton reçu par email. */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse(
                "Votre mot de passe a été réinitialisé. Vous pouvez vous connecter.", true));
    }
}

