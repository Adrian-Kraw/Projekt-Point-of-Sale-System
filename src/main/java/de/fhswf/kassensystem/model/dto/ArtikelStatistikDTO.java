package de.fhswf.kassensystem.model.dto;

import de.fhswf.kassensystem.model.Artikel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * TODO: Kommentar hinzufügen
 */
 @Getter
 @Setter
public class ArtikelStatistikDTO {
    private Artikel artikel;
    private int anzahlVerkauft;
    private BigDecimal gesamtumsatz;
}
