package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Kategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository für den Datenbankzugriff auf {@link Kategorie}-Entitäten.
 *
 * <p>
 *     Stellt neben den Standard-CRUD-Operationen aus {@link JpaRepository} eine Suche nach
 *     Kategorienamen bereit, die zum Beispiel bei der Validierung auf Duplikate beim anlegen einer
 *     neuen Kategorie genutzt werden kann.
 * </p>
 *
 * @author Adrian Krawietz, Paula Martin
 */
@Repository
public interface KategorieRepository extends JpaRepository<Kategorie, Long> {

    /**
     * Sucht eine Kategorie anhand ihres exakten Namens.
     *
     * @param name der exakte Name der gesuchten Kategorie
     * @return ein {@code Optional} mit der gefundenen Kategorie, oder {@code Optional.empty()} wenn keine
     *         Kategorie mit dem angegebenen Namen existiert.
     */
    Optional<Kategorie> findByName(String name);
}
