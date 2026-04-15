package de.fhswf.kassensystem.model;

import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column(length = 100)
    private String bestelltVon;

    @Column
    private LocalDate besetelltAm;
}
