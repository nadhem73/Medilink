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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class,
        properties = {"jwt.secret=testSecretKeyForJWTTokenGenerationAndValidationThatIsLongEnough"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService service;

    @MockBean
    private JwtService jwtService;

    @Test
    void createAppointment_shouldReturn201() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(1L).patientId(1L).doctorId(2L)
                .dateTime(LocalDateTime.parse("2025-06-20T10:00:00"))
                .status("PENDING").mode("PRESENTIEL")
                .build();

        when(service.createAppointment(eq(1L), any(AppointmentRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/patients/appointments")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "doctorId": 2,
                                    "dateTime": "2025-06-20T10:00:00",
                                    "mode": "PRESENTIEL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.patientId", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(service).createAppointment(eq(1L), any(AppointmentRequest.class));
    }

    @Test
    void getMyAppointments_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(1L).patientId(1L).doctorId(2L)
                .status("PENDING").mode("PRESENTIEL")
                .build();

        when(service.getPatientAppointments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/patients/appointments")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(service).getPatientAppointments(1L);
    }

    @Test
    void getMyDoctorAppointments_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(2L).patientId(3L).doctorId(1L)
                .status("CONFIRMED").mode("TELECONSULTATION")
                .build();

        when(service.getDoctorAppointments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/patients/appointments/doctor")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctorId", is(1)));

        verify(service).getDoctorAppointments(1L);
    }

    @Test
    void getActiveDoctorIds_shouldReturn200() throws Exception {
        when(service.getActiveDoctorIdsForPatient(1L)).thenReturn(List.of(2L, 3L));

        mockMvc.perform(get("/api/patients/appointments/active-doctor-ids")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is(2)))
                .andExpect(jsonPath("$[1]", is(3)));

        verify(service).getActiveDoctorIdsForPatient(1L);
    }

    @Test
    void getAvailableSlots_shouldReturn200() throws Exception {
        when(service.getAvailableSlots(
                eq(1L), eq("2025-06-20"), eq("09:00"), eq("12:00"),
                eq("14:00"), eq("17:00")))
                .thenReturn(List.of("09:00", "09:30", "10:00"));

        mockMvc.perform(get("/api/patients/appointments/available-slots")
                        .param("doctorId", "1")
                        .param("date", "2025-06-20")
                        .param("debutMatin", "09:00")
                        .param("finMatin", "12:00")
                        .param("debutApresMidi", "14:00")
                        .param("finApresMidi", "17:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("09:00")));

        verify(service).getAvailableSlots(
                eq(1L), eq("2025-06-20"), eq("09:00"), eq("12:00"),
                eq("14:00"), eq("17:00"));
    }

    @Test
    void checkAvailability_whenSlotFree_shouldReturnTrue() throws Exception {
        doNothing().when(service).validateTimeSlot(anyLong(), any(LocalDateTime.class));

        mockMvc.perform(get("/api/patients/appointments/check-availability")
                        .param("doctorId", "1")
                        .param("dateTime", "2025-06-20T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void cancelAppointment_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(1L).patientId(1L).doctorId(2L)
                .status("CANCELLED").mode("PRESENTIEL")
                .build();

        when(service.cancelAppointment(1L, 1L)).thenReturn(dto);

        mockMvc.perform(put("/api/patients/appointments/1/cancel")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(service).cancelAppointment(1L, 1L);
    }

    @Test
    void cancelAppointmentByDoctor_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(5L).patientId(1L).doctorId(2L)
                .status("CANCELLED").mode("TELECONSULTATION")
                .build();

        when(service.cancelAppointmentByDoctor(1L, 5L)).thenReturn(dto);

        mockMvc.perform(put("/api/patients/appointments/5/doctor-cancel")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(service).cancelAppointmentByDoctor(1L, 5L);
    }

    @Test
    void confirmAppointment_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(1L).patientId(1L).doctorId(2L)
                .status("CONFIRMED").mode("PRESENTIEL")
                .build();

        when(service.confirmAppointment(2L, 1L)).thenReturn(dto);

        mockMvc.perform(put("/api/patients/appointments/1/confirm")
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        verify(service).confirmAppointment(2L, 1L);
    }
}
