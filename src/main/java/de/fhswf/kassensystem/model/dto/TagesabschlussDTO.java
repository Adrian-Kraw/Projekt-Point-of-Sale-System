package de.fhswf.kassensystem.model.dto;

import de.fhswf.kassensystem.model.Verkauf;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Todo: Kommentar ergänzen
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
