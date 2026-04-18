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
     * Sucht Artikel anhand eines Namensfragments. Groß- und Kleinschreibung
     * wird ignoriert.
     *
     * @param name Der Name nach dem gesucht werden soll
     * @return Liste an Artikeln oder eine leere Liste, wenn keine Artikel gefunden werden konnten
     */
    List<Artikel> findByNameContainingIgnoreCase(String name);

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
