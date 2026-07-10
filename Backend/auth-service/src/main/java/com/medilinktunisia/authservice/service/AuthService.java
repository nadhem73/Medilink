package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.client.MedicalRecordRequest;
import com.medilinktunisia.authservice.client.PatientServiceClient;
import com.medilinktunisia.authservice.dto.request.AdminUserActionRequest;
import com.medilinktunisia.authservice.dto.request.LinkTelegramRequest;
import com.medilinktunisia.authservice.dto.request.LoginRequest;
import com.medilinktunisia.authservice.dto.request.RegisterRequest;
import com.medilinktunisia.authservice.dto.response.AdminUserDto;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.dto.response.DoctorListDto;
import com.medilinktunisia.authservice.dto.response.PatientListDto;
import com.medilinktunisia.authservice.dto.response.UserDto;
import com.medilinktunisia.authservice.exception.AccountStatusException;
import com.medilinktunisia.authservice.exception.DuplicateResourceException;
import com.medilinktunisia.authservice.exception.EmailAlreadyExistsException;
import com.medilinktunisia.authservice.model.entity.Admin;
import com.medilinktunisia.authservice.model.entity.Doctor;
import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.model.entity.Pharmacy;
import com.medilinktunisia.authservice.model.entity.User;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import com.medilinktunisia.authservice.repository.DoctorRepository;
import com.medilinktunisia.authservice.repository.PatientRepository;
import com.medilinktunisia.authservice.repository.PharmacyRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import com.medilinktunisia.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logique d'authentification : inscription patient et connexion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PatientServiceClient patientServiceClient;
    private final EmailVerificationService emailVerificationService;

    /**
     * Inscription d'un patient (auto-inscription).
     * Crée l'identité dans l'auth-service puis le dossier médical dans le patient-service.
     */
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Un compte existe déjà avec cet email");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Ce numéro de téléphone est déjà utilisé par un autre compte");
        }

        // Le CIN est optionnel : on le normalise (vide -> null) pour que plusieurs
        // patients sans CIN ne se chevauchent pas sur la contrainte d'unicité.
        String cin = request.getCin() != null ? request.getCin().trim() : null;
        if (cin != null && cin.isEmpty()) {
            cin = null;
        }
        if (cin != null && patientRepository.existsByCin(cin)) {
            throw new DuplicateResourceException("Ce numéro CIN est déjà utilisé par un autre compte");
        }

        Patient patient = new Patient();
        patient.setEmail(request.getEmail());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setCin(cin);
        // Le rôle est forcé côté serveur : une auto-inscription = toujours un PATIENT.
        patient.setRole(Role.PATIENT);
        patient.setStatus(UserStatus.ACTIVE);

        Patient saved = patientRepository.save(patient);

        // Création du dossier médical dans le patient-service.
        createMedicalRecord(saved.getId(), request);

        return toUserDto(saved);
    }

    private void createMedicalRecord(Long userId, RegisterRequest request) {
        MedicalRecordRequest medicalRecord = MedicalRecordRequest.builder()
                .userId(userId)
                .bloodGroup(request.getBloodGroup())
                .height(request.getHeight())
                .weight(request.getWeight())
                .allergies(request.getAllergies())
                .chronicDiseases(request.getChronicDiseases())
                .currentTreatments(request.getCurrentTreatments())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .insuranceCompany(request.getInsuranceCompany())
                .insuranceNumber(request.getInsuranceNumber())
                .build();
        try {
            patientServiceClient.createMedicalRecord(medicalRecord);
        } catch (Exception e) {
            // Le compte est créé même si le patient-service est indisponible ;
            // le dossier médical pourra être complété plus tard.
            log.error("Échec de la création du dossier médical pour l'utilisateur {} : {}",
                    userId, e.getMessage());
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Selon le rôle, on retrouve l'email du compte à partir de l'identifiant fourni.
        String email = request.getEmail();

        if (request.getCin() != null && !request.getCin().isBlank()) {
            // Patient : identifié par son CIN.
            String cin = request.getCin().trim();
            email = patientRepository.findByCin(cin)
                    .map(Patient::getEmail)
                    .orElse(null);
        } else if (request.getLicenseNumber() != null && !request.getLicenseNumber().isBlank()) {
            // Identifiant par numéro de licence : médecin (numéro d'ordre) ou pharmacie.
            String license = request.getLicenseNumber().trim();
            email = doctorRepository.findByLicenseNumber(license)
                    .map(Doctor::getEmail)
                    .or(() -> pharmacyRepository.findByLicenseNumber(license).map(Pharmacy::getEmail))
                    .orElse(null);
        }

        // Fallback : l'identifiant saisi est peut-être un email (admin, etc.).
        if (email == null || email.isBlank()) {
            String raw = request.getCin() != null ? request.getCin() : request.getLicenseNumber();
            if (raw != null && !raw.isBlank()) {
                String candidate = raw.trim();
                if (userRepository.findByEmail(candidate).isPresent()) {
                    email = candidate;
                }
            }
        }

        if (email == null || email.isBlank()) {
            throw new BadCredentialsException("Identifiant ou mot de passe incorrect");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        User user = userRepository.findByEmail(email).orElseThrow();

        // Le mot de passe est valide : on vérifie l'état du compte.
        checkAccountStatus(user);

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(toUserDto(user))
                .build();
    }

    /**
     * Applique la politique de statut lors de la connexion.
     * <ul>
     *   <li>SUSPENDED avec date de fin dépassée → réactivation automatique (ACTIVE).</li>
     *   <li>SUSPENDED encore en cours → connexion refusée (AccountStatusException).</li>
     *   <li>INACTIVE → connexion refusée (réactivation par l'admin uniquement).</li>
     * </ul>
     */
    private void checkAccountStatus(User user) {
        String role = user.getRole() != null ? user.getRole().name() : null;

        if (user.getStatus() == UserStatus.SUSPENDED) {
            LocalDateTime until = user.getSuspendUntil();
            if (until != null && !until.isAfter(LocalDateTime.now())) {
                // La suspension est arrivée à échéance : on réactive le compte.
                user.setStatus(UserStatus.ACTIVE);
                user.setSuspendUntil(null);
                userRepository.save(user);
                log.info("Compte {} réactivé automatiquement (fin de suspension).", user.getEmail());
                return;
            }
            throw new AccountStatusException(UserStatus.SUSPENDED, role, until, "Votre compte est suspendu.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AccountStatusException(UserStatus.INACTIVE, role, null, "Votre compte est désactivé.");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(toUserDto(user))
                .build();
    }

    public UserDto getCurrentUser(String email) {
        return toUserDto(userRepository.findByEmail(email).orElseThrow());
    }

    public void requestEmailVerification(String email) {
        emailVerificationService.requestEmailVerification(email);
    }

    @Transactional
    public UserDto verifyEmail(String email, String code) {
        return toUserDto(emailVerificationService.verifyEmail(email, code));
    }

    /**
     * Liste tous les médecins actifs pour l'écran de prise de rendez-vous patient.
     */
    public List<DoctorListDto> getAllActiveDoctors() {
        return doctorRepository.findAll().stream()
                .filter(d -> d.getStatus() == UserStatus.ACTIVE)
                .map(this::toDoctorListDto)
                .toList();
    }

    /**
     * Liste tous les patients actifs (utilisé par le panel médecin).
     */
    public List<PatientListDto> getAllActivePatients() {
        return patientRepository.findAll().stream()
                .filter(p -> p.getStatus() == UserStatus.ACTIVE)
                .map(this::toPatientListDto)
                .toList();
    }

    private UserDto toUserDto(User user) {
        UserDto.UserDtoBuilder builder = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .isEmailVerified(user.isEmailVerified())
                .roles(List.of(user.getRole().name()))
                .createdAt(user.getCreatedAt());

        if (user instanceof Patient patient) {
            builder.birthDate(patient.getBirthDate())
                    .gender(patient.getGender() != null ? patient.getGender().name() : null)
                    .address(patient.getAddress());
        } else if (user instanceof Pharmacy pharmacy) {
            builder.pharmacieId(pharmacy.getId());
        }
        return builder.build();
    }

    private DoctorListDto toDoctorListDto(Doctor doctor) {
        return DoctorListDto.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .specialty(doctor.getSpecialty())
                .hospital(doctor.getHospital())
                .licenseNumber(doctor.getLicenseNumber())
                .build();
    }

    public void linkTelegram(LinkTelegramRequest request) {
        Patient patient = patientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Patient not found with email: " + request.getEmail()));
        patient.setTelegramChatId(request.getTelegramChatId());
        patientRepository.save(patient);
    }

    public String getPatientTelegramChatId(Long patientId) {
        return patientRepository.findById(patientId)
                .map(Patient::getTelegramChatId)
                .orElse(null);
    }

    /**
     * Admin : liste tous les utilisateurs (tous rôles, tous statuts).
     */
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !(u instanceof Admin))
                .map(this::toAdminUserDto)
                .toList();
    }

    /**
     * Admin : modifier le statut d'un utilisateur (ACTIVE/INACTIVE/SUSPENDED).
     * Pour SUSPENDED, un suspendUntil peut être précisé.
     */
    @Transactional
    public void updateUserStatus(Long userId, AdminUserActionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        user.setStatus(request.getStatus());

        if (request.getStatus() == UserStatus.SUSPENDED && request.getSuspendUntil() != null) {
            user.setSuspendUntil(request.getSuspendUntil());
        } else {
            user.setSuspendUntil(null);
        }

        userRepository.save(user);
    }

    private AdminUserDto toAdminUserDto(User user) {
        AdminUserDto.AdminUserDtoBuilder builder = AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .suspendUntil(user.getSuspendUntil());

        if (user instanceof Doctor doctor) {
            builder.specialty(doctor.getSpecialty())
                    .licenseNumber(doctor.getLicenseNumber());
        } else if (user instanceof Pharmacy pharmacy) {
            builder.licenseNumber(pharmacy.getLicenseNumber())
                    .pharmacyName(pharmacy.getPharmacyName());
        }

        return builder.build();
    }

    private PatientListDto toPatientListDto(Patient patient) {
        return PatientListDto.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .gender(patient.getGender() != null ? patient.getGender().name() : null)
                .address(patient.getAddress())
                .birthDate(patient.getBirthDate() != null ? patient.getBirthDate().toString() : null)
                .cin(patient.getCin())
                .telegramChatId(patient.getTelegramChatId())
                .build();
    }
}
