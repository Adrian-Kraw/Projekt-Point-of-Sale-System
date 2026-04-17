package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Repräsentiert eine einzelne Position innerhalb eines Verkaufsvorgangs.
 *
 * <p>
 *     Jede Position verknüpft einen {@link Artikel} mit einem {@link Verkauf} und hält die zum Verkaufszeitpunkt
 *     gültigen Werte fest. Positionen werden beim Hinzufügen von Artikeln zum Warenkorb angelegt und können bis
 *     zum Abschluss des Vorgangs geändert oder entfernt werden.
 * </p>
 *
 * <p>
 *     Der {@code einzelpreis} wird bewusst als Snapshot gespeichert, damit spätere Preisänderungen am Artikel keine
 *     Auswirkung auf historische Belege haben.
 * </p>
 *
 * @author Paula Martin
 */
@Entity
@Table(name = "verkaufsposition")
@Getter
@Setter
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
