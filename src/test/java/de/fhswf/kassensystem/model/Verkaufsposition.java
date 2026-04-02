package de.fhswf.kassensystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Repräsentiert eine Verkaufsposition im System.
 */
@Entity
@Table(name = "Verkaufsposition")
public class Verkaufsposition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int menge;

    @Column(nullable = false)
    private BigDecimal einzelpreis;

    @ManyToOne
    @JoinColumn(name = "artikel_id", nullable = false)
    private Artikel artikel;

    @ManyToOne
    @JoinColumn(name = "verkauf_id", nullable = false)
    private Verkauf verkauf;
}
