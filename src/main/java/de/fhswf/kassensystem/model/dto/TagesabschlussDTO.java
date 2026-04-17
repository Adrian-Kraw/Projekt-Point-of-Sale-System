package de.fhswf.kassensystem.model.dto;

import de.fhswf.kassensystem.model.Verkauf;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object für den Tagesabschluss eines Geschäftstages.
 *
 * <p>
 *     Fasst alle relevanten Kennzahlen eines ausgewählten Tages zusammen und wird im Rahmen des Tagesabschlusses
 *     verwendet, um dem Manager eine Übersicht über Umsatz, Transaktionsanzahl und Zahlungsarten bereitzustellen.
 * </p>
 *
 * <p>
 *     Dieses DTO wird nicht in der Datenbank persistiert, sondern zur Laufzeit aus den {@code Verkauf}-Objekten des
 *     jeweiligen Tages aggregiert.
 * </p>
 *
 * @author Paula Martin
 */
@Getter
@Setter
public class TagesabschlussDTO {
    private LocalDate datum;
    private BigDecimal gesamtumsatz;
    private int anzahlTransaktionen;
    private BigDecimal umsatzBar;
    private BigDecimal umsatzKarte;
    private List<Verkauf> verkaeufe;
}
