package com.medilinktunisia.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.authservice.dto.request.*;
import com.medilinktunisia.authservice.dto.response.AuthResponse;
import com.medilinktunisia.authservice.dto.response.DoctorListDto;
import com.medilinktunisia.authservice.dto.response.PatientListDto;
import com.medilinktunisia.authservice.dto.response.UserDto;
import com.medilinktunisia.authservice.security.CustomUserDetailsService;
import com.medilinktunisia.authservice.security.JwtService;
import com.medilinktunisia.authservice.service.AuthService;
import com.medilinktunisia.authservice.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPhone("12345678");
        request.setCin("12345678");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .build();
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_returns200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        AuthResponse response = AuthResponse.builder()
                .accessToken("new-token")
                .refreshToken("new-refresh")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .build();
        when(authService.refreshToken("refresh-token")).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-token"));
    }

    @Test
    void getAllDoctors_returns200() throws Exception {
        DoctorListDto doctor = DoctorListDto.builder()
                .id(1L).firstName("Dr").lastName("Smith").build();
        when(authService.getAllActiveDoctors()).thenReturn(List.of(doctor));

        mockMvc.perform(get("/api/auth/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllPatients_returns200() throws Exception {
        PatientListDto patient = PatientListDto.builder()
                .id(1L).firstName("John").lastName("Doe").build();
        when(authService.getAllActivePatients()).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/auth/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void forgotPassword_returns200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setRole("patient");
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_returns200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token");
        request.setNewPassword("newPass123");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
