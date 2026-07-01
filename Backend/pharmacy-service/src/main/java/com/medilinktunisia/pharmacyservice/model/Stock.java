package com.medilinktunisia.pharmacyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Lot de stock d'un médicament (domaine «Gestion Pharmacie & Stock»).
 * Chaque ligne représente un lot physique : quantité disponible, dates de
 * fabrication/expiration, emplacement de rangement et date du dernier
 * réapprovisionnement. Un médicament peut avoir plusieurs lots
 * (relation un-à-plusieurs vers {@link Medicament}).
 * <p>
 * Alimenté au démarrage depuis {@code data/stock_medicaments.csv}
 * (cf. {@code StockSeeder}).
 */
@Entity
@Table(name = "stock_medicaments", indexes = {
        @Index(name = "idx_stock_medicament_id", columnList = "medicament_id"),
        @Index(name = "idx_stock_date_expiration", columnList = "date_expiration")
})
@Getter
@Setter
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Médicament concerné (clé étrangère vers {@code medicaments}). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicament_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stock_medicament"))
    private Medicament medicament;

    /** Quantité actuellement en stock pour ce lot. */
    @Column(name = "quantite_en_stock", nullable = false)
    private Integer quantiteEnStock = 0;

    /** Date de fabrication du lot. */
    @Column(name = "date_fabrication")
    private LocalDate dateFabrication;

    /** Date d'expiration du lot. */
    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    /** Emplacement de rangement (ex. «B12», «Rayon5»). */
    @Column(name = "emplacement", length = 50)
    private String emplacement;

    /** Date du dernier réapprovisionnement de ce lot. */
    @Column(name = "dernier_reapprovisionnement")
    private LocalDate dernierReapprovisionnement;
}
