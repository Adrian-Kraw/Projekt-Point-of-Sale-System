package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.Einstellung;
import de.fhswf.kassensystem.repository.EinstellungRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Verwaltet systemweite Einstellungen (Key-Value in DB).
 */
@Service
public class EinstellungService {

    private static final String KEY_BON_ZIELWERT = "bon_zielwert";

    private final EinstellungRepository repository;

    public EinstellungService(EinstellungRepository repository) {
        this.repository = repository;
    }

    public BigDecimal getBonZielwert() {
        return repository.findById(KEY_BON_ZIELWERT)
                .map(e -> {
                    try { return new BigDecimal(e.getWert()); }
                    catch (Exception ex) { return BigDecimal.ZERO; }
                })
                .orElse(BigDecimal.ZERO);
    }

    public void setBonZielwert(BigDecimal wert) {
        Einstellung e = repository.findById(KEY_BON_ZIELWERT)
                .orElseGet(() -> {
                    Einstellung neu = new Einstellung();
                    neu.setSchluessel(KEY_BON_ZIELWERT);
                    return neu;
                });
        e.setWert(wert != null ? wert.toPlainString() : "0");
        repository.save(e);
    }
}