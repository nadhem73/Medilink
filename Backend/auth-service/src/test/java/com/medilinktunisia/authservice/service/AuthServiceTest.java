package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.client.PatientServiceClient;
import com.medilinktunisia.authservice.dto.request.LoginRequest;
import com.medilinktunisia.authservice.dto.request.RegisterRequest;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.model.entity.Patient;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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
    void register_shouldCreatePatient() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setGender(Gender.MALE);
        request.setBirthDate(LocalDate.of(1990, 1, 1));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        var result = authService.register(request);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void register_withExistingEmail_shouldThrow() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(com.medilinktunisia.authservice.exception.EmailAlreadyExistsException.class,
                () -> authService.register(request));
    }

    @Test
    void getAllActiveDoctors_shouldReturnOnlyActiveDoctors() {
        var activeDoctor = new com.medilinktunisia.authservice.model.entity.Doctor();
        activeDoctor.setId(1L);
        activeDoctor.setFirstName("Dr");
        activeDoctor.setLastName("Smith");
        activeDoctor.setEmail("dr.smith@test.com");
        activeDoctor.setStatus(UserStatus.ACTIVE);
        activeDoctor.setRole(Role.DOCTOR);

        var inactiveDoctor = new com.medilinktunisia.authservice.model.entity.Doctor();
        inactiveDoctor.setId(2L);
        inactiveDoctor.setFirstName("Dr");
        inactiveDoctor.setLastName("Inactive");
        inactiveDoctor.setEmail("dr.inactive@test.com");
        inactiveDoctor.setStatus(UserStatus.PENDING);
        inactiveDoctor.setRole(Role.DOCTOR);

        when(doctorRepository.findAll()).thenReturn(java.util.List.of(activeDoctor, inactiveDoctor));

        var result = authService.getAllActiveDoctors();

        assertEquals(1, result.size());
        assertEquals("Dr Smith", result.get(0).getFirstName() + " " + result.get(0).getLastName());
    }

    @Test
    void getAllActivePatients_shouldReturnOnlyActivePatients() {
        var activePatient = new Patient();
        activePatient.setId(1L);
        activePatient.setFirstName("John");
        activePatient.setLastName("Active");
        activePatient.setEmail("john@test.com");
        activePatient.setStatus(UserStatus.ACTIVE);
        activePatient.setRole(Role.PATIENT);

        var inactivePatient = new Patient();
        inactivePatient.setId(2L);
        inactivePatient.setFirstName("Jane");
        inactivePatient.setLastName("Inactive");
        inactivePatient.setEmail("jane@test.com");
        inactivePatient.setStatus(UserStatus.PENDING);
        inactivePatient.setRole(Role.PATIENT);

        when(patientRepository.findAll()).thenReturn(java.util.List.of(activePatient, inactivePatient));

        var result = authService.getAllActivePatients();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }
}
