package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Repräsentiert einen Mehrwertsteuersatz im System.
 */
@Entity
@Table(name = "Mehrwertsteuer")
@Getter
@Setter
public class Mehrwertsteuer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String bezeichnung;

    @Column(nullable = false)
    private BigDecimal satz;
}
