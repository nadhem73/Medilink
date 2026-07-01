package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.Medicament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentRepository extends JpaRepository<Medicament, Long> {

    /** Recherche par nom commercial (insensible à la casse, partielle). */
    Page<Medicament> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /** Recherche par principe actif (DCI). */
    List<Medicament> findByDciContainingIgnoreCase(String dci);

    /** Liste par classe thérapeutique. */
    List<Medicament> findByType(String type);
}
