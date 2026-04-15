package de.fhswf.kassensystem.service;

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

@Service
public class LagerService {
    private ArtikelRepository artikelRepository;
    private WareneingangRepository wareneingangRepository;
    private final SecurityUtils securityUtils;

    public LagerService(ArtikelRepository artikelRepository, WareneingangRepository wareneingangRepository, SecurityUtils securityUtils) {
        this.artikelRepository = artikelRepository;
        this.wareneingangRepository = wareneingangRepository;
        this.securityUtils = securityUtils;
    }

    public int getBestand(Long artikelId) {
        Artikel artikel = artikelRepository.getArtikelById(artikelId);
        return artikel.getBestand();
    }

    public Wareneingang wareneingangBuchen(Wareneingang wareneingang) {
        Artikel artikel = wareneingang.getArtikel();
        artikel.setBestand(artikel.getBestand() + wareneingang.getMenge());
        artikelRepository.save(artikel);

        return wareneingangRepository.save(wareneingang);
    }

    public List<Artikel> getMinimalbestandWarnliste() {
        List<Long> bereitsBestellt = wareneingangRepository
                .findByStatus(WareneingangStatus.AUSSTEHEND)
                .stream()
                .map(w -> w.getArtikel().getId())
                .toList();

        return artikelRepository.findArtikelUnterMinimalbestand()
                .stream()
                .filter(a -> !bereitsBestellt.contains(a))
                .toList();
    }

    public void bestellungAufgeben(Wareneingang eingang) {
        eingang.setStatus(WareneingangStatus.AUSSTEHEND);
        eingang.setBestelltVon(securityUtils.getEingeloggterUser());
        eingang.setBestelltAm(LocalDateTime.now());
        wareneingangRepository.save(eingang);
    }

    public void lieferungBestaetigen(Long wareneingangId) {
        Wareneingang wareneingang = wareneingangRepository.findById(wareneingangId)
                .orElseThrow(() -> new IllegalArgumentException("Wareneingang nicht gefunden."));

        Artikel artikel = wareneingang.getArtikel();
        artikel.setBestand(artikel.getBestand() + wareneingang.getMenge());
        artikelRepository.save(artikel);

        wareneingang.setStatus(WareneingangStatus.BESTAETIGT);
        wareneingang.setDatum(LocalDate.now());
        wareneingangRepository.save(wareneingang);
    }

    public void lieferungStornieren(Long wareneingangId) {
        wareneingangRepository.deleteById(wareneingangId);
    }

    public List <Wareneingang> getAusstehendeLieferungen() {
        return wareneingangRepository.findByStatus(WareneingangStatus.AUSSTEHEND);
    }


}
