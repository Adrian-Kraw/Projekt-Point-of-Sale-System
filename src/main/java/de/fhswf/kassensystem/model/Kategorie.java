package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Repräsentiert eine Kategorie im Kassensystem.
 *
 * <p>
 *     Kategorien dienen der thematischen Gruppierung von Artikeln und werden bei der Artikelanlage zugewiesen. Sie
 *     ermöglichen die Filterung im Artikelstamm sowie eine kategoriebasierte Auswertung.
 * </p>
 *
 * @author Paula Martin
 */
@Entity
@Table(name = "kategorie")
@Getter
@Setter
public class Kategorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;
}
