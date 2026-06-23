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
import com.medilinktunisia.authservice.model.enums.Gender;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import com.medilinktunisia.authservice.repository.DoctorRepository;
import com.medilinktunisia.authservice.repository.PatientRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import com.medilinktunisia.authservice.security.JwtService;
import com.medilinktunisia.authservice.service.EmailVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PatientServiceClient patientServiceClient;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks private AuthService authService;

    @Captor private ArgumentCaptor<Patient> patientCaptor;
    @Captor private ArgumentCaptor<MedicalRecordRequest> medicalRecordCaptor;

    private RegisterRequest registerRequest;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPhone("+21650123456");
        registerRequest.setCin("12345678");
        registerRequest.setPassword("password123");
        registerRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        registerRequest.setGender(Gender.MALE);
        registerRequest.setAddress("Tunis");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setEmail("john.doe@example.com");
        testPatient.setPassword("encodedPassword");
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setPhone("+21650123456");
        testPatient.setCin("12345678");
        testPatient.setBirthDate(LocalDate.of(1990, 1, 1));
        testPatient.setGender(Gender.MALE);
        testPatient.setAddress("Tunis");
        testPatient.setRole(Role.PATIENT);
        testPatient.setStatus(UserStatus.ACTIVE);
        testPatient.setEmailVerified(false);
        testPatient.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
    }

    @Test
    void register_shouldCreatePatientAndMedicalRecord_whenValidRequest() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        UserDto result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("+21650123456");
        assertThat(result.getRoles()).contains("PATIENT");

        verify(patientRepository).save(patientCaptor.capture());
        Patient saved = patientCaptor.getValue();
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getRole()).isEqualTo(Role.PATIENT);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(patientServiceClient).createMedicalRecord(medicalRecordCaptor.capture());
        MedicalRecordRequest captured = medicalRecordCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(testPatient.getId());
    }

    @Test
    void register_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("email");

        verify(patientRepository, never()).save(any());
        verify(patientServiceClient, never()).createMedicalRecord(any());
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenPhoneAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("téléphone");

        verify(patientRepository, never()).save(any());
        verify(patientServiceClient, never()).createMedicalRecord(any());
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenCinAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(patientRepository.existsByCin(registerRequest.getCin())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CIN");

        verify(patientRepository, never()).save(any());
        verify(patientServiceClient, never()).createMedicalRecord(any());
    }

    @Test
    void login_byEmail_shouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testPatient));
        when(jwtService.generateAccessToken(testPatient)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testPatient)).thenReturn("refresh-token");
        when(jwtService.getExpiration()).thenReturn(900000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900000L);
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    }

    @Test
    void login_byCin_shouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setCin("12345678");
        request.setPassword("password123");

        when(patientRepository.findByCin(request.getCin())).thenReturn(Optional.of(testPatient));
        when(userRepository.findByEmail(testPatient.getEmail())).thenReturn(Optional.of(testPatient));
        when(jwtService.generateAccessToken(testPatient)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testPatient)).thenReturn("refresh-token");
        when(jwtService.getExpiration()).thenReturn(900000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testPatient.getEmail(), request.getPassword()));
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenCredentialsAreWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("wrong-password");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void refreshToken_withValidToken_shouldReturnNewAuthResponse() {
        String refreshToken = "valid-refresh-token";
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractEmail(refreshToken)).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testPatient));
        when(jwtService.generateAccessToken(testPatient)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testPatient)).thenReturn("new-refresh-token");
        when(jwtService.getExpiration()).thenReturn(900000L);

        AuthResponse response = authService.refreshToken(refreshToken);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refreshToken_withInvalidToken_shouldThrowIllegalArgumentException() {
        String refreshToken = "invalid-token";
        when(jwtService.isTokenValid(refreshToken)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalide");
    }

    @Test
    void getCurrentUser_whenFound_shouldReturnUserDto() {
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testPatient));

        UserDto result = authService.getCurrentUser(email);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
    }

    @Test
    void getCurrentUser_whenNotFound_shouldThrowException() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(email))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getAllActivePatients_shouldReturnOnlyActivePatients() {
        Patient activePatient1 = createPatient(1L, "active1@test.com", UserStatus.ACTIVE, "MALE");
        Patient activePatient2 = createPatient(2L, "active2@test.com", UserStatus.ACTIVE, "FEMALE");
        Patient inactivePatient = createPatient(3L, "inactive@test.com", UserStatus.INACTIVE, "MALE");

        when(patientRepository.findAll()).thenReturn(List.of(activePatient1, activePatient2, inactivePatient));

        List<PatientListDto> result = authService.getAllActivePatients();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PatientListDto::getEmail)
                .containsExactlyInAnyOrder("active1@test.com", "active2@test.com");
    }
    }

    @Test
    void getAllActiveDoctors_shouldReturnOnlyActiveDoctors() {
        Doctor activeDoctor1 = createDoctor(1L, "active1@test.com", UserStatus.ACTIVE);
        Doctor activeDoctor2 = createDoctor(2L, "active2@test.com", UserStatus.ACTIVE);
        Doctor suspendedDoctor = createDoctor(3L, "suspended@test.com", UserStatus.SUSPENDED);

        when(doctorRepository.findAll()).thenReturn(List.of(activeDoctor1, activeDoctor2, suspendedDoctor));

        List<DoctorListDto> result = authService.getAllActiveDoctors();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DoctorListDto::getEmail)
                .containsExactlyInAnyOrder("active1@test.com", "active2@test.com");
    }

    private Patient createPatient(Long id, String email, UserStatus status, String gender) {
        Patient p = new Patient();
        p.setId(id);
        p.setEmail(email);
        p.setFirstName("First" + id);
        p.setLastName("Last" + id);
        p.setPhone("+216" + id);
        p.setStatus(status);
        p.setRole(Role.PATIENT);
        p.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        if (gender != null) {
            p.setGender(Gender.valueOf(gender));
        }
        return p;
    }

    private Doctor createDoctor(Long id, String email, UserStatus status) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setEmail(email);
        d.setFirstName("First" + id);
        d.setLastName("Last" + id);
        d.setPhone("+216" + id);
        d.setStatus(status);
        d.setRole(Role.DOCTOR);
        d.setSpecialty("Cardiology");
        d.setHospital("Hospital " + id);
        d.setLicenseNumber("LIC" + id);
        d.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        return d;
    }
}
