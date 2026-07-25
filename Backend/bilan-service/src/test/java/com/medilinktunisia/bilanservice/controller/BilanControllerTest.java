package com.medilinktunisia.bilanservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.bilanservice.dto.*;
import com.medilinktunisia.bilanservice.BilanServiceApplication;
import com.medilinktunisia.bilanservice.security.JwtService;
import com.medilinktunisia.bilanservice.service.BilanService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = BilanServiceApplication.class)
@AutoConfigureMockMvc
class BilanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BilanController bilanController;

    private final Authentication auth = new UsernamePasswordAuthenticationToken(
            1L, null, List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));

    @MockBean
    private BilanService bilanService;

    @MockBean
    private JwtService jwtService;

    private final UUID bilanId = UUID.randomUUID();

    @Test
    void scanBilan_shouldReturnOk() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test-image".getBytes());
        ScanBilanResponse response = ScanBilanResponse.builder()
                .id(bilanId)
                .typeBilan("Bilan sanguin")
                .status("PENDING")
                .build();

        when(bilanService.scanBilan(any(), anyLong())).thenReturn(response);

        mockMvc.perform(multipart("/api/bilans/scan")
                        .file(image)
                        .header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bilanId.toString()))
                .andExpect(jsonPath("$.typeBilan").value("Bilan sanguin"));
    }

    @Test
    void scanBilan_shouldReturn422WhenOcrFails() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test-image".getBytes());

        when(bilanService.scanBilan(any(), anyLong()))
                .thenThrow(new IllegalArgumentException("OCR impossible"));

        mockMvc.perform(multipart("/api/bilans/scan")
                        .file(image)
                        .header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("OCR impossible"));
    }

    @Test
    void scanBilan_shouldReturn500OnInternalError() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test-image".getBytes());

        when(bilanService.scanBilan(any(), anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(multipart("/api/bilans/scan")
                        .file(image)
                        .header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Erreur lors du scan : Unexpected error"));
    }

    @Test
    void getBilans_shouldReturnPage() {
        BilanSummary summary = BilanSummary.builder()
                .id(bilanId)
                .typeBilan("Bilan sanguin")
                .dateBilan(LocalDate.of(2026, 6, 15))
                .status("PENDING")
                .resultCount(3)
                .abnormalCount(1)
                .patientId(1L)
                .build();
        Page<BilanSummary> page = new PageImpl<>(List.of(summary));

        when(bilanService.getPatientBilans(anyLong(), anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<Page<BilanSummary>> response = bilanController.getBilans(0, 20, auth);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertEquals("Bilan sanguin", response.getBody().getContent().getFirst().getTypeBilan());
        assertEquals(3, response.getBody().getContent().getFirst().getResultCount());
    }

    @Test
    void getBilan_shouldReturnOk() throws Exception {
        ScanBilanResponse response = ScanBilanResponse.builder()
                .id(bilanId)
                .typeBilan("Bilan sanguin")
                .status("PENDING")
                .build();

        when(bilanService.getBilan(any(UUID.class), anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/bilans/{id}", bilanId).header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bilanId.toString()));
    }

    @Test
    void getBilan_shouldReturn404WhenNotFound() throws Exception {
        when(bilanService.getBilan(any(UUID.class), anyLong()))
                .thenThrow(new EntityNotFoundException("Bilan introuvable"));

        mockMvc.perform(get("/api/bilans/{id}", bilanId).header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Bilan introuvable"));
    }

    @Test
    void getBilan_shouldReturn403WhenAccessDenied() throws Exception {
        when(bilanService.getBilan(any(UUID.class), anyLong()))
                .thenThrow(new SecurityException("Accès refusé"));

        mockMvc.perform(get("/api/bilans/{id}", bilanId).header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Accès refusé"));
    }

    @Test
    void confirmBilan_shouldReturnOk() throws Exception {
        ScanBilanResponse response = ScanBilanResponse.builder()
                .id(bilanId)
                .status("CONFIRMED")
                .build();

        when(bilanService.confirmBilan(any(UUID.class), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/bilans/{id}/confirm", bilanId).header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void assignDoctor_shouldReturnOk() throws Exception {
        ScanBilanResponse response = ScanBilanResponse.builder()
                .id(bilanId)
                .doctorId(2L)
                .build();

        when(bilanService.assignDoctor(any(UUID.class), anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/bilans/{id}/assign-doctor", bilanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorId\": 2}")
                        .header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(2));
    }

    @Test
    void updateResult_shouldReturnOk() throws Exception {
        ScanBilanResponse response = ScanBilanResponse.builder()
                .id(bilanId)
                .build();

        when(bilanService.updateResult(any(UUID.class), anyLong(), any(UpdateResultRequest.class), anyLong()))
                .thenReturn(response);

        mockMvc.perform(put("/api/bilans/{bilanId}/results/{resultId}", bilanId, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valeur\": \"6.5\"}")
                        .header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void getDoctorBilans_shouldReturnPage() {
        Page<BilanSummary> page = new PageImpl<>(List.of());
        when(bilanService.getDoctorBilans(anyLong(), anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<Page<BilanSummary>> response = bilanController.getDoctorBilans(0, 20, auth);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    }

    @Test
    void getBilanForDoctor_shouldReturnOk() throws Exception {
        ScanBilanResponse response = ScanBilanResponse.builder()
                .id(bilanId)
                .build();

        when(bilanService.getBilanForDoctor(any(UUID.class), anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/bilans/doctor/{id}", bilanId).header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void updateReviewStatus_shouldReturnOk() throws Exception {
        ScanBilanResponse response = ScanBilanResponse.builder()
                .id(bilanId)
                .reviewStatus("LU")
                .build();

        when(bilanService.updateReviewStatus(any(UUID.class), anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(patch("/api/bilans/doctor/{id}/review", bilanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewStatus\": \"LU\"}")
                        .header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewStatus").value("LU"));
    }

    @Test
    void deleteBilan_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/bilans/{id}", bilanId).header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBilan_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Bilan introuvable"))
                .when(bilanService).deleteBilan(any(UUID.class), anyLong());

        mockMvc.perform(delete("/api/bilans/{id}", bilanId).header("X-User-Id", "1").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Bilan introuvable"));
    }

    @Test
    void endpoints_shouldReturn403WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/bilans"))
                .andExpect(status().isForbidden());
    }
}
