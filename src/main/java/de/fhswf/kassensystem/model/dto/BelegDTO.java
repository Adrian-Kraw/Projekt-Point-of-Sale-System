package de.fhswf.kassensystem.model.dto;

import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Zahlungsart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BelegDTO {

    private Long verkaufId;
    private LocalDateTime timestamp;
    private List<Verkaufsposition> positionen;
    private BigDecimal gesamtsumme;
    private Zahlungsart zahlungsart;
    private BigDecimal rabatt;
    private User kassierer;

}
