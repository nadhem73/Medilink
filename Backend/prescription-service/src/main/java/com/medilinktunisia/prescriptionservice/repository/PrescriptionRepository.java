package com.medilinktunisia.prescriptionservice.repository;

import com.medilinktunisia.prescriptionservice.model.entity.Prescription;
import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    List<Prescription> findByPatientIdOrderByPrescriptionDateDesc(Long patientId);

    List<Prescription> findByDoctorIdOrderByPrescriptionDateDesc(Long doctorId);

    List<Prescription> findByPharmacyIdOrderByDispensedDateDesc(Long pharmacyId);

    List<Prescription> findByStatusOrderByPrescriptionDateDesc(PrescriptionStatus status);

    List<Prescription> findByPatientIdAndStatus(Long patientId, PrescriptionStatus status);

    List<Prescription> findByPatientIdAndStatusOrderByPrescriptionDateDesc(Long patientId, PrescriptionStatus status);

    List<Prescription> findByDoctorIdAndStatusOrderByPrescriptionDateDesc(Long doctorId, PrescriptionStatus status);

    @Query("SELECT p FROM Prescription p WHERE p.expiryDate < :now AND p.status = 'ACTIVE'")
    List<Prescription> findExpiredPrescriptions(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Prescription p WHERE p.patientId = :patientId AND p.prescriptionDate BETWEEN :startDate AND :endDate ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByPatientIdAndDateRange(
        @Param("patientId") Long patientId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM Prescription p WHERE p.doctorId = :doctorId AND p.prescriptionDate BETWEEN :startDate AND :endDate ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByDoctorIdAndDateRange(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.doctorId = :doctorId")
    Long countByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.patientId = :patientId")
    Long countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.pharmacyId = :pharmacyId")
    Long countByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}
