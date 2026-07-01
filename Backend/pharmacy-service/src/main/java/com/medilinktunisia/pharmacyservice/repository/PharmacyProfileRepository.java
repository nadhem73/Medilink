package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.PharmacyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacyProfileRepository extends JpaRepository<PharmacyProfile, Long> {

    Optional<PharmacyProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
