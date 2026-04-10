package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Einfache Key-Value-Tabelle für systemweite Einstellungen.
 * Beispiel: schluessel="bon_zielwert", wert="5.00"
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