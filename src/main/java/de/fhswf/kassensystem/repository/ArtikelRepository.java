package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für den Datenzugriff auf {@link Artikel}-Entitäten.
 *
 * <p>
 *     Stellt neben den Standard-CRUD-Operationen aus {@link JpaRepository} zusätzliche Abfragen
 *     für die Artikelsuche und die Minimalbestands-Warnliste.
 * </p>
 *
 * @author Paula Martin
 */
@Repository
public interface ArtikelRepository extends JpaRepository<Artikel, Long> {

    /**
     * Gibt alle Artikel zurück, die zu einer bestimmten Kategorie gehören.
     *
     * @param kategorie die Kategorie nach der gefiltert werden soll.
     * @return Liste aller Artikel der angebenen Kategorie oder eine leere Liste, wenn keine Artikel gefunden wurden
     */
    List<Artikel> findByKategorie(Kategorie kategorie);

    /**
     * Sucht Artikel anhand eines Namensfragments. Groß- und Kleinschreibung
     * wird ignoriert.
     *
     * @param name Der Name nach dem gesucht werden soll
     * @return Liste an Artikeln oder eine leere Liste, wenn keine Artikel gefunden werden konnten
     */
    List<Artikel> findByNameContainingIgnoreCase(String name);

    /**
     * Gibt den Artikel mit der angebenen ID zurück.
     *
     * @param id der Datenbankidentifikator des gesuchten Artikels
     * @return der gefundene Artikel oder {@code null} wenn kein Artikel mit der angegebenen ID existiert.
     */
    Artikel findArtikelById(Long id);

    /**
     * Gibt alle Artikel zurück, deren aktueller Bestand den definierten Minimalbestand unterschreitet.
     *
     * <p>
     *     Die Abfrage vergleicht {@code bestand} direkt mit {@code minimalbestand} auf Datenbankebene, sodass
     *     artikelindividuelle Schwellenwerte berücksichtigt werden.
     * </p>
     *
     * @return Liste aller Artikel unterhalb des Minimalbestands, oder eine leere Liste, wenn kein Artikel
     *         nachbestellt werden muss.
     */
    @Query("SELECT a FROM Artikel a WHERE a.bestand < a.minimalbestand")
    List<Artikel> findArtikelUnterMinimalbestand();
}
