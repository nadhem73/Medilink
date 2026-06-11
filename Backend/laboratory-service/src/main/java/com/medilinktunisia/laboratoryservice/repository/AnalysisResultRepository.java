package com.medilinktunisia.laboratoryservice.repository;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    List<AnalysisResult> findByRequestId(Long requestId);

    @Query("SELECT r FROM AnalysisResult r WHERE " +
           "r.request.id = :requestId AND r.validated = true")
    List<AnalysisResult> findValidatedResultsByRequest(@Param("requestId") Long requestId);

    @Query("SELECT r FROM AnalysisResult r WHERE " +
           "r.request.id = :requestId AND r.validated = false")
    List<AnalysisResult> findUnvalidatedResultsByRequest(@Param("requestId") Long requestId);

    @Query("SELECT COUNT(r) FROM AnalysisResult r WHERE " +
           "r.request.laboratory.id = :laboratoryId AND r.validated = true")
    long countValidatedResults(@Param("laboratoryId") Long laboratoryId);
}
