package com.medilinktunisia.authservice.controller;

import com.medilinktunisia.authservice.dto.request.ForgotPasswordRequest;
import com.medilinktunisia.authservice.dto.request.LoginRequest;
import com.medilinktunisia.authservice.dto.request.OtpVerificationRequest;
import com.medilinktunisia.authservice.dto.request.RefreshTokenRequest;
import com.medilinktunisia.authservice.dto.request.RegisterRequest;
import com.medilinktunisia.authservice.dto.request.ResetPasswordRequest;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.dto.response.DoctorListDto;
import com.medilinktunisia.authservice.dto.response.PatientListDto;
import com.medilinktunisia.authservice.dto.response.UserDto;
import com.medilinktunisia.authservice.security.CustomUserDetailsService;
import com.medilinktunisia.authservice.security.JwtService;
import com.medilinktunisia.authservice.security.SecurityConfig;
import com.medilinktunisia.authservice.service.AuthService;
import com.medilinktunisia.authservice.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private PasswordResetService passwordResetService;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_shouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");
        request.setPhone("+21650123456");
        request.setCin("12345678");
        request.setPassword("password123");

        when(authService.register(any(RegisterRequest.class))).thenReturn(UserDto.builder().build());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void login_shouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("password123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(900000L)
                .user(UserDto.builder().build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void refresh_shouldReturn200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900000L)
                .build();

        when(authService.refreshToken(anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    @WithMockUser(username = "john@test.com")
    void me_shouldReturn200() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("john@test.com")
                .firstName("John")
                .lastName("Doe")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.getCurrentUser("john@test.com")).thenReturn(userDto);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    @WithMockUser
    void getAllDoctors_shouldReturn200() throws Exception {
        DoctorListDto doctor1 = DoctorListDto.builder()
                .id(1L).firstName("Doc1").lastName("Test").email("doc1@test.com").build();
        DoctorListDto doctor2 = DoctorListDto.builder()
                .id(2L).firstName("Doc2").lastName("Test").email("doc2@test.com").build();

        when(authService.getAllActiveDoctors()).thenReturn(List.of(doctor1, doctor2));

        mockMvc.perform(get("/api/auth/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("doc1@test.com"));
    }

    @Test
    @WithMockUser
    void getAllPatients_shouldReturn200() throws Exception {
        PatientListDto patient1 = PatientListDto.builder()
                .id(1L).firstName("Pat1").lastName("Test").email("pat1@test.com").build();
        PatientListDto patient2 = PatientListDto.builder()
                .id(2L).firstName("Pat2").lastName("Test").email("pat2@test.com").build();

        when(authService.getAllActivePatients()).thenReturn(List.of(patient1, patient2));

        mockMvc.perform(get("/api/auth/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("pat1@test.com"));
    }

    @Test
    @WithMockUser(username = "john@test.com")
    void requestEmailVerification_shouldReturn200() throws Exception {
        doNothing().when(authService).requestEmailVerification("john@test.com");

        mockMvc.perform(post("/api/auth/verify-email/request")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "john@test.com")
    void verifyEmail_shouldReturn200() throws Exception {
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setCode("123456");

        UserDto userDto = UserDto.builder()
                .id(1L).email("john@test.com").isEmailVerified(true).build();

        when(authService.verifyEmail(anyString(), anyString())).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/verify-email/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    void forgotPassword_shouldReturn200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setRole("patient");
        request.setEmail("john@test.com");

        doNothing().when(passwordResetService).requestReset(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_shouldReturn200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("newpassword123");

        doNothing().when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
