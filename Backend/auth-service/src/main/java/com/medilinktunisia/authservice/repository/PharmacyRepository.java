package com.medilinktunisia.authservice.repository;

import com.medilinktunisia.authservice.model.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);
}
