package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /** Lots de stock d'un médicament donné. */
    List<Stock> findByMedicamentId(Long medicamentId);

    /** Lots dont la quantité est inférieure ou égale à un seuil (alerte rupture). */
    List<Stock> findByQuantiteEnStockLessThanEqual(Integer threshold);

    /** Lots expirant avant une date (gestion des périmés). */
    List<Stock> findByDateExpirationBefore(LocalDate date);
}
