package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.entity.PharmacyOrder;
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
public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {

    Optional<PharmacyOrder> findByOrderNumber(String orderNumber);

    List<PharmacyOrder> findByPharmacyId(Long pharmacyId);

    Page<PharmacyOrder> findByPharmacyId(Long pharmacyId, Pageable pageable);

    @Query("SELECT o FROM PharmacyOrder o WHERE " +
           "o.pharmacy.id = :pharmacyId AND o.status = :status")
    List<PharmacyOrder> findByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId,
                                                   @Param("status") PharmacyOrder.OrderStatus status);

    @Query("SELECT o FROM PharmacyOrder o WHERE " +
           "o.pharmacy.id = :pharmacyId AND o.orderDate BETWEEN :startDate AND :endDate")
    List<PharmacyOrder> findByPharmacyIdAndDateRange(@Param("pharmacyId") Long pharmacyId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM PharmacyOrder o WHERE " +
           "o.pharmacy.id = :pharmacyId AND o.status IN ('PENDING', 'CONFIRMED', 'PROCESSING')")
    long countActiveOrders(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT SUM(o.totalAmount) FROM PharmacyOrder o WHERE " +
           "o.pharmacy.id = :pharmacyId AND o.status = 'DELIVERED' AND " +
           "o.orderDate BETWEEN :startDate AND :endDate")
    Double calculateTotalOrdersValue(@Param("pharmacyId") Long pharmacyId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM PharmacyOrder o WHERE " +
           "o.pharmacy.id = :pharmacyId AND o.status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED') " +
           "ORDER BY o.orderDate ASC")
    List<PharmacyOrder> findActiveOrders(@Param("pharmacyId") Long pharmacyId);
}
