package com.medilinktunisia.prescriptionservice.repository;

import com.medilinktunisia.prescriptionservice.model.entity.PickupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PickupCodeRepository extends JpaRepository<PickupCode, Long> {

    Optional<PickupCode> findByPrescriptionId(Long prescriptionId);

    Optional<PickupCode> findByCode(String code);

    boolean existsByPrescriptionId(Long prescriptionId);
}
