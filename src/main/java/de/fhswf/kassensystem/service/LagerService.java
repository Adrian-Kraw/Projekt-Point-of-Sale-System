package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import de.fhswf.kassensystem.exception.WareneingangBereitsBestaetigt;
import de.fhswf.kassensystem.exception.WareneingangNotFoundException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.WareneingangRepository;
import de.fhswf.kassensystem.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service für die Lager- und Bestandsverwaltung im Kassensystem.
 *
 * <p>
 *     Verwaltet Wareneingänge und Bestandsprüfungen.
 * </p>
 *
 * @author Paula Martin
 */
@Service
public class LagerService {
    private ArtikelRepository artikelRepository;
    private WareneingangRepository wareneingangRepository;
    private final SecurityUtils securityUtils;

    /**
     * Erstellt eine neue Instanz mit den benötigten Abhängigkeiten.
     *
     * @param artikelRepository Repository für den Datenbankzugriff auf Artikel
     * @param wareneingangRepository Repository für den Datenbankzugriff auf Wareneingänge
     * @param securityUtils Hilfskomponente zum Abrufen des eingeloggten Benutzers.
     */
    public LagerService(ArtikelRepository artikelRepository, WareneingangRepository wareneingangRepository, SecurityUtils securityUtils) {
        this.artikelRepository = artikelRepository;
        this.wareneingangRepository = wareneingangRepository;
        this.securityUtils = securityUtils;
    }

    /**
     * Gibt alle Artikel zurück, deren Bestand den Minimalbestand unterschreitet und für die noch keine
     * ausstehende Bestellung vorliegt.
     *
     * <p>
     *     Artikel mit einem bereits ausstehenden Wareneingang werden bewusst ausgeblendet, um Doppelbestellungen
     *     zu vermeiden.
     * </p>
     *
     * @return Liste der nachbestellpflichtigen Artikel ohne laufende Bestellungen, oder eine leere Liste.
     */
    public List<Artikel> getMinimalbestandWarnliste() {
        List<Long> bereitsBestellt = wareneingangRepository
                .findByStatus(WareneingangStatus.AUSSTEHEND)
                .stream()
                .map(w -> w.getArtikel().getId())
                .toList();

        return artikelRepository.findArtikelUnterMinimalbestand()
                .stream()
                .filter(a -> !bereitsBestellt.contains(a.getId()))
                .toList();
    }

    /**
     * Erfasst einen neuen Wareneingang mit dem Status {@code AUSSTEHEND}.
     *
     * <p>
     *     Setzt automatisch den aktuell eingeloggten Nutzer als Besteller sowie den aktuellen
     *     Zeitpunkt als Erfassungszeitpunkt. Der Artikelbestand wird zu diesem Zeitpunkt noch nicht erhöht.
     * </p>
     *
     * @param eingang der zu erfassende Wareneingang mit Artikel und Menge
     */
    public void bestellungAufgeben(Wareneingang eingang) {
        if (eingang == null) {
            throw new IllegalArgumentException("Wareneingang darf nicht null sein.");
        }
        if (eingang.getArtikel() == null) {
            throw new IllegalArgumentException("Wareneingang muss einem Artikel zugeordnet sein.");
        }
        if (eingang.getMenge() <= 0) {
            throw new UngueltigeEingabeException("Menge muss größer als 0 sein");
        }

        eingang.setStatus(WareneingangStatus.AUSSTEHEND);
        securityUtils.getEingeloggterUser()
                        .ifPresent(eingang::setBestelltVon);
        eingang.setBestelltAm(LocalDateTime.now());
        wareneingangRepository.save(eingang);
    }

    /**
     * Bestätigt einen ausstehenden Wareneingang und erhöht den Artikelbestand.
     *
     * <p>
     *     Der Artikelbestand wird um die im Wareneingang angegebene Menge erhöht. Anschließend wird der Wareneingang
     *     auf {@code BESTAETIGT} gesetzt und das aktuelle Datum als Lieferdatum gespeichert.
     * </p>
     *
     * @param wareneingangId die ID des zu bestätigenden Wareneingangs
     */
    public void lieferungBestaetigen(Long wareneingangId) {
        Wareneingang wareneingang = wareneingangRepository.findById(wareneingangId)
                .orElseThrow(() -> new WareneingangNotFoundException(wareneingangId));

        if (wareneingang.getStatus() == WareneingangStatus.BESTAETIGT) {
            throw new WareneingangBereitsBestaetigt(wareneingangId);
        }

        Artikel artikel = wareneingang.getArtikel();
        if (artikel == null) {
            throw new IllegalStateException(
                    "Wareneingang " + wareneingangId + " hat keinen zugeordneten Artikel.");

        }

        artikel.setBestand(artikel.getBestand() + wareneingang.getMenge());
        artikelRepository.save(artikel);

        wareneingang.setStatus(WareneingangStatus.BESTAETIGT);
        wareneingang.setDatum(LocalDate.now());
        wareneingangRepository.save(wareneingang);
    }

    /**
     * Storniert einen ausstehenden Wareneingang.
     *
     * @param wareneingangId
     */
    public void lieferungStornieren(Long wareneingangId) {
        Wareneingang wareneingang = wareneingangRepository.findById(wareneingangId)
                        .orElseThrow(() -> new WareneingangNotFoundException(wareneingangId));

        if (wareneingang.getStatus() == WareneingangStatus.BESTAETIGT) {
            throw new WareneingangBereitsBestaetigt(wareneingangId);
        }
        wareneingang.setStatus(WareneingangStatus.STORNIERT);
        wareneingangRepository.save(wareneingang);
    }

    /**
     * Gibt alle Wareneingänge mit dem Status {@code AUSSTEHEND} zurück.
     *
     * <p>
     *     Wird verwendet, um dem Manager eine Übersicht über noch nicht bestätigte Lieferungen bereitzustellen.
     * </p>
     *
     * @return Liste aller ausstehenden Wareneingänge, oder eine leere Liste, wenn keine vorhanden sind.
     */
    public List <Wareneingang> getAusstehendeLieferungen() {
        return wareneingangRepository.findByStatus(WareneingangStatus.AUSSTEHEND);
    }


}