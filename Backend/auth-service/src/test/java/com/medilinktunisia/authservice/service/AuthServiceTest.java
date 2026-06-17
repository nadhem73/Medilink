package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.client.PatientServiceClient;
import com.medilinktunisia.authservice.dto.request.LoginRequest;
import com.medilinktunisia.authservice.dto.request.RegisterRequest;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.dto.response.DoctorListDto;
import com.medilinktunisia.authservice.dto.response.PatientListDto;
import com.medilinktunisia.authservice.dto.response.UserDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    private AuthService authService;

    @Captor private ArgumentCaptor<Patient> patientCaptor;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, patientRepository, doctorRepository,
                passwordEncoder, jwtService, authenticationManager,
                patientServiceClient, emailVerificationService);
    }

    @Test
    void register_createsPatientAndMedicalRecord() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPhone("12345678");
        request.setCin("12345678");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("12345678")).thenReturn(false);
        when(patientRepository.existsByCin("12345678")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        Patient savedPatient = new Patient();
        savedPatient.setId(1L);
        savedPatient.setEmail("john@example.com");
        savedPatient.setFirstName("John");
        savedPatient.setLastName("Doe");
        savedPatient.setRole(Role.PATIENT);
        savedPatient.setStatus(UserStatus.ACTIVE);

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        authService.register(request);

        verify(patientRepository).save(any(Patient.class));
        verify(patientServiceClient).createMedicalRecord(any());
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withEmail_succeeds() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        Patient user = new Patient();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword("encoded");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("john@example.com", "password123"));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_badPassword_throws() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_returnsNewTokens() {
        String refreshToken = "valid-refresh-token";
        Patient user = new Patient();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);

        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractEmail(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtService.getExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.refreshToken(refreshToken);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void getCurrentUser_returnsUserDto() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setEmail("john@example.com");
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setRole(Role.PATIENT);
        patient.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(patient));

        UserDto result = authService.getCurrentUser("john@example.com");

        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void getAllActiveDoctors_returnsList() {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("Dr");
        doctor.setLastName("Smith");
        doctor.setSpecialty("Cardiology");
        doctor.setStatus(UserStatus.ACTIVE);

        when(doctorRepository.findAll()).thenReturn(List.of(doctor));

        List<DoctorListDto> result = authService.getAllActiveDoctors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Dr");
    }

    @Test
    void getAllActivePatients_returnsList() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setStatus(UserStatus.ACTIVE);

        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientListDto> result = authService.getAllActivePatients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
    }
}
