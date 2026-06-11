package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.model.dto.*;
import com.medilinktunisia.authservice.model.entity.RefreshToken;
import com.medilinktunisia.authservice.model.entity.Role;
import com.medilinktunisia.authservice.model.entity.User;
import com.medilinktunisia.authservice.repository.RoleRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import com.medilinktunisia.authservice.security.JwtTokenProvider;
import com.medilinktunisia.authservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * Inscription d'un nouveau utilisateur
     */
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // ✅ SÉCURITÉ: Forcer le rôle PATIENT pour les inscriptions publiques
        if (request.getRole() != Role.RoleName.PATIENT) {
            throw new RuntimeException("Seul le rôle PATIENT est autorisé pour l'inscription publique");
        }

        // Créer le nouvel utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress() != null ? request.getAddress() : "")
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .cin(request.getCin())
                .status(User.UserStatus.ACTIVE)
                .isEmailVerified(false)
                .roles(new HashSet<>())
                .build();

        // Assigner le rôle PATIENT
        Role role = roleRepository.findByName(Role.RoleName.PATIENT)
                .orElseThrow(() -> new RuntimeException("Rôle PATIENT non trouvé"));
        user.addRole(role);

        // Sauvegarder
        userRepository.save(user);

        log.info("Nouvel utilisateur PATIENT enregistré: {}", user.getEmail());

        return MessageResponse.builder()
                .message("Inscription réussie! Veuillez vérifier votre email.")
                .success(true)
                .build();
    }

    /**
     * Connexion utilisateur
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authentification
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // Vérifier si l'utilisateur est actif
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("Compte désactivé ou en attente de validation");
        }

        // Générer les tokens
        String accessToken = jwtTokenProvider.generateAccessTokenFromUser(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("Utilisateur connecté: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(UserDto.fromUser(user))
                .build();
    }

    /**
     * Refresh du token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUser(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(UserDto.fromUser(user))
                .build();
    }

    /**
     * Déconnexion
     */
    @Transactional
    public MessageResponse logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);

        return MessageResponse.builder()
                .message("Déconnexion réussie")
                .success(true)
                .build();
    }
}
