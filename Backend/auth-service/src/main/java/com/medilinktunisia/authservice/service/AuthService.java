package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.client.MedicalRecordRequest;
import com.medilinktunisia.authservice.client.PatientServiceClient;
import com.medilinktunisia.authservice.dto.request.LoginRequest;
import com.medilinktunisia.authservice.dto.request.RegisterRequest;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.dto.response.DoctorListDto;
import com.medilinktunisia.authservice.dto.response.PatientListDto;
import com.medilinktunisia.authservice.dto.response.UserDto;
import com.medilinktunisia.authservice.exception.DuplicateResourceException;
import com.medilinktunisia.authservice.exception.EmailAlreadyExistsException;
import com.medilinktunisia.authservice.model.entity.Doctor;
import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.model.entity.User;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import com.medilinktunisia.authservice.repository.DoctorRepository;
import com.medilinktunisia.authservice.repository.PatientRepository;
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

    public AuthResponse login(LoginRequest request) {
        // Selon le rôle, on retrouve l'email du compte à partir de l'identifiant fourni.
        String email = request.getEmail();
        if (request.getCin() != null && !request.getCin().isBlank()) {
            // Patient : identifié par son CIN.
            email = patientRepository.findByCin(request.getCin().trim())
                    .map(Patient::getEmail)
                    .orElseThrow(() -> new BadCredentialsException("CIN ou mot de passe incorrect"));
        } else if (request.getLicenseNumber() != null && !request.getLicenseNumber().isBlank()) {
            // Médecin : identifié par son numéro d'ordre.
            email = doctorRepository.findByLicenseNumber(request.getLicenseNumber().trim())
                    .map(Doctor::getEmail)
                    .orElseThrow(() -> new BadCredentialsException("Numéro d'ordre ou mot de passe incorrect"));
        }
        if (email == null || email.isBlank()) {
            throw new BadCredentialsException("Identifiant ou mot de passe incorrect");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        User user = userRepository.findByEmail(email).orElseThrow();

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(toUserDto(user))
                .build();
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
                .build();
    }
}
