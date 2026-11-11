package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /** Lots de stock d'un médicament donné. */
    List<Stock> findByMedicamentId(Long medicamentId);

    /** Lots disponibles (> 0) d'un médicament, triés par date d'expiration (FIFO). */
    @Query("SELECT s FROM Stock s WHERE s.medicament.id = :medicamentId AND s.quantiteEnStock > 0 " +
           "ORDER BY CASE WHEN s.dateExpiration IS NULL THEN 1 ELSE 0 END, s.dateExpiration ASC, " +
           "CASE WHEN s.dateFabrication IS NULL THEN 1 ELSE 0 END, s.dateFabrication ASC, s.id ASC")
    List<Stock> findAvailableLotsFifo(@Param("medicamentId") Long medicamentId);

    /** Lots dont la quantité est inférieure ou égale à un seuil (alerte rupture). */
    List<Stock> findByQuantiteEnStockLessThanEqual(Integer threshold);

    /** Lots expirant avant une date (gestion des périmés). */
    List<Stock> findByDateExpirationBefore(LocalDate date);
}
