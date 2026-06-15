package com.medilinktunisia.authservice.repository;

import com.medilinktunisia.authservice.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByCin(String cin);

    Optional<Patient> findByCin(String cin);
}
