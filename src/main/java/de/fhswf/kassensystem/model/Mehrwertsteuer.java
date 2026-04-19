package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Repräsentiert einen Mehrwertsteuersatz im Kassensystem.
 *
 * <p>
 *     Mehrwertsteuersätze werden Artikeln zugewiesen und fließen in die MwSt-Aufschlüsselung auf dem Beleg sowie die
 *     Umsatzauswertungen ein.
 * </p>
 *
 * <p>
 *     Beispiele:
 *     <ul>
 *         <li>{@code "Regelsteuersatz"} -> {@code 19.00}</li>
 *         <li>{@code "Ermäßigter Steuersatz"} -> {@code 7.00}</li>
 *     </ul>
 * </p>
 *
 * @author Paula Martin
 */
@Entity
@Table(name = "mehrwertsteuer")
@Getter
@Setter
public class Mehrwertsteuer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String bezeichnung;

    @Column(nullable = false)
    private BigDecimal satz;
}
