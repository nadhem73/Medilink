package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    Optional<Patient> findByEmail(String email);

    Boolean existsByUserId(Long userId);

    Boolean existsByEmail(String email);

    @Query("SELECT p FROM Patient p WHERE p.isActive = true")
    List<Patient> findAllActivePatients();

    @Query("SELECT p FROM Patient p WHERE p.city = :city AND p.isActive = true")
    List<Patient> findPatientsByCity(String city);
}
