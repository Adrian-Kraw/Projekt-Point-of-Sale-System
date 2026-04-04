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
 * TODO: Kommentar ergänzen
 */
@Service
public class BerichteService {
    private final VerkaufRepository verkaufRepository;

    /**
     * Konstruktor für BerichteService
     * @param verkaufRepository
     */
    public BerichteService(VerkaufRepository verkaufRepository) {
        this.verkaufRepository = verkaufRepository;
    }

    /**
     * Todo: Kommentar ergänzen
     *
     * @param start
     * @param end
     * @return
     */
    public List<Verkauf> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return verkaufRepository.findByTimestampBetween(start, end);
    }

    /**
     * Todo: Kommentar ergänzen
     *
     * @param datum
     * @return
     */
    public TagesabschlussDTO getTagesabschluss(LocalDate datum) {
        LocalDateTime start = datum.atStartOfDay();
        LocalDateTime end = datum.atTime(23, 59, 59);
        List<Verkauf> verkaeufe = findByTimestampBetween(start, end);

        TagesabschlussDTO dto = new TagesabschlussDTO();
        dto.setDatum(datum);
        dto.setAnzahlTransaktionen(verkaeufe.size());

        BigDecimal umsatzBar = BigDecimal.ZERO;
        for (Verkauf v : verkaeufe) {
            if (v.getZahlungsart() == Zahlungsart.BAR) {
                umsatzBar = umsatzBar.add(v.getGesamtsumme());
            }
        }

        BigDecimal umsatzKarte = BigDecimal.ZERO;
        for (Verkauf v : verkaeufe) {
            if (v.getZahlungsart() == Zahlungsart.KARTE) {
                umsatzKarte = umsatzKarte.add(v.getGesamtsumme());
            }
        }

        BigDecimal gesamtUmsatz = BigDecimal.ZERO;
        for (Verkauf v : verkaeufe) {
            gesamtUmsatz = gesamtUmsatz.add(v.getGesamtsumme());
        }

        return dto;
    }

    /**
     * Todo: Kommentar ergänzen
     *
     * @return
     */
    public List<ArtikelStatistikDTO> getArtikelStatistik(int tage) {
        LocalDateTime start = LocalDateTime.now().minusDays(tage);
        LocalDateTime end = LocalDateTime.now();
        List<Verkauf> verkaeufe = verkaufRepository.findByTimestampBetween(start, end);

        Map<Artikel, Integer> artikelAnzahl = new HashMap<>();
        for (Verkauf verkauf : verkaeufe) {
            for (Verkaufsposition position : verkauf.getPositionen()) {
                Artikel artikel = position.getArtikel();
                int aktuelleAnzahl = artikelAnzahl.getOrDefault(artikel, 0);
                artikelAnzahl.put(artikel, aktuelleAnzahl + position.getMenge());
            }
        }

        List<ArtikelStatistikDTO> statistik = new ArrayList<>();
        for (Map.Entry<Artikel, Integer> entry : artikelAnzahl.entrySet()) {
            ArtikelStatistikDTO dto = new ArtikelStatistikDTO();
            dto.setArtikel(entry.getKey());
            dto.setAnzahlVerkauft(entry.getValue());
            statistik.add(dto);
        }

        return statistik;
    }
}
