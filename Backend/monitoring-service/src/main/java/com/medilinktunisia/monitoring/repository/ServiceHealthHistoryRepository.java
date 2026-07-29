package com.medilinktunisia.monitoring.repository;

import com.medilinktunisia.monitoring.entity.ServiceHealthHistory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceHealthHistoryRepository extends JpaRepository<ServiceHealthHistory, Long> {

    List<ServiceHealthHistory> findByServiceNameAndTimestampBetweenOrderByTimestampAsc(
            String serviceName, LocalDateTime from, LocalDateTime to);

    List<ServiceHealthHistory> findByTimestampBetweenOrderByTimestampAsc(
            LocalDateTime from, LocalDateTime to);

    @Query("SELECT DISTINCT s.serviceName FROM ServiceHealthHistory s")
    List<String> findDistinctServiceNames();

    @Query("SELECT s FROM ServiceHealthHistory s WHERE s.timestamp = " +
           "(SELECT MAX(s2.timestamp) FROM ServiceHealthHistory s2 WHERE s2.serviceName = s.serviceName)")
    List<ServiceHealthHistory> findLatestPerService();

    @Query("SELECT AVG(s.cpuPercent), AVG(s.ramPercent), AVG(s.diskPercent), " +
           "AVG(s.responseTimeMs), COALESCE(SUM(s.requestsCount), 0), COALESCE(SUM(s.errorsCount), 0) " +
           "FROM ServiceHealthHistory s WHERE s.timestamp BETWEEN :from AND :to")
    List<Object[]> aggregateMetricsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceHealthHistory s WHERE s.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);
}
