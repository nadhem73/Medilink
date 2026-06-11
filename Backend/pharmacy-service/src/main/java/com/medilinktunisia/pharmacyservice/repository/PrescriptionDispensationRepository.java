package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.entity.PrescriptionDispensation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionDispensationRepository extends JpaRepository<PrescriptionDispensation, Long> {

    Optional<PrescriptionDispensation> findByDispensationNumber(String dispensationNumber);

    List<PrescriptionDispensation> findByPharmacyId(Long pharmacyId);

    Page<PrescriptionDispensation> findByPharmacyId(Long pharmacyId, Pageable pageable);

    List<PrescriptionDispensation> findByPrescriptionId(Long prescriptionId);

    List<PrescriptionDispensation> findByPatientId(Long patientId);

    Page<PrescriptionDispensation> findByPatientId(Long patientId, Pageable pageable);

    @Query("SELECT d FROM PrescriptionDispensation d WHERE " +
           "d.pharmacy.id = :pharmacyId AND d.status = :status")
    List<PrescriptionDispensation> findByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId,
                                                              @Param("status") PrescriptionDispensation.DispensationStatus status);

    @Query("SELECT d FROM PrescriptionDispensation d WHERE " +
           "d.pharmacy.id = :pharmacyId AND d.dispensationDate BETWEEN :startDate AND :endDate")
    List<PrescriptionDispensation> findByPharmacyIdAndDateRange(@Param("pharmacyId") Long pharmacyId,
                                                                 @Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM PrescriptionDispensation d WHERE " +
           "d.pharmacy.id = :pharmacyId AND d.status = 'DISPENSED'")
    long countDispensedByPharmacy(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT SUM(d.totalAmount) FROM PrescriptionDispensation d WHERE " +
           "d.pharmacy.id = :pharmacyId AND d.status = 'DISPENSED' AND " +
           "d.dispensationDate BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenue(@Param("pharmacyId") Long pharmacyId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT d FROM PrescriptionDispensation d WHERE " +
           "d.patientId = :patientId ORDER BY d.dispensationDate DESC")
    Page<PrescriptionDispensation> findPatientDispensationHistory(@Param("patientId") Long patientId, 
                                                                   Pageable pageable);

    // Dispensations en attente
    @Query("SELECT d FROM PrescriptionDispensation d WHERE " +
           "d.pharmacy.id = :pharmacyId AND d.status IN ('PENDING', 'IN_PREPARATION') " +
           "ORDER BY d.createdAt ASC")
    List<PrescriptionDispensation> findPendingDispensations(@Param("pharmacyId") Long pharmacyId);
}
