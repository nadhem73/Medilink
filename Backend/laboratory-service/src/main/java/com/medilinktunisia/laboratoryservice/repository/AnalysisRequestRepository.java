package com.medilinktunisia.laboratoryservice.repository;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisRequest;
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
public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {

    Optional<AnalysisRequest> findByRequestNumber(String requestNumber);

    List<AnalysisRequest> findByLaboratoryId(Long laboratoryId);

    Page<AnalysisRequest> findByLaboratoryId(Long laboratoryId, Pageable pageable);

    List<AnalysisRequest> findByPatientId(Long patientId);

    Page<AnalysisRequest> findByPatientId(Long patientId, Pageable pageable);

    @Query("SELECT r FROM AnalysisRequest r WHERE " +
           "r.laboratory.id = :laboratoryId AND r.status = :status")
    List<AnalysisRequest> findByLaboratoryIdAndStatus(@Param("laboratoryId") Long laboratoryId,
                                                       @Param("status") AnalysisRequest.RequestStatus status);

    @Query("SELECT r FROM AnalysisRequest r WHERE " +
           "r.laboratory.id = :laboratoryId AND r.requestDate BETWEEN :startDate AND :endDate")
    List<AnalysisRequest> findByLaboratoryIdAndDateRange(@Param("laboratoryId") Long laboratoryId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM AnalysisRequest r WHERE " +
           "r.patientId = :patientId ORDER BY r.requestDate DESC")
    Page<AnalysisRequest> findPatientAnalysisHistory(@Param("patientId") Long patientId, 
                                                      Pageable pageable);

    @Query("SELECT r FROM AnalysisRequest r WHERE " +
           "r.laboratory.id = :laboratoryId AND r.status IN ('PENDING', 'CONFIRMED', 'SAMPLE_COLLECTED', 'IN_PROGRESS') " +
           "ORDER BY r.priority DESC, r.requestDate ASC")
    List<AnalysisRequest> findPendingRequests(@Param("laboratoryId") Long laboratoryId);

    @Query("SELECT r FROM AnalysisRequest r WHERE " +
           "r.laboratory.id = :laboratoryId AND r.status = 'READY' AND r.patientNotified = false")
    List<AnalysisRequest> findReadyResultsNotNotified(@Param("laboratoryId") Long laboratoryId);

    @Query("SELECT COUNT(r) FROM AnalysisRequest r WHERE " +
           "r.laboratory.id = :laboratoryId AND r.status = 'COMPLETED'")
    long countCompletedByLaboratory(@Param("laboratoryId") Long laboratoryId);

    @Query("SELECT SUM(r.totalAmount) FROM AnalysisRequest r WHERE " +
           "r.laboratory.id = :laboratoryId AND r.paid = true AND " +
           "r.requestDate BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenue(@Param("laboratoryId") Long laboratoryId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
}
