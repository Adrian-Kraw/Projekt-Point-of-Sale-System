package de.fhswf.kassensystem.model.dto;

import de.fhswf.kassensystem.model.Artikel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) für die Artikelstatistik innerhalb eines Anwendungszeitraums.
 *
 * <p>
 *     Wird im Rahmen von Berichten verwendet, um pro Artikel die verkaufte Gesamtmenge sowie den erzielten Umsatz
 *     zu übermitteln.
 * </p>
 *
 * <p>
 *   Dieses DTO ist ausschließlich für den Lesefall vorgesehen und wird nicht direkt in der Datenbank persistiert.
 * </p>
 *
 * @author Paula Martin
 */
 @Getter
 @Setter
public class ArtikelStatistikDTO {
    private Artikel artikel;
    private int anzahlVerkauft;
    private BigDecimal gesamtumsatz;
}
