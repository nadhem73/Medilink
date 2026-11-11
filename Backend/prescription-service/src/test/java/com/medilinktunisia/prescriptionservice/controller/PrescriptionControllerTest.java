package com.medilinktunisia.prescriptionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.prescriptionservice.dto.PickupCodeResponse;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
                .thenReturn(Optional.of(createResponse(1L, "SOUMISE")));

        mockMvc.perform(get("/api/prescriptions/consultation/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultationId").value(100));
    }

    @Test
    void getPrescriptionByConsultation_notFound_returns204() throws Exception {
        when(prescriptionService.getPrescriptionByConsultation(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/prescriptions/consultation/99"))
                .andExpect(status().isNoContent());
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

    // ── updateStatus endpoint ──────────────────────────────────────

    @Test
    void updateStatus_returns200() throws Exception {
        PrescriptionResponse response = createResponse(1L, "EN_PREPARATION");
        when(prescriptionService.updateStatus(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/prescriptions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EN_PREPARATION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_PREPARATION"));
    }

    @Test
    void updateStatus_invalidStatus_returns400() throws Exception {
        when(prescriptionService.updateStatus(eq(1L), any()))
                .thenThrow(new RuntimeException("Only submitted prescriptions can be put in preparation"));

        mockMvc.perform(put("/api/prescriptions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PREPAREE\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── assign-pharmacy endpoint ───────────────────────────────────

    @Test
    void assignPharmacy_returns200() throws Exception {
        PrescriptionResponse response = createResponse(1L, "SOUMISE");
        response.setPharmacieId(5L);
        when(prescriptionService.assignToPharmacy(1L, 5L)).thenReturn(response);

        mockMvc.perform(put("/api/prescriptions/1/assign-pharmacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pharmacyId\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pharmacieId").value(5));
    }

    @Test
    void assignPharmacy_alreadyAssigned_returns400() throws Exception {
        when(prescriptionService.assignToPharmacy(1L, 5L))
                .thenThrow(new RuntimeException("Pharmacy already assigned to this prescription"));

        mockMvc.perform(put("/api/prescriptions/1/assign-pharmacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pharmacyId\":5}"))
                .andExpect(status().isBadRequest());
    }

    // ── getByPharmacy endpoint ─────────────────────────────────────

    @Test
    void getByPharmacy_returns200() throws Exception {
        when(prescriptionService.getPrescriptionsByPharmacy(5L))
                .thenReturn(List.of(createResponse(1L, "SOUMISE"), createResponse(2L, "EN_PREPARATION")));

        mockMvc.perform(get("/api/prescriptions/pharmacy/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByPharmacy_empty_returns200() throws Exception {
        when(prescriptionService.getPrescriptionsByPharmacy(99L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/prescriptions/pharmacy/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── getAllPrescriptions endpoint ───────────────────────────────

    @Test
    void getAllPrescriptions_returns200() throws Exception {
        when(prescriptionService.getAllPrescriptions())
                .thenReturn(List.of(createResponse(1L, "SOUMISE"), createResponse(2L, "DISPENSEE")));

        mockMvc.perform(get("/api/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllPrescriptions_empty_returns200() throws Exception {
        when(prescriptionService.getAllPrescriptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── storePickupCode endpoint ───────────────────────────────────

    @Test
    void storePickupCode_returns200() throws Exception {
        PickupCodeResponse response = PickupCodeResponse.builder()
                .id(1L).prescriptionId(1L).code("654321").used(false).build();
        when(prescriptionService.storePickupCode(eq(1L), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/prescriptions/1/pickup-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("654321"));
    }

    @Test
    void storePickupCode_alreadyExists_returns400() throws Exception {
        when(prescriptionService.storePickupCode(eq(1L), anyString()))
                .thenThrow(new RuntimeException("Pickup code already exists for this prescription"));

        mockMvc.perform(post("/api/prescriptions/1/pickup-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"654321\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── getPickupCode endpoint ─────────────────────────────────────

    @Test
    void getPickupCode_returns200() throws Exception {
        PickupCodeResponse response = PickupCodeResponse.builder()
                .id(1L).prescriptionId(1L).code("123456").used(false).build();
        when(prescriptionService.getPickupCode(1L)).thenReturn(response);

        mockMvc.perform(get("/api/prescriptions/1/pickup-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("123456"));
    }

    @Test
    void getPickupCode_notFound_returns400() throws Exception {
        when(prescriptionService.getPickupCode(99L))
                .thenThrow(new RuntimeException("No pickup code found for prescription: 99"));

        mockMvc.perform(get("/api/prescriptions/99/pickup-code"))
                .andExpect(status().isBadRequest());
    }

    // ── validatePickup endpoint ───────────────────────────────────

    @Test
    void validatePickupCode_returns200() throws Exception {
        PrescriptionResponse response = createResponse(1L, "RETIREE");
        when(prescriptionService.validatePickupCode(1L, "123456")).thenReturn(response);

        mockMvc.perform(post("/api/prescriptions/1/validate-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIREE"));
    }

    @Test
    void validatePickupCode_invalid_returns400() throws Exception {
        when(prescriptionService.validatePickupCode(1L, "000000"))
                .thenThrow(new RuntimeException("Invalid pickup code"));

        mockMvc.perform(post("/api/prescriptions/1/validate-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"000000\"}"))
                .andExpect(status().isBadRequest());
    }
}
