package de.fhswf.kassensystem.model;

import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repräsentiert einen Verkauf im System.
 */
@Entity
@Table(name = "Verkauf")
@Getter
@Setter
public class Verkauf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private BigDecimal gesamtsumme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Zahlungsart zahlungsart;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User kassierer;

    @Column(nullable = false)
    private BigDecimal rabatt;

    @OneToMany(mappedBy = "verkauf", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Verkaufsposition> positionen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
}