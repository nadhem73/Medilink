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
import com.medilinktunisia.authservice.model.enums.Gender;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import com.medilinktunisia.authservice.repository.DoctorRepository;
import com.medilinktunisia.authservice.repository.PatientRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import com.medilinktunisia.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PatientServiceClient patientServiceClient;

    @Mock
    private EmailVerificationService emailVerificationService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, patientRepository, doctorRepository,
                passwordEncoder, jwtService, authenticationManager,
                patientServiceClient, emailVerificationService
        );
    }

    @Test
    void register_shouldCreatePatientWithMedicalRecord() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("+21650123456");
        request.setCin("12345678");
        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.of(1990, 1, 1));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(patientRepository.existsByCin(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        UserDto result = authService.register(request);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(patientServiceClient, times(1)).createMedicalRecord(any());
    }

    @Test
    void register_withExistingEmail_shouldThrow() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(request));
    }

    @Test
    void login_withEmail_shouldSucceed() {
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

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void login_badPassword_shouldThrow() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void refreshToken_shouldReturnNewTokens() {
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

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
    }

    @Test
    void getCurrentUser_shouldReturnUserDto() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setEmail("john@example.com");
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setRole(Role.PATIENT);
        patient.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(patient));

        UserDto result = authService.getCurrentUser("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
    }

    @Test
    void getAllActiveDoctors_shouldReturnOnlyActiveDoctors() {
        Doctor activeDoctor = new Doctor();
        activeDoctor.setId(1L);
        activeDoctor.setFirstName("Dr");
        activeDoctor.setLastName("Smith");
        activeDoctor.setEmail("dr.smith@test.com");
        activeDoctor.setStatus(UserStatus.ACTIVE);
        activeDoctor.setRole(Role.DOCTOR);

        Doctor inactiveDoctor = new Doctor();
        inactiveDoctor.setId(2L);
        inactiveDoctor.setFirstName("Dr");
        inactiveDoctor.setLastName("Inactive");
        inactiveDoctor.setEmail("dr.inactive@test.com");
        inactiveDoctor.setStatus(UserStatus.PENDING);
        inactiveDoctor.setRole(Role.DOCTOR);

        when(doctorRepository.findAll()).thenReturn(List.of(activeDoctor, inactiveDoctor));

        List<DoctorListDto> result = authService.getAllActiveDoctors();

        assertEquals(1, result.size());
        assertEquals("Dr Smith", result.get(0).getFirstName() + " " + result.get(0).getLastName());
    }

    @Test
    void getAllActivePatients_shouldReturnOnlyActivePatients() {
        Patient activePatient = new Patient();
        activePatient.setId(1L);
        activePatient.setFirstName("John");
        activePatient.setLastName("Active");
        activePatient.setEmail("john@test.com");
        activePatient.setStatus(UserStatus.ACTIVE);
        activePatient.setRole(Role.PATIENT);

        Patient inactivePatient = new Patient();
        inactivePatient.setId(2L);
        inactivePatient.setFirstName("Jane");
        inactivePatient.setLastName("Inactive");
        inactivePatient.setEmail("jane@test.com");
        inactivePatient.setStatus(UserStatus.PENDING);
        inactivePatient.setRole(Role.PATIENT);

        when(patientRepository.findAll()).thenReturn(List.of(activePatient, inactivePatient));

        List<PatientListDto> result = authService.getAllActivePatients();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }
}
