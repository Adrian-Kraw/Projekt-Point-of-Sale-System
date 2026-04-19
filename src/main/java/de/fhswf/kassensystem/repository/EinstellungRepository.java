package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Einstellung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository für den Datenbankzugriff auf {@link Einstellung}-Entitäten.
 *
 * <p>
 *     Der Primärschlüssel ist vom Typ {@code String} (der Einstellungsschlüssel), daher wird
 *     {@code JpaRepository<Einstellung, String>} verwendet.
 * </p>
 *
 * <p>
 *     Die Standard-CRUD-Operationen aus {@link JpaRepository} sind ausreichend, da Einstellungen ausschließlich
 *     über ihren eindeutigen Schlüssel abgerufen werden.
 * </p>
 *
 * @author Adrian Krawietz, Paula Martin
 */
@Repository
public interface EinstellungRepository extends JpaRepository<Einstellung, String> {
}