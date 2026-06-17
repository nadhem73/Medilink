package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.dto.AppointmentDto;
import com.medilinktunisia.patientservice.dto.AppointmentRequest;
import com.medilinktunisia.patientservice.security.JwtAuthenticationFilter;
import com.medilinktunisia.patientservice.security.SecurityConfig;
import com.medilinktunisia.patientservice.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService service;

    private final LocalDateTime now = LocalDateTime.of(2026, 6, 17, 14, 30);

    @Test
    void createAppointment_shouldReturn201() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(1L).patientId(1L).doctorId(2L)
                .dateTime(now).status("PENDING").mode("PRESENTIEL")
                .build();

        when(service.createAppointment(anyLong(), any(AppointmentRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/patients/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "doctorId": 2,
                                    "dateTime": "2026-06-17T14:30:00",
                                    "mode": "PRESENTIEL",
                                    "notes": "Routine check"
                                }
                                """)
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getMyAppointments_shouldReturn200() throws Exception {
        when(service.getPatientAppointments(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/patients/appointments")
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMyDoctorAppointments_shouldReturn200() throws Exception {
        when(service.getDoctorAppointments(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/patients/appointments/doctor")
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void cancelAppointment_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(5L).patientId(1L).doctorId(2L)
                .dateTime(now).status("CANCELLED").mode("PRESENTIEL")
                .build();

        when(service.cancelAppointment(1L, 5L)).thenReturn(dto);

        mockMvc.perform(put("/api/patients/appointments/{id}/cancel", 5L)
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void confirmAppointment_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(5L).patientId(1L).doctorId(2L)
                .dateTime(now).status("CONFIRMED").mode("PRESENTIEL")
                .build();

        when(service.confirmAppointment(1L, 5L)).thenReturn(dto);

        mockMvc.perform(put("/api/patients/appointments/{id}/confirm", 5L)
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelAppointmentByDoctor_shouldReturn200() throws Exception {
        AppointmentDto dto = AppointmentDto.builder()
                .id(5L).patientId(1L).doctorId(2L)
                .dateTime(now).status("CANCELLED").mode("TELECONSULTATION")
                .build();

        when(service.cancelAppointmentByDoctor(1L, 5L)).thenReturn(dto);

        mockMvc.perform(put("/api/patients/appointments/{id}/doctor-cancel", 5L)
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getAvailableSlots_shouldReturn200() throws Exception {
        when(service.getAvailableSlots(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of("09:00", "09:30"));

        mockMvc.perform(get("/api/patients/appointments/available-slots")
                        .param("doctorId", "1")
                        .param("date", "2026-06-17")
                        .param("debutMatin", "09:00")
                        .param("finMatin", "10:00")
                        .param("debutApresMidi", "14:00")
                        .param("finApresMidi", "15:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("09:00"))
                .andExpect(jsonPath("$[1]").value("09:30"));
    }

    @Test
    void checkAvailability_whenSlotFree_shouldReturnTrue() throws Exception {
        doNothing().when(service).validateTimeSlot(anyLong(), any(LocalDateTime.class));

        mockMvc.perform(get("/api/patients/appointments/check-availability")
                        .param("doctorId", "1")
                        .param("dateTime", "2026-06-17T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void getActiveDoctorIds_shouldReturn200() throws Exception {
        when(service.getActiveDoctorIdsForPatient(1L)).thenReturn(List.of(2L, 3L));

        mockMvc.perform(get("/api/patients/appointments/active-doctor-ids")
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(2L))
                .andExpect(jsonPath("$[1]").value(3L));
    }
}
