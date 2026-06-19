package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.model.entity.EmailVerificationOtp;
import com.medilinktunisia.authservice.model.entity.User;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import com.medilinktunisia.authservice.repository.EmailVerificationOtpRepository;
import com.medilinktunisia.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationOtpRepository otpRepository;
    @Mock private EmailService emailService;

    private EmailVerificationService service;

    @Captor private ArgumentCaptor<EmailVerificationOtp> otpCaptor;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(userRepository, otpRepository, emailService);
    }

    @Test
    void requestEmailVerification_generatesOtpAndSendsEmail() {
        User user = new User() {
            { setId(1L); setEmail("john@example.com"); setFirstName("John"); setLastName("Doe"); }
        };
        user.setEmailVerified(false);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        service.requestEmailVerification("john@example.com");

        verify(otpRepository).save(otpCaptor.capture());
        EmailVerificationOtp saved = otpCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getCode()).hasSize(6);
        verify(emailService).sendEmailVerificationOtp(eq("john@example.com"), eq("John Doe"), anyString());
    }

    @Test
    void verifyEmail_validCode_marksEmailVerified() {
        User user = new User() {
            { setId(1L); setEmail("john@example.com"); setStatus(UserStatus.PENDING); }
        };
        user.setEmailVerified(false);

        EmailVerificationOtp otp = new EmailVerificationOtp();
        otp.setUserId(1L);
        otp.setCode("123456");
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndCode(1L, "123456")).thenReturn(Optional.of(otp));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.verifyEmail("john@example.com", "123456");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(otpRepository).save(otpCaptor.capture());
        assertThat(otpCaptor.getValue().isUsed()).isTrue();
    }

    @Test
    void verifyEmail_invalidCode_throws() {
        User user = new User() {
            { setId(1L); setEmail("john@example.com"); }
        };

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndCode(1L, "000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmail("john@example.com", "000000"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void verifyEmail_expiredCode_throws() {
        User user = new User() {
            { setId(1L); setEmail("john@example.com"); }
        };

        EmailVerificationOtp otp = new EmailVerificationOtp();
        otp.setUserId(1L);
        otp.setCode("123456");
        otp.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndCode(1L, "123456")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.verifyEmail("john@example.com", "123456"))
                .isInstanceOf(BadCredentialsException.class);
    }
}
