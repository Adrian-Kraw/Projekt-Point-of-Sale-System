package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.ArtikelNotFoundException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service für die Verwaltung von Artikeln im Kassensystem.
 *
 * <p>
 *     Kapselt die Geschäftslogik für die Artikelstammdaten und delegiert Datenbankzugriffe an das
 *     {@link ArtikelRepository}.
 * </p>
 */
@Service
public class ArtikelService {
    private final ArtikelRepository artikelRepository;

    /**
     * Konstruktor zur Erstellung eines ArtikelService-Objekts.
     * @param artikelRepository ArtikelRepository
     */
    public ArtikelService(ArtikelRepository artikelRepository) {
        this.artikelRepository = artikelRepository;
    }

    /**
     * Sucht einen Artikel unter der genauen Angabe einer ID.
     * @param id Die id des Artikels
     * @return Den Artikel mit der ID
     */
    public Artikel findArtikelById(Long id) {
        if (id == null) {
            throw new ArtikelNotFoundException(null);
        }
        return artikelRepository.findById(id)
                .orElseThrow(() -> new ArtikelNotFoundException(id));
    }

    /**
     * Gibt alle Artikel zurück.
     * @return Liste aller Artikel.
     */
    public List<Artikel> findAllArtikel() {
        return artikelRepository.findAll();
    }

    /**
     * Gibt einen Artikel unter Angabe des Namens zurück
     * @param name Name des Artikels
     * @return Gibt die Artikel zurück oder eine leere Liste.
     */
    public List<Artikel> findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Suchbegriff darf nicht null sein.");
        }
        return artikelRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Erstellt einen neuen Artikel.
     * @param artikel Der Artikel, der hinzugefügt werden soll.
     * @return Gibt den Artikel zurück, der gespeichert werden soll.
     */
    public Artikel createArtikel(Artikel artikel) {
        validiereArtikel(artikel);
        return artikelRepository.save(artikel);
    }

    /**
     * Updatet einen Artikel.
     * @param artikel Der Artikel, der bearbeitet werden soll.
     * @return Gibt den Artikel zurück, der bearbeitet werden soll.
     */
    public Artikel updateArtikel(Artikel artikel) {
        validiereArtikel(artikel);
        if (artikel.getId () == null) {
            throw new IllegalArgumentException("Artikel ID darf beim Update nicht null sein");
        }
        return artikelRepository.save(artikel);
    }

    /**
     * "Löscht" einen Artikel. Der Artikel wird nicht wirklich gelöscht,
     * sondern nur deaktiviert.
     * @param id Die Id des Artikels.
     */
    public void deleteArtikel(Long id) {
        Artikel artikel = findArtikelById(id);
        artikel.setAktiv(false);
        artikelRepository.save(artikel);
    }

    /**
     * Validiert einen Artikel auf Pflichtfelder.
     *
     * @param artikel der zu validierende Artikel
     */
    private void validiereArtikel(Artikel artikel) {
        if (artikel == null) {
            throw new IllegalArgumentException("Artikel darf nicht null sein.");
        }

        if (artikel.getName() == null || artikel.getName().isBlank()) {
            throw new IllegalArgumentException("Artikelname darf nicht leer sein.");
        }

        if (artikel.getPreis() == null || artikel.getPreis().signum() < 0) {
            throw new IllegalArgumentException("Artikelpreis muss größer oder gleich 0 sein.");
        }

        if (artikel.getKategorie() == null) {
            throw new IllegalArgumentException("Artikel muss einer Kategorie zugeordnet sein.");
        }

        if (artikel.getMehrwertsteuer() == null) {
            throw new IllegalArgumentException("Artikel muss einen Mehrwertsteuersatz zugeordnet sein.");
        }
    }
}
