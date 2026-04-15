package de.fhswf.kassensystem.model;

import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Wareneingang")
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
