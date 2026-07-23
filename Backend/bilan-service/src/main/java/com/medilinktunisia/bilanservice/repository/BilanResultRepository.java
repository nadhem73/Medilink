package com.medilinktunisia.bilanservice.repository;

import com.medilinktunisia.bilanservice.model.entity.BilanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BilanResultRepository extends JpaRepository<BilanResult, Long> {
    List<BilanResult> findByBilanId(UUID bilanId);
}
