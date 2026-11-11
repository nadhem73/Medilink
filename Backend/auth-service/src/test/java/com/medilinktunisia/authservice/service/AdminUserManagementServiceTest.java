package com.medilinktunisia.authservice.service;

import com.medilinktunisia.authservice.client.PatientServiceClient;
import com.medilinktunisia.authservice.dto.request.AdminUserActionRequest;
import com.medilinktunisia.authservice.dto.request.LoginRequest;
import com.medilinktunisia.authservice.dto.response.AdminUserDto;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.exception.AccountStatusException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests dédiés à la gestion des utilisateurs par l'admin et à la politique de
 * statut à la connexion (suspension / désactivation / réactivation automatique).
 * Fichier séparé de {@code AuthServiceTest} pour limiter les conflits de merge.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserManagementServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PharmacyRepository pharmacyRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PatientServiceClient patientServiceClient;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks private AuthService authService;

    @Captor private ArgumentCaptor<User> userCaptor;

    // ---------------------------------------------------------------------
    // getAllUsers()
    // ---------------------------------------------------------------------

    @Test
    void getAllUsers_shouldExcludeAdmins() {
        Patient patient = patient(1L, "patient@test.com", UserStatus.ACTIVE);
        Doctor doctor = doctor(2L, "doc@test.com", UserStatus.ACTIVE);
        Admin admin = new Admin();
        admin.setId(3L);
        admin.setEmail("admin@medilink.tn");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        when(userRepository.findAll()).thenReturn(List.of(patient, doctor, admin));

        List<AdminUserDto> result = authService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AdminUserDto::getEmail)
                .containsExactlyInAnyOrder("patient@test.com", "doc@test.com");
        assertThat(result).extracting(AdminUserDto::getRole).doesNotContain("ADMIN");
    }

    @Test
    void getAllUsers_shouldMapDoctorAndPharmacySpecificFields() {
        Doctor doctor = doctor(1L, "doc@test.com", UserStatus.ACTIVE);
        doctor.setSpecialty("Cardiologie");
        doctor.setLicenseNumber("DOC-123");

        Pharmacy pharmacy = pharmacy(2L, "pharma@test.com", UserStatus.SUSPENDED);
        pharmacy.setPharmacyName("Pharmacie El Manar");
        pharmacy.setLicenseNumber("PH-999");

        when(userRepository.findAll()).thenReturn(List.of(doctor, pharmacy));

        List<AdminUserDto> result = authService.getAllUsers();

        AdminUserDto doc = result.stream().filter(u -> u.getRole().equals("DOCTOR")).findFirst().orElseThrow();
        assertThat(doc.getSpecialty()).isEqualTo("Cardiologie");
        assertThat(doc.getLicenseNumber()).isEqualTo("DOC-123");

        AdminUserDto pha = result.stream().filter(u -> u.getRole().equals("PHARMACY")).findFirst().orElseThrow();
        assertThat(pha.getPharmacyName()).isEqualTo("Pharmacie El Manar");
        assertThat(pha.getLicenseNumber()).isEqualTo("PH-999");
    }

    // ---------------------------------------------------------------------
    // updateUserStatus()
    // ---------------------------------------------------------------------

    @Test
    void updateUserStatus_shouldSetSuspendedWithDate() {
        Doctor doctor = doctor(1L, "doc@test.com", UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(doctor));

        LocalDateTime until = LocalDateTime.now().plusDays(7);
        AdminUserActionRequest req = new AdminUserActionRequest();
        req.setStatus(UserStatus.SUSPENDED);
        req.setSuspendUntil(until);

        authService.updateUserStatus(1L, req);

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(saved.getSuspendUntil()).isEqualTo(until);
    }

    @Test
    void updateUserStatus_shouldClearSuspendUntil_whenActivating() {
        Doctor doctor = doctor(1L, "doc@test.com", UserStatus.SUSPENDED);
        doctor.setSuspendUntil(LocalDateTime.now().plusDays(3));
        when(userRepository.findById(1L)).thenReturn(Optional.of(doctor));

        AdminUserActionRequest req = new AdminUserActionRequest();
        req.setStatus(UserStatus.ACTIVE);

        authService.updateUserStatus(1L, req);

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getSuspendUntil()).isNull();
    }

    @Test
    void updateUserStatus_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AdminUserActionRequest req = new AdminUserActionRequest();
        req.setStatus(UserStatus.INACTIVE);

        assertThatThrownBy(() -> authService.updateUserStatus(99L, req))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // login() — politique de statut du compte
    // ---------------------------------------------------------------------

    @Test
    void login_shouldThrowAccountStatus_whenSuspendedAndNotExpired() {
        Doctor doctor = doctor(1L, "doc@test.com", UserStatus.SUSPENDED);
        doctor.setSuspendUntil(LocalDateTime.now().plusDays(2));
        when(userRepository.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));

        LoginRequest request = new LoginRequest();
        request.setEmail("doc@test.com");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountStatusException.class)
                .satisfies(ex -> assertThat(((AccountStatusException) ex).getStatus())
                        .isEqualTo(UserStatus.SUSPENDED));

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void login_shouldThrowAccountStatus_whenInactive() {
        Pharmacy pharmacy = pharmacy(1L, "pharma@test.com", UserStatus.INACTIVE);
        when(userRepository.findByEmail("pharma@test.com")).thenReturn(Optional.of(pharmacy));

        LoginRequest request = new LoginRequest();
        request.setEmail("pharma@test.com");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountStatusException.class)
                .satisfies(ex -> assertThat(((AccountStatusException) ex).getStatus())
                        .isEqualTo(UserStatus.INACTIVE));

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void login_shouldAutoReactivate_whenSuspensionExpired() {
        Doctor doctor = doctor(1L, "doc@test.com", UserStatus.SUSPENDED);
        doctor.setSuspendUntil(LocalDateTime.now().minusMinutes(1)); // échéance dépassée
        when(userRepository.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));
        when(jwtService.generateAccessToken(doctor)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(doctor)).thenReturn("refresh-token");
        when(jwtService.getExpiration()).thenReturn(900000L);

        LoginRequest request = new LoginRequest();
        request.setEmail("doc@test.com");
        request.setPassword("password");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getSuspendUntil()).isNull();
    }

    @Test
    void login_shouldSucceed_whenActive() {
        Doctor doctor = doctor(1L, "doc@test.com", UserStatus.ACTIVE);
        when(userRepository.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));
        when(jwtService.generateAccessToken(doctor)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(doctor)).thenReturn("refresh-token");
        when(jwtService.getExpiration()).thenReturn(900000L);

        LoginRequest request = new LoginRequest();
        request.setEmail("doc@test.com");
        request.setPassword("password");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // Fabriques
    // ---------------------------------------------------------------------

    private Patient patient(Long id, String email, UserStatus status) {
        Patient p = new Patient();
        p.setId(id);
        p.setEmail(email);
        p.setFirstName("First" + id);
        p.setLastName("Last" + id);
        p.setPhone("+216" + id);
        p.setRole(Role.PATIENT);
        p.setStatus(status);
        p.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        return p;
    }

    private Doctor doctor(Long id, String email, UserStatus status) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setEmail(email);
        d.setFirstName("First" + id);
        d.setLastName("Last" + id);
        d.setPhone("+216" + id);
        d.setRole(Role.DOCTOR);
        d.setStatus(status);
        d.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        return d;
    }

    private Pharmacy pharmacy(Long id, String email, UserStatus status) {
        Pharmacy ph = new Pharmacy();
        ph.setId(id);
        ph.setEmail(email);
        ph.setFirstName("First" + id);
        ph.setLastName("Last" + id);
        ph.setPhone("+216" + id);
        ph.setRole(Role.PHARMACY);
        ph.setStatus(status);
        ph.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        return ph;
    }
}
