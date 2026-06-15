package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
