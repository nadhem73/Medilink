package com.medilinktunisia.authservice.repository;

import com.medilinktunisia.authservice.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);
}
