package de.fhswf.kassensystem.model;

import de.fhswf.kassensystem.model.enums.Rolle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Repräsentiert einen Benutzer im System.
 */
@Entity
@Table(name = "User")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String benutzername;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rolle rolle;

    @Column(nullable = false)
    private boolean aktiv;
}
