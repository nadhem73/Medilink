package com.medilinktunisia.doctorservice.service;

import com.medilinktunisia.doctorservice.dto.DoctorProfileDto;
import com.medilinktunisia.doctorservice.dto.DoctorProfileRequest;
import com.medilinktunisia.doctorservice.model.DoctorProfile;
import com.medilinktunisia.doctorservice.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorProfileRepository repository;

    /**
     * Crée le profil opérationnel d'un médecin (appelé par l'auth-service à la
     * création du compte). Idempotent : ne recrée pas si un profil existe déjà.
     */
    public void createDoctorProfile(DoctorProfileRequest request) {
        if (request.getUserId() == null || repository.existsByUserId(request.getUserId())) {
            return;
        }
        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(request.getUserId());
        profile.setAvailable(request.getAvailable() != null ? request.getAvailable() : Boolean.TRUE);
        profile.setBiography(request.getBiography());
        profile.setFee(request.getFee());
        profile.setDebutMatin(request.getDebutMatin() != null ? request.getDebutMatin() : "08:00");
        profile.setFinMatin(request.getFinMatin() != null ? request.getFinMatin() : "13:00");
        profile.setDebutApresMidi(request.getDebutApresMidi() != null ? request.getDebutApresMidi() : "15:00");
        profile.setFinApresMidi(request.getFinApresMidi() != null ? request.getFinApresMidi() : "19:00");
        repository.save(profile);
    }

    /** Profil du médecin connecté ; vide si aucun n'existe encore. */
    public DoctorProfileDto getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(this::toDto)
                .orElseGet(() -> DoctorProfileDto.builder().userId(userId).available(Boolean.TRUE).build());
    }

    /** Tous les profils médecins (écran de prise de RDV patient). */
    public java.util.List<DoctorProfileDto> getAllProfiles() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Met à jour le profil du médecin connecté (panel médecin).
     * Crée le profil s'il n'existait pas encore.
     */
    public DoctorProfileDto updateByUserId(Long userId, DoctorProfileRequest request) {
        DoctorProfile profile = repository.findByUserId(userId)
                .orElseGet(() -> {
                    DoctorProfile p = new DoctorProfile();
                    p.setUserId(userId);
                    return p;
                });
        if (request.getAvailable() != null) {
            profile.setAvailable(request.getAvailable());
        }
        profile.setBiography(request.getBiography());
        profile.setFee(request.getFee());
        if (request.getDebutMatin() != null) profile.setDebutMatin(request.getDebutMatin());
        if (request.getFinMatin() != null) profile.setFinMatin(request.getFinMatin());
        if (request.getDebutApresMidi() != null) profile.setDebutApresMidi(request.getDebutApresMidi());
        if (request.getFinApresMidi() != null) profile.setFinApresMidi(request.getFinApresMidi());
        return toDto(repository.save(profile));
    }

    private DoctorProfileDto toDto(DoctorProfile p) {
        return DoctorProfileDto.builder()
                .userId(p.getUserId())
                .available(p.getAvailable())
                .biography(p.getBiography())
                .fee(p.getFee())
                .debutMatin(p.getDebutMatin())
                .finMatin(p.getFinMatin())
                .debutApresMidi(p.getDebutApresMidi())
                .finApresMidi(p.getFinApresMidi())
                .build();
    }
}
