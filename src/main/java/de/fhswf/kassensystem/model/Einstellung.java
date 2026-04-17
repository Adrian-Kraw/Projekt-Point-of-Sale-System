package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Einfache Key-Value-Tabelle für systemweite Einstellungen.
 *
 * <p>
 *     Ermöglicht das flexible Hinterlegen von Systemparamatern ohne Codeänderung,
 *     zum Beispiel Schwellenwerte, Anzeigeoptionen oder betriebliche Vorgaben.
 * </p>
 *
 * <p>
 *     Beispiele:
 *     <ul>
 *         <li>{@code bon_zielwert} -> {@code "5.00"}</li>
 *     </ul>
 * </p>
 *
 * <p>
 *     Der Schlüssel dient gleichzeitig als Primärschlüssel und sollte systemweit eindeutig und sprechend benannt sein.
 * </p>
 *
 * @author Adrian Krawietz, Paula Martin
 */
@Entity
@Table(name = "einstellung")
@Getter
@Setter
public class Einstellung {

    @Id
    @Column(length = 100, nullable = false)
    private String schluessel;

    @Column(length = 255)
    private String wert;
}