package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.model.dto.AppointmentCreateRequest;
import com.medilinktunisia.patientservice.model.dto.AppointmentDto;
import com.medilinktunisia.patientservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/patient/{patientId}")
    public ResponseEntity<AppointmentDto> createAppointment(
            @PathVariable Long patientId,
            @Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentDto appointment = appointmentService.createAppointment(patientId, request);
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDto>> getPatientAppointments(@PathVariable Long patientId) {
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointments(@PathVariable Long patientId) {
        List<AppointmentDto> appointments = appointmentService.getUpcomingAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<AppointmentDto>> getPastAppointments(@PathVariable Long patientId) {
        List<AppointmentDto> appointments = appointmentService.getPastAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentDto appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentDto> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "PATIENT") String cancelledBy) {
        AppointmentDto appointment = appointmentService.cancelAppointment(appointmentId, reason, cancelledBy);
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/{appointmentId}/confirm")
    public ResponseEntity<AppointmentDto> confirmAppointment(@PathVariable Long appointmentId) {
        AppointmentDto appointment = appointmentService.confirmAppointment(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/{appointmentId}/complete")
    public ResponseEntity<AppointmentDto> completeAppointment(@PathVariable Long appointmentId) {
        AppointmentDto appointment = appointmentService.completeAppointment(appointmentId);
        return ResponseEntity.ok(appointment);
    }
}
