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
 * BerichteService liefert Auswertungen für Tagesabschluss,
 * Umsatzübersicht und Artikelstatistik.
 */
@Service
public class BerichteService {

    private final VerkaufRepository verkaufRepository;

    public BerichteService(VerkaufRepository verkaufRepository) {
        this.verkaufRepository = verkaufRepository;
    }

    /**
     * Gibt alle Verkäufe in einem Zeitraum zurück.
     */
    public List<Verkauf> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return verkaufRepository.findByTimestampBetween(start, end);
    }

    /**
     * Berechnet den Tagesabschluss für ein bestimmtes Datum.
     * Setzt alle Felder des TagesabschlussDTO korrekt.
     *
     * @param datum Das Datum für den Abschluss
     * @return TagesabschlussDTO mit allen berechneten Werten
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
     * @param tage Anzahl der Tage zurück
     * @return Liste von ArtikelStatistikDTO, nach Menge sortiert
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

        // Nach Menge absteigend sortieren
        statistik.sort((a, b) -> b.getAnzahlVerkauft() - a.getAnzahlVerkauft());

        return statistik;
    }
}