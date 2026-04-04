package de.fhswf.kassensystem.model.dto;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
