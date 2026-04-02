package de.fhswf.kassensystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Repräsentiert eine Kategorie.
 */
@Entity
@Table(name = "Kategorie")
@Getter
@Setter
public class Kategorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;
}
