package com.medilinktunisia.ambulanceservice.controller;

import com.medilinktunisia.ambulanceservice.model.dto.AmbulanceDto;
import com.medilinktunisia.ambulanceservice.model.dto.LocationUpdateRequest;
import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import com.medilinktunisia.ambulanceservice.service.AmbulanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ambulances")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    @GetMapping
    public ResponseEntity<List<AmbulanceDto>> getAllAmbulances() {
        return ResponseEntity.ok(ambulanceService.getAllAmbulances());
    }

    @GetMapping("/available")
    public ResponseEntity<List<AmbulanceDto>> getAvailableAmbulances() {
        return ResponseEntity.ok(ambulanceService.getAvailableAmbulances());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmbulanceDto> getAmbulanceById(@PathVariable Long id) {
        return ResponseEntity.ok(ambulanceService.getAmbulanceById(id));
    }

    @PostMapping
    public ResponseEntity<AmbulanceDto> createAmbulance(@Valid @RequestBody AmbulanceDto ambulanceDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ambulanceService.createAmbulance(ambulanceDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmbulanceDto> updateAmbulance(
            @PathVariable Long id,
            @Valid @RequestBody AmbulanceDto ambulanceDto) {
        return ResponseEntity.ok(ambulanceService.updateAmbulance(id, ambulanceDto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam AmbulanceStatus status) {
        ambulanceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        ambulanceService.updateLocation(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmbulance(@PathVariable Long id) {
        ambulanceService.deleteAmbulance(id);
        return ResponseEntity.noContent().build();
    }
}
