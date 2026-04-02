package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerkaufService {

    private VerkaufRepository verkaufRepository;

    public VerkaufService(VerkaufRepository verkaufRepository) {
        this.verkaufRepository = verkaufRepository;
    }

    public Verkauf verkaufStarten() {
        Verkauf verkauf = new Verkauf();
        verkauf.setTimestamp(LocalDateTime.now());
        verkauf.setStatus(Status.OFFEN);
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

    }

    public Verkauf artikelEntfernen(Long verkaufId, Long artikelId) {

    }

    public Verkauf mengeAendern(Long verkaufId, Long artikelId, int neueMenge) {

    }

    public BigDecimal summeBerechnen(Long verkaufId) {

    }

    public void verkaufStornieren(Long verkaufId) {

    }

    public BelegDTO belegAnzeigen() {

    }


}
