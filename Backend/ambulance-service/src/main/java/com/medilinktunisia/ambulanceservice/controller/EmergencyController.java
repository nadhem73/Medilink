package com.medilinktunisia.ambulanceservice.controller;

import com.medilinktunisia.ambulanceservice.model.dto.EmergencyCreateRequest;
import com.medilinktunisia.ambulanceservice.model.dto.EmergencyDto;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyStatus;
import com.medilinktunisia.ambulanceservice.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/emergencies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping
    public ResponseEntity<EmergencyDto> createEmergency(
            @Valid @RequestBody EmergencyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emergencyService.createEmergency(request));
    }

    @GetMapping
    public ResponseEntity<List<EmergencyDto>> getAllEmergencies() {
        return ResponseEntity.ok(emergencyService.getAllEmergencies());
    }

    @GetMapping("/active")
    public ResponseEntity<List<EmergencyDto>> getActiveEmergencies() {
        return ResponseEntity.ok(emergencyService.getActiveEmergencies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyDto> getEmergencyById(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.getEmergencyById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<EmergencyDto> getEmergencyByCode(@PathVariable String code) {
        return ResponseEntity.ok(emergencyService.getEmergencyByCode(code));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<EmergencyDto> assignAmbulance(
            @PathVariable Long id,
            @RequestParam Long ambulanceId) {
        return ResponseEntity.ok(emergencyService.assignAmbulance(id, ambulanceId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EmergencyDto> updateStatus(
            @PathVariable Long id,
            @RequestParam EmergencyStatus status) {
        return ResponseEntity.ok(emergencyService.updateStatus(id, status));
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<EmergencyDto> addParamedicNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                emergencyService.addParamedicNotes(id, request.get("notes"))
        );
    }

    @PutMapping("/{id}/hospital")
    public ResponseEntity<EmergencyDto> setDestinationHospital(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                emergencyService.setDestinationHospital(
                        id,
                        request.get("hospital"),
                        request.get("address")
                )
        );
    }
}
