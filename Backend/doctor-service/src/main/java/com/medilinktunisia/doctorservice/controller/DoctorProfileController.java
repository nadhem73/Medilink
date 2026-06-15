package com.medilinktunisia.doctorservice.controller;

import com.medilinktunisia.doctorservice.dto.DoctorProfileDto;
import com.medilinktunisia.doctorservice.dto.DoctorProfileRequest;
import com.medilinktunisia.doctorservice.service.DoctorProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final DoctorProfileService service;

    /**
     * Endpoint INTERNE (service-à-service) : création du profil médecin
     * lors de la création du compte via l'auth-service.
     */
    @PostMapping("/internal/doctor-profile")
    public ResponseEntity<Void> createDoctorProfile(@RequestBody DoctorProfileRequest request) {
        service.createDoctorProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Profil du médecin connecté (affiché dans le panel médecin).
     * L'identifiant utilisateur est extrait du JWT par le filtre de sécurité.
     */
    @GetMapping("/me/doctor-profile")
    public ResponseEntity<DoctorProfileDto> getMyDoctorProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    /**
     * Liste tous les profils médecins (tarif, biographie, disponibilité).
     * Utilisé par le panel patient pour l'écran de prise de rendez-vous.
     */
    @GetMapping("/all")
    public ResponseEntity<java.util.List<DoctorProfileDto>> getAllDoctorProfiles() {
        return ResponseEntity.ok(service.getAllProfiles());
    }

    /**
     * Mise à jour du profil du médecin connecté depuis le panel médecin.
     */
    @PutMapping("/me/doctor-profile")
    public ResponseEntity<DoctorProfileDto> updateMyDoctorProfile(
            HttpServletRequest request,
            @RequestBody DoctorProfileRequest body) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(service.updateByUserId(userId, body));
    }
}
