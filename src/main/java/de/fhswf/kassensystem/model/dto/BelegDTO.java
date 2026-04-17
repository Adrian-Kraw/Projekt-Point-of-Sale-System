package de.fhswf.kassensystem.model.dto;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object zur Darstellung eines Kassenbelegs.
 *
 * <p>
 *     Enthält alle relevanten Informationen eines abgeschlossenen Verkaufsvorgangs und dient als Grundlage für die
 *     Beleganzeige sowie den PDF-/Druckdialog.
 * </p>
 *
 * <p>
 *     Dieses DTO wird nicht persistiert, sondern aus einem abgeschlossenen {@code Verkauf}-Objekt zusammengestellt.
 * </p>
 *
 * @author Paula Martin
 */
@Getter
@Setter
public class BelegDTO {

    private Long verkaufId;
    private LocalDateTime timestamp;
    private List<Verkaufsposition> positionen;
    private BigDecimal gesamtsumme;
    private Zahlungsart zahlungsart;
    private BigDecimal rabatt;
    private User kassierer;
}
