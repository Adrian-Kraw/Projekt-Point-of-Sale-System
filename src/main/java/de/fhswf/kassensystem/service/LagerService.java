package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.WareneingangRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LagerService {
    private ArtikelRepository artikelRepository;
    private WareneingangRepository wareneingangRepository;

    public LagerService(ArtikelRepository artikelRepository, WareneingangRepository wareneingangRepository) {
        this.artikelRepository = artikelRepository;
        this.wareneingangRepository = wareneingangRepository;
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
        return artikelRepository.findArtikelUnterMinimalbestand();
    }
}
