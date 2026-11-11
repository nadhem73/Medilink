package com.medilinktunisia.bilanservice.repository;

import com.medilinktunisia.bilanservice.model.entity.Bilan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BilanRepository extends JpaRepository<Bilan, UUID> {
    Page<Bilan> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    Page<Bilan> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);
}
