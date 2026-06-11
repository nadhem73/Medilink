package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.model.dto.PatientCreateRequest;
import com.medilinktunisia.patientservice.model.dto.PatientDto;
import com.medilinktunisia.patientservice.model.dto.PatientUpdateRequest;
import com.medilinktunisia.patientservice.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientCreateRequest request) {
        PatientDto patient = patientService.createPatient(request);
        return new ResponseEntity<>(patient, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable Long id) {
        PatientDto patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PatientDto> getPatientByUserId(@PathVariable Long userId) {
        PatientDto patient = patientService.getPatientByUserId(userId);
        return ResponseEntity.ok(patient);
    }

    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        List<PatientDto> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientUpdateRequest request) {
        PatientDto patient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(patient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
