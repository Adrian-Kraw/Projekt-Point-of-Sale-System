package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.dto.ArtikelStatistikDTO;
import de.fhswf.kassensystem.model.dto.TagesabschlussDTO;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service für Berichte und Auswertungen im Kassensystem.
 *
 * <p>
 *    Stellt die Auswertungslogik für den Tagesabschluss, die Umsatzübersicht und die Artikelstatistik bereit.
 *    Alle Berechnungen basieren ausschließlich auf Verkäufen mit dem Status {@code ABGESCHLOSSEN}. Stornierte
 *    Verkäufe werden durch das Repository bereits gefiltert.
 * </p>
 *
 * @author Paula Martin, Adrian Krawietz
 */
@Service
public class BerichteService {

    private final VerkaufRepository verkaufRepository;

    /**
     * Erstellt eine neue Instanz mit dem benötigten Repository.
     *
     * @param verkaufRepository Repository für den Datenbankzugriff auf Verkäufe.
     */
    public BerichteService(VerkaufRepository verkaufRepository) {
        this.verkaufRepository = verkaufRepository;
    }

    /**
     * Gibt alle abgeschlossenen Verkäufe innerhalb eines Zeitraums zurück.
     *
     * @param start Beginn des Zeitraums (inklusiv)
     * @param end Ende des Zeitraums
     * @return Liste aller abgeschlossenen Verkäufe im Zeitraum, oder eine leere Liste. wenn
     *         keine gefunden wurden.
     */
    public List<Verkauf> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return verkaufRepository.findByTimestampBetween(start, end);
    }

    /**
     * Berechnet den Tagesabschluss für ein bestimmtes Datum.
     * Setzt alle Felder des TagesabschlussDTO korrekt.
     *
     * <p>
     *     Der Zeitraum umfasst den gesamten Tag von {@code 00:00:00} bis {@code 23:59:59}. Die Umsätze
     *     werden nach Zahlungsart (BAR / KARTE) aufgeschlüsselt.
     * </p>
     *
     * @param datum Das Datum, für das der Tagesabschluss erstellt werden soll
     * @return TagesabschlussDTO mit Gesamtumsatz, Transaktionsanzahl und Aufschlüsselung nach Zahlungsart.
     */
    public TagesabschlussDTO getTagesabschluss(LocalDate datum) {
        LocalDateTime start = datum.atStartOfDay();
        LocalDateTime end   = datum.atTime(23, 59, 59);

        List<Verkauf> verkaeufe = findByTimestampBetween(start, end);

        TagesabschlussDTO dto = new TagesabschlussDTO();
        dto.setDatum(datum);
        dto.setAnzahlTransaktionen(verkaeufe.size());
        dto.setVerkaeufe(verkaeufe);

        BigDecimal umsatzBar   = BigDecimal.ZERO;
        BigDecimal umsatzKarte = BigDecimal.ZERO;
        BigDecimal gesamtUmsatz= BigDecimal.ZERO;

        for (Verkauf v : verkaeufe) {
            BigDecimal summe = v.getGesamtsumme() != null ? v.getGesamtsumme() : BigDecimal.ZERO;
            gesamtUmsatz = gesamtUmsatz.add(summe);

            if (v.getZahlungsart() == Zahlungsart.BAR) {
                umsatzBar = umsatzBar.add(summe);
            } else if (v.getZahlungsart() == Zahlungsart.KARTE) {
                umsatzKarte = umsatzKarte.add(summe);
            }
        }

        dto.setGesamtumsatz(gesamtUmsatz);
        dto.setUmsatzBar(umsatzBar);
        dto.setUmsatzKarte(umsatzKarte);

        return dto;
    }

    /**
     * Berechnet die Artikelstatistik für die letzten n Tage.
     * Sortiert nach Verkaufsmenge (absteigend).
     *
     * <p>
     *     Der Umsatz je Artikel ergibt sich aus dem Snapshot-Einzelpreis der {@link Verkaufsposition}
     *     multipliziert mit der Menge. Damit werden historisch korrekte Werte auch nach Preisänderungen
     *     geliefert.
     * </p>
     *
     * <p>
     *     Artikel mit {@code null}-Positionsliste werden übersprungen, um {@code NullPointerException} bei
     *     inkonsistenten Datensätzen zu vermeiden.
     * </p>
     *
     * @param tage Anzahl der vergangenen Tage, die ausgewertet werden sollen
     * @return Liste von {@link ArtikelStatistikDTO}, absteigend nach verkaufter Menge sortiert, oder eine leere
     *         Liste, wenn keine Verkäufe im Zeitraum vorliegen.
     */
    public List<ArtikelStatistikDTO> getArtikelStatistik(int tage) {
        LocalDateTime start = LocalDateTime.now().minusDays(tage);
        LocalDateTime end   = LocalDateTime.now();

        List<Verkauf> verkaeufe = verkaufRepository.findByTimestampBetween(start, end);

        Map<Artikel, Integer> artikelAnzahl = new HashMap<>();
        Map<Artikel, BigDecimal> artikelUmsatz = new HashMap<>();

        for (Verkauf verkauf : verkaeufe) {
            if (verkauf.getPositionen() == null) continue;
            for (Verkaufsposition position : verkauf.getPositionen()) {
                Artikel artikel = position.getArtikel();
                int menge = position.getMenge();
                BigDecimal umsatz = position.getEinzelpreis()
                        .multiply(BigDecimal.valueOf(menge));

                artikelAnzahl.merge(artikel, menge, Integer::sum);
                artikelUmsatz.merge(artikel, umsatz, BigDecimal::add);
            }
        }

        List<ArtikelStatistikDTO> statistik = new ArrayList<>();
        for (Map.Entry<Artikel, Integer> entry : artikelAnzahl.entrySet()) {
            ArtikelStatistikDTO dto = new ArtikelStatistikDTO();
            dto.setArtikel(entry.getKey());
            dto.setAnzahlVerkauft(entry.getValue());
            dto.setGesamtumsatz(artikelUmsatz.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            statistik.add(dto);
        }

        // Nach verkaufter Menge absteigend sortieren
        statistik.sort((a, b) -> b.getAnzahlVerkauft() - a.getAnzahlVerkauft());

        return statistik;
    }
}