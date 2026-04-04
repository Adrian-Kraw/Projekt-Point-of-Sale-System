package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.dto.BelegDTO;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerkaufService {

    private VerkaufRepository verkaufRepository;
    private ArtikelRepository artikelRepository;

    public VerkaufService(VerkaufRepository verkaufRepository, ArtikelRepository artikelRepository) {
        this.verkaufRepository = verkaufRepository;
        this.artikelRepository = artikelRepository;
    }

    public Verkauf verkaufStarten() {
        Verkauf verkauf = new Verkauf();
        verkauf.setTimestamp(LocalDateTime.now());
        verkauf.setStatus(Status.OFFEN);
        // Offen: Aktuellen User holen
        return verkaufRepository.save(verkauf);
    }

    public Verkauf artikelHinzufuegen(Long verkaufId, Artikel artikel, int menge) {
        Verkauf verkauf = verkaufRepository.getVerkaufById(verkaufId);
        Verkaufsposition verkaufsposition = new Verkaufsposition();
        verkaufsposition.setArtikel(artikel);
        verkaufsposition.setMenge(menge);

        List<Verkaufsposition> positionen = verkauf.getPositionen();
        positionen.add(verkaufsposition);

        return verkaufRepository.save(verkauf);
    }

    public Verkauf verkaufAbschliessen(Long verkaufId, Zahlungsart zahlungsart, BigDecimal rabatt) {
        Verkauf verkauf = verkaufRepository.getVerkaufById(verkaufId);
        verkauf.setStatus(Status.ABGESCHLOSSEN);
        verkauf.setZahlungsart(zahlungsart);
        verkauf.setRabatt(rabatt);
        
        return verkaufRepository.save(verkauf);
    }

    public Verkauf artikelEntfernen(Long verkaufId, Long artikelId) {
        Verkauf verkauf = verkaufRepository.getVerkaufById(verkaufId);

        Verkaufsposition zuEntfernen = null;
        for (Verkaufsposition position : verkauf.getPositionen()) {
            if (position.getArtikel().getId().equals(artikelId)) {
                zuEntfernen = position;
                break;
            }
        }

        if (zuEntfernen != null) {
            verkauf.getPositionen().remove(zuEntfernen);
        }

        return verkaufRepository.save(verkauf);
        
    }   

    public Verkauf mengeAendern(Long verkaufId, Long artikelId, int neueMenge) {
        Verkauf verkauf = verkaufRepository.findById(verkaufId)
                .orElseThrow(() -> new RuntimeException("Verkauf nicht gefunden"));

        for (Verkaufsposition position : verkauf.getPositionen()) {
            if (position.getArtikel().getId().equals(artikelId)) {
                position.setMenge(neueMenge);
            }
        }

        return verkaufRepository.save(verkauf);
    }

    public BigDecimal summeBerechnen(Long verkaufId) {
        Verkauf verkauf = verkaufRepository.getVerkaufById(verkaufId);
        BigDecimal gesamtSumme = BigDecimal.ZERO;
        
        for (Verkaufsposition position : verkauf.getPositionen()) {
            gesamtSumme = gesamtSumme.add((position.getEinzelpreis().multiply(BigDecimal.valueOf(position.getMenge()))));
        }

        return gesamtSumme;
    }

    public void verkaufStornieren(Long verkaufId) {
        Verkauf verkauf = verkaufRepository.getVerkaufById(verkaufId);
        verkauf.setStatus(Status.STORNIERT);
    }

    public BelegDTO belegAnzeigen(Long verkaufId) {
        Verkauf verkauf = verkaufRepository.findById(verkaufId)
                .orElseThrow(() -> new RuntimeException("Verkauf nicht gefunden"));
        BelegDTO dto = new BelegDTO();
        dto.setTimestamp(verkauf.getTimestamp());
        dto.setPositionen(verkauf.getPositionen());
        dto.setGesamtsumme(verkauf.getGesamtsumme());
        dto.setZahlungsart(verkauf.getZahlungsart());
        dto.setRabatt(verkauf.getRabatt());
        dto.setKassierer(verkauf.getKassierer());

        return dto;
    }


}
