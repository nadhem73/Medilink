package com.medilinktunisia.prescriptionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.prescriptionservice.dto.PrescriptionCreateRequest;
import com.medilinktunisia.prescriptionservice.dto.PrescriptionItemRequest;
import com.medilinktunisia.prescriptionservice.dto.PrescriptionResponse;
import com.medilinktunisia.prescriptionservice.exception.GlobalExceptionHandler;
import com.medilinktunisia.prescriptionservice.security.JwtService;
import com.medilinktunisia.prescriptionservice.service.PrescriptionService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrescriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private JwtService jwtService;

    private final Long doctorId = 1L;

    private PrescriptionResponse createResponse(Long id, String status) {
        return PrescriptionResponse.builder()
                .id(id)
                .consultationId(100L)
                .patientId(10L)
                .doctorId(doctorId)
                .status(status)
                .notes("Prescription de test")
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createPrescription_returns201() throws Exception {
        PrescriptionResponse response = createResponse(1L, "SOUMISE");
        when(prescriptionService.createPrescription(eq(doctorId), any())).thenReturn(response);

        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicamentId(1L);
        item.setMedicamentName("DOLIPRANE");
        item.setPosologie("1 comprimé 3 fois par jour");

        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setConsultationId(100L);
        request.setPatientId(10L);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/prescriptions")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SOUMISE"));
    }

    @Test
    void createPrescription_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/prescriptions")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPrescription_missingFields_returns400() throws Exception {
        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setConsultationId(100L);
        request.setItems(List.of());

        mockMvc.perform(post("/api/prescriptions")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPrescription_returns200() throws Exception {
        when(prescriptionService.getPrescription(1L)).thenReturn(createResponse(1L, "SOUMISE"));

        mockMvc.perform(get("/api/prescriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPrescription_notFound_returns404() throws Exception {
        when(prescriptionService.getPrescription(99L))
                .thenThrow(new RuntimeException("Prescription not found: 99"));

        mockMvc.perform(get("/api/prescriptions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPrescriptionByConsultation_returns200() throws Exception {
        when(prescriptionService.getPrescriptionByConsultation(100L))
                .thenReturn(createResponse(1L, "SOUMISE"));

        mockMvc.perform(get("/api/prescriptions/consultation/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultationId").value(100));
    }

    @Test
    void getPrescriptionByConsultation_notFound_returns400() throws Exception {
        when(prescriptionService.getPrescriptionByConsultation(99L))
                .thenThrow(new RuntimeException("No prescription found for consultation: 99"));

        mockMvc.perform(get("/api/prescriptions/consultation/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPrescriptionsByPatient_returns200() throws Exception {
        when(prescriptionService.getPrescriptionsByPatient(10L))
                .thenReturn(List.of(createResponse(1L, "SOUMISE"), createResponse(2L, "DISPENSEE")));

        mockMvc.perform(get("/api/prescriptions/patient/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPrescriptionsByPatient_empty_returns200() throws Exception {
        when(prescriptionService.getPrescriptionsByPatient(99L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/prescriptions/patient/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updatePrescription_returns200() throws Exception {
        when(prescriptionService.updatePrescription(eq(1L), eq(doctorId), any()))
                .thenReturn(createResponse(1L, "SOUMISE"));

        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicamentId(1L);
        item.setMedicamentName("DOLIPRANE");
        item.setPosologie("1 comprimé 3 fois par jour");

        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setConsultationId(100L);
        request.setPatientId(10L);
        request.setItems(List.of(item));

        mockMvc.perform(put("/api/prescriptions/1")
                        .requestAttr("userId", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelPrescription_returns204() throws Exception {
        doNothing().when(prescriptionService).cancelPrescription(1L, doctorId);

        mockMvc.perform(delete("/api/prescriptions/1")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelPrescription_unauthorized_returns403() throws Exception {
        doThrow(new RuntimeException("Unauthorized: prescription belongs to another doctor"))
                .when(prescriptionService).cancelPrescription(1L, doctorId);

        mockMvc.perform(delete("/api/prescriptions/1")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelPrescription_conflict_returns409() throws Exception {
        doThrow(new RuntimeException("Stock insuffisant pour : DOLIPRANE"))
                .when(prescriptionService).cancelPrescription(1L, doctorId);

        mockMvc.perform(delete("/api/prescriptions/1")
                        .requestAttr("userId", doctorId))
                .andExpect(status().isConflict());
    }
}
