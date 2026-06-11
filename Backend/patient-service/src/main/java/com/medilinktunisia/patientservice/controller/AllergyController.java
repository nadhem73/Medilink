package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.model.dto.AllergyCreateRequest;
import com.medilinktunisia.patientservice.model.dto.AllergyDto;
import com.medilinktunisia.patientservice.service.AllergyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/allergies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AllergyController {

    private final AllergyService allergyService;

    @PostMapping
    public ResponseEntity<AllergyDto> addAllergy(
            @PathVariable Long patientId,
            @Valid @RequestBody AllergyCreateRequest request) {
        AllergyDto allergy = allergyService.addAllergy(patientId, request);
        return new ResponseEntity<>(allergy, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AllergyDto>> getAllergies(@PathVariable Long patientId) {
        List<AllergyDto> allergies = allergyService.getAllergiesByPatientId(patientId);
        return ResponseEntity.ok(allergies);
    }

    @GetMapping("/critical")
    public ResponseEntity<List<AllergyDto>> getCriticalAllergies(@PathVariable Long patientId) {
        List<AllergyDto> allergies = allergyService.getCriticalAllergies(patientId);
        return ResponseEntity.ok(allergies);
    }

    @DeleteMapping("/{allergyId}")
    public ResponseEntity<Void> deleteAllergy(@PathVariable Long allergyId) {
        allergyService.deleteAllergy(allergyId);
        return ResponseEntity.noContent().build();
    }
}
