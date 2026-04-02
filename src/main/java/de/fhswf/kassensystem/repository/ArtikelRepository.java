package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für die Artikel.
 */
@Repository
public interface ArtikelRepository extends JpaRepository<Artikel, Long> {

    /**
     * Gibt alle Artikel zurück, die zu einer gewissen Kategorie gehören.
     * @param kategorie Die Kategorie nach der gesucht werden soll.
     * @return Eine Liste an Artikeln, die zu der Kategorie gehören.
     */
    List<Artikel> findByKategorie(Kategorie kategorie);

    /**
     * Sucht einen Artikel unter der Angabe des Namens. Groß- und Kleinschreibung
     * wird ignoriert.
     * @param name Der Name nach dem gesucht werden soll
     * @return Liste an Artikeln
     */
    List<Artikel> findByNameContainingIgnoreCase(String name);

    /**
     *
     * @param id
     * @return
     */
    Artikel getArtikelById(Long id);

    /**
     *
     * @param minimalbestand
     * @return
     */
    @Query("SELECT a FROM Artikel a WHERE a.bestand < a.minimalbestand")
    List<Artikel> findArtikelUnterMinimalbestand();
}
