package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.dto.request.ForgotPasswordRequest;
import com.medilinktunisia.authservice.dto.request.ResetPasswordRequest;
import com.medilinktunisia.authservice.model.entity.Doctor;
import com.medilinktunisia.authservice.model.entity.PasswordResetToken;
import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.repository.DoctorRepository;
import com.medilinktunisia.authservice.repository.PasswordResetTokenRepository;
import com.medilinktunisia.authservice.repository.PharmacyRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PharmacyRepository pharmacyRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    private PasswordResetService service;

    @Captor private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(userRepository, doctorRepository,
                pharmacyRepository, tokenRepository, passwordEncoder, emailService);
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(service, "expirationMinutes", 60L);
    }

    @Test
    void requestReset_forPatientByEmail_createsTokenAndSendsEmail() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setEmail("john@example.com");
        patient.setFirstName("John");
        patient.setLastName("Doe");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setRole("patient");
        request.setEmail("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(patient));

        service.requestReset(request);

        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken token = tokenCaptor.getValue();
        assertThat(token.getUserId()).isEqualTo(1L);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.isUsable()).isTrue();
        verify(emailService).sendPasswordResetEmail(eq("john@example.com"), eq("John Doe"), anyString());
    }

    @Test
    void requestReset_forDoctorByLicenseNumber_createsTokenAndSendsEmail() {
        Doctor doctor = new Doctor();
        doctor.setId(2L);
        doctor.setEmail("dr@example.com");
        doctor.setFirstName("Dr");
        doctor.setLicenseNumber("LIC123");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setRole("doctor");
        request.setLicenseNumber("LIC123");

        when(doctorRepository.findByLicenseNumber("LIC123")).thenReturn(Optional.of(doctor));

        service.requestReset(request);

        verify(tokenRepository).save(any());
        verify(emailService).sendPasswordResetEmail(eq("dr@example.com"), anyString(), anyString());
    }

    @Test
    void resetPassword_validToken_updatesPassword() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(1L);
        resetToken.setToken("valid-token");
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setPassword("old-encoded");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("newPass123");

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(passwordEncoder.encode("newPass123")).thenReturn("new-encoded");

        service.resetPassword(request);

        assertThat(patient.getPassword()).isEqualTo("new-encoded");
        assertThat(resetToken.isUsed()).isTrue();
        verify(tokenRepository, times(1)).save(resetToken);
    }

    @Test
    void resetPassword_invalidToken_throws() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("newPass123");

        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
