package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository für den Datenbankzugriff auf {@link User}-Entitäten.
 *
 * <p>
 *     Stellt neben den Standard-CRUD-Operationen aus {@link JpaRepository} Abfragen
 *     für die Authentifizierung sowie die Benutzerverwaltung bereit.
 * </p>
 *
 * @author Paula Martin, Adrian Krawietz
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Sucht einen Benutzer anhand seines exakten Benutzernamens.
     *
     * @param benutzername der exakte Benutzername des gesuchten Benutzers.
     * @return der Benutzer oder {@code null} wenn kein Benutzer mit dem angegebenen Benutzernamen
     *         existiert.
     */
    User findByBenutzername(String benutzername);
}
