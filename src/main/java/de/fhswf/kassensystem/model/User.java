package de.fhswf.kassensystem.model;

import de.fhswf.kassensystem.model.enums.Rolle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Repräsentiert einen Benutzer im Kassensystem.
 *
 * <p>
 *     Benutzer können sich im System anmelden und erhalten basierend auf ihrer {@link Rolle} unterschiedliche
 *     Zugriffsrechte, die über Spring Security durchgesetzt werden.
 * </p>
 *
 * <p>
 *     Die Tabelle wurde bewusst {@code kassensystem_user} benannt, da {@code user} in PostgreSQL ein
 *     reserviertes Schlüsselwort ist.
 * </p>
 *
 * <p>
 *     Passwörter werden in der Datenbank nie im Klartext gespeichert.
 * </p>
 *
 * @author Paula Martin
 */
@Entity
@Table(name = "kassensystem_user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true)
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
