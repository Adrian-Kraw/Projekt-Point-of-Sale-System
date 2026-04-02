package de.fhswf.kassensystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = Wareneingang)
@Getter
@Setter
public class Wareneingang {

    private Artikel artikel;
    private int menge;
    private String kommentar;
    private String lieferant;
    private LocalDate datum;
}
