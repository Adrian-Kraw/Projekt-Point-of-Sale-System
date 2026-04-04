package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Repräsentiert einen Artikel im System.
 */
@Entity
@Table(name = "Artikel")
@Getter
@Setter
public class Artikel {

    public static final int STANDARD_MINIMALBESTAND = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal preis;

    @ManyToOne
    @JoinColumn(name = "kategorie_id", nullable = false)
    private Kategorie kategorie;

    @Column(nullable = false)
    private boolean aktiv;

    @Column(nullable = false)
    private int bestand;

    @Column(nullable = false)
    private int minimalbestand = STANDARD_MINIMALBESTAND;

    @ManyToOne
    @JoinColumn(name = "mehrwertsteuer_id", nullable = false)
    private Mehrwertsteuer mehrwertsteuer;
}
