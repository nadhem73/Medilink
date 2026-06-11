package com.medilinktunisia.laboratoryservice.repository;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisTypeRepository extends JpaRepository<AnalysisType, Long> {

    Optional<AnalysisType> findByAnalysisCode(String analysisCode);

    Page<AnalysisType> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<AnalysisType> findByCategory(AnalysisType.AnalysisCategory category);

    List<AnalysisType> findByActiveTrue();

    @Query("SELECT a FROM AnalysisType a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.analysisCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<AnalysisType> searchAnalysisTypes(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT a FROM AnalysisType a WHERE a.category = :category AND a.active = true")
    List<AnalysisType> findActiveByCategory(@Param("category") AnalysisType.AnalysisCategory category);

    @Query("SELECT COUNT(a) FROM AnalysisType a WHERE a.active = true")
    long countActiveAnalysisTypes();
}
