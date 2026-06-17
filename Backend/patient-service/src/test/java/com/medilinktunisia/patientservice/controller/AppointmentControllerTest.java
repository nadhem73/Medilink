package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.dto.AppointmentDto;
import com.medilinktunisia.patientservice.dto.AppointmentRequest;
import com.medilinktunisia.patientservice.security.JwtService;
import com.medilinktunisia.patientservice.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class, properties = {"jwt.secret=testSecretKeyForJWTTokenGenerationAndValidationThatIsLongEnough"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void createAppointment_shouldReturn201() throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(LocalDateTime.now().plusDays(1));
        request.setMode("PRESENTIEL");

        AppointmentDto response = AppointmentDto.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(1L)
                .status("PENDING")
                .mode("PRESENTIEL")
                .build();

        when(appointmentService.createAppointment(anyLong(), any(AppointmentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/patients/appointments")
                        .requestAttr("userId", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.mode").value("PRESENTIEL"));
    }

    @Test
    void getMyAppointments_shouldReturn200() throws Exception {
        AppointmentDto appointment = AppointmentDto.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(1L)
                .status("PENDING")
                .mode("PRESENTIEL")
                .build();

        when(appointmentService.getPatientAppointments(anyLong()))
                .thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/patients/appointments")
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getActiveDoctorIds_shouldReturn200() throws Exception {
        when(appointmentService.getActiveDoctorIdsForPatient(anyLong()))
                .thenReturn(List.of(1L, 3L));

        mockMvc.perform(get("/api/patients/appointments/active-doctor-ids")
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1L))
                .andExpect(jsonPath("$[1]").value(3L));
    }

    @Test
    void cancelAppointment_shouldReturn200() throws Exception {
        AppointmentDto cancelled = AppointmentDto.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(1L)
                .status("CANCELLED")
                .build();

        when(appointmentService.cancelAppointment(anyLong(), anyLong()))
                .thenReturn(cancelled);

        mockMvc.perform(put("/api/patients/appointments/1/cancel")
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void createAppointment_withInvalidMode_shouldReturn400() throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(LocalDateTime.now().plusDays(1));
        request.setMode("INVALID_MODE");

        when(appointmentService.createAppointment(anyLong(), any(AppointmentRequest.class)))
                .thenThrow(new IllegalArgumentException("Mode de consultation invalide"));

        mockMvc.perform(post("/api/patients/appointments")
                        .requestAttr("userId", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
