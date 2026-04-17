package de.fhswf.kassensystem.model;

import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Repräsentiert einen Wareneingang im Kassensystem.
 *
 * <p>
 *     Ein Wareneingang dokumentiert die Lieferung von Artikeln und ermöglicht die Erhöhung des Lagerbestands. Er wird
 *     zunächst mit dem Status {@code AUSSTEHEND} angelegt und muss von einem Manger bestätigt werden, bevor der
 *     Bestand des zugehörigen Artikels erhöht wird.
 * </p>
 *
 * <p>
 *     Wareneingänge werden nicht gelöscht, sondern dienen als Nachweis für Bestandsveränderungen.
 * </p>
 *
 * @author Paula Martin
 */
@Entity
@Table(name = "wareneingang")
@Getter
@Setter
public class Wareneingang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "artikel_id")
    private Artikel artikel;

    @Column(nullable = false)
    private int menge;

    @Column(length = 100)
    private String kommentar;

    @Column(length = 50)
    private String lieferant;

    @Column
    private LocalDate datum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WareneingangStatus status = WareneingangStatus.AUSSTEHEND;

    @ManyToOne
    @JoinColumn(name = "bestellt_von_id", nullable = false)
    private User bestelltVon;

    @Column
    private LocalDateTime bestelltAm;
}
