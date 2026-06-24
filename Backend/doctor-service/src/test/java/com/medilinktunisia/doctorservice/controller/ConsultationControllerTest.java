package com.medilinktunisia.doctorservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.doctorservice.dto.ConsultationRequest;
import com.medilinktunisia.doctorservice.dto.ConsultationResponse;
import com.medilinktunisia.doctorservice.exception.GlobalExceptionHandler;
import com.medilinktunisia.doctorservice.security.JwtService;
import com.medilinktunisia.doctorservice.service.ConsultationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ConsultationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsultationService service;

    @MockBean
    private JwtService jwtService;

    private final Long doctorId = 1L;

    private ConsultationResponse createResponse(Long id, String status) {
        return ConsultationResponse.builder()
                .id(id)
                .patientId(10L)
                .doctorId(doctorId)
                .appointmentId(100L)
                .startTime(LocalDateTime.now())
                .status(status)
                .type("PRESENTIEL")
                .reason("Headache")
                .build();
    }

    @Test
    void getTodayConsultations_returns200() throws Exception {
        when(service.getTodayConsultations(doctorId)).thenReturn(List.of(createResponse(1L, "PENDING")));

        mockMvc.perform(get("/api/doctors/consultations/today")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllConsultations_returns200() throws Exception {
        when(service.getAllConsultations(doctorId)).thenReturn(List.of(createResponse(1L, "PENDING")));

        mockMvc.perform(get("/api/doctors/consultations")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllConsultations_withStatus_returnsFiltered() throws Exception {
        when(service.getConsultationsByStatus(doctorId, "IN_PROGRESS"))
                .thenReturn(List.of(createResponse(1L, "IN_PROGRESS")));

        mockMvc.perform(get("/api/doctors/consultations")
                        .param("status", "IN_PROGRESS")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void getConsultation_returns200() throws Exception {
        when(service.getConsultation(1L)).thenReturn(createResponse(1L, "PENDING"));

        mockMvc.perform(get("/api/doctors/consultations/1")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getConsultation_notFound_returns404() throws Exception {
        when(service.getConsultation(99L)).thenThrow(new RuntimeException("Consultation not found: 99"));

        mockMvc.perform(get("/api/doctors/consultations/99")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isNotFound());
    }

    @Test
    void startConsultation_returns201() throws Exception {
        ConsultationRequest request = new ConsultationRequest();
        request.setPatientId(10L);
        request.setAppointmentId(100L);
        request.setType("PRESENTIEL");
        request.setReason("Follow-up");

        when(service.startConsultation(eq(doctorId), any())).thenReturn(createResponse(1L, "IN_PROGRESS"));

        mockMvc.perform(post("/api/doctors/consultations")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void startConsultation_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/doctors/consultations")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateConsultation_returns200() throws Exception {
        ConsultationRequest request = new ConsultationRequest();
        request.setDiagnosis("Updated diagnosis");

        when(service.updateConsultation(eq(1L), eq(doctorId), any()))
                .thenReturn(createResponse(1L, "IN_PROGRESS"));

        mockMvc.perform(put("/api/doctors/consultations/1")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void completeConsultation_returns200() throws Exception {
        ConsultationRequest request = new ConsultationRequest();
        request.setDiagnosis("Final diagnosis");

        when(service.completeConsultation(eq(1L), eq(doctorId), any()))
                .thenReturn(createResponse(1L, "COMPLETED"));

        mockMvc.perform(put("/api/doctors/consultations/1/complete")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getConsultationsByPatient_returns200() throws Exception {
        when(service.getConsultationsByPatient(doctorId, 10L))
                .thenReturn(List.of(createResponse(1L, "COMPLETED"), createResponse(2L, "PENDING")));

        mockMvc.perform(get("/api/doctors/consultations/patient/10")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void cancelConsultation_returns204() throws Exception {
        doNothing().when(service).cancelConsultation(1L, doctorId);

        mockMvc.perform(delete("/api/doctors/consultations/1")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isNoContent());
    }
}
