package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.dto.BelegDTO;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerkaufService {

    private VerkaufRepository verkaufRepository;
    private ArtikelRepository artikelRepository;
    private UserRepository userRepository;

    public VerkaufService(VerkaufRepository verkaufRepository,
                          ArtikelRepository artikelRepository,
                          UserRepository userRepository) {
        this.verkaufRepository = verkaufRepository;
        this.artikelRepository = artikelRepository;
        this.userRepository = userRepository;
    }

    public Verkauf verkaufStarten() {
        Verkauf verkauf = new Verkauf();
        verkauf.setTimestamp(LocalDateTime.now());
        verkauf.setStatus(Status.OFFEN);
        // Defaultwerte damit NOT-NULL Constraints nicht verletzt werden
        verkauf.setGesamtsumme(java.math.BigDecimal.ZERO);
        verkauf.setRabatt(java.math.BigDecimal.ZERO);
        verkauf.setZahlungsart(de.fhswf.kassensystem.model.enums.Zahlungsart.BAR);
        // Eingeloggten Kassierer setzen
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            de.fhswf.kassensystem.model.User kassierer =
                    userRepository.findByBenutzername(auth.getName());
            if (kassierer != null) {
                verkauf.setKassierer(kassierer);
            }
        }
        return verkaufRepository.save(verkauf);
    }

    public Verkauf artikelHinzufuegen(Long verkaufId, Artikel artikel, int menge) {
        Verkauf verkauf = verkaufRepository.getVerkaufById(verkaufId);

        Verkaufsposition verkaufsposition = new Verkaufsposition();
        verkaufsposition.setArtikel(artikel);
        verkaufsposition.setMenge(menge);
        verkaufsposition.setEinzelpreis(artikel.getPreis());
        verkaufsposition.setVerkauf(verkauf);

        List<Verkaufsposition> positionen = verkauf.getPositionen();
        if (positionen == null) {
            positionen = new java.util.ArrayList<>();
            verkauf.setPositionen(positionen);
        }
        positionen.add(verkaufsposition);

        return verkaufRepository.save(verkauf);
    }

    public Verkauf verkaufAbschliessen(Long verkaufId, Zahlungsart zahlungsart, BigDecimal rabatt, BigDecimal gesamtsumme) {
        Verkauf verkauf = verkaufRepository.getVerkaufById(verkaufId);
        verkauf.setStatus(Status.ABGESCHLOSSEN);
        verkauf.setZahlungsart(zahlungsart);
        verkauf.setRabatt(rabatt);
        verkauf.setGesamtsumme(gesamtsumme);
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


    /**
     * Legt einen kompletten Verkauf in einer einzigen Transaktion an.
     * Vermeidet mehrfache DB-Roundtrips und Race Conditions.
     */
    @Transactional
    public Verkauf verkaufKomplett(
            java.util.List<de.fhswf.kassensystem.model.Verkaufsposition> positionen,
            Zahlungsart zahlungsart,
            BigDecimal rabatt,
            BigDecimal gesamtsumme) {

        Verkauf verkauf = new Verkauf();
        verkauf.setTimestamp(java.time.LocalDateTime.now());
        verkauf.setStatus(Status.ABGESCHLOSSEN);
        verkauf.setZahlungsart(zahlungsart);
        verkauf.setRabatt(rabatt);
        verkauf.setGesamtsumme(gesamtsumme);

        // Kassierer aus SecurityContext
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            de.fhswf.kassensystem.model.User kassierer =
                    userRepository.findByBenutzername(auth.getName());
            if (kassierer != null) verkauf.setKassierer(kassierer);
        }

        // Positionen verknüpfen
        for (de.fhswf.kassensystem.model.Verkaufsposition pos : positionen) {
            pos.setVerkauf(verkauf);
        }
        verkauf.setPositionen(positionen);

        return verkaufRepository.save(verkauf);
    }


}