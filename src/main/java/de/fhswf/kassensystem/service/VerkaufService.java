package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import de.fhswf.kassensystem.repository.UserRepository;
import de.fhswf.kassensystem.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service für die Durchführung und Persistierung von Verkaufsvorgängen.
 *
 * <p>
 *     Kapselt die Geschäftslogik für den Kassiervorgang.
 * </p>
 *
 * @author Paula Martin, Adrian Krawietz
 */
@Service
public class VerkaufService {

    private VerkaufRepository verkaufRepository;
    private ArtikelRepository artikelRepository;
    private SecurityUtils securityUtils;
    private UserRepository userRepository;

    public VerkaufService(VerkaufRepository verkaufRepository,
                          UserRepository userRepository,
                          SecurityUtils securityUtils,
                          ArtikelRepository artikelRepository) {
        this.verkaufRepository = verkaufRepository;
        this.artikelRepository = artikelRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    /**
     * Schließt einen Verkaufsvorgang ab und persistiert ihn atomar.
     *
     * <p>Alle Schritte (Verkauf anlegen, Kassierer zuweisen, Positionen verknüpfen
     * und speichern) erfolgen in einer einzigen Datenbanktransaktion ({@code @Transactional}),
     * um inkonsistente Zustände und Race Conditions bei parallelen Kassenvorgängen
     * zu vermeiden.</p>
     *
     * @param positionen Liste der Verkaufspositionen mit Artikel, Menge und Einzelpreis
     * @param zahlungsart gewählte Zahlungsart (BAR oder KARTE)
     * @param rabatt gewährter prozentualer Rabatt, {@code 0} für keinen Rabatt
     * // @param gesamtsumme berechneter Gesamtbetrag nach Rabattabzug inklusive MwSt
     * @return der persistierte {@link Verkauf} inklusive generierter ID
     */
    @Transactional
    public Verkauf verkaufKomplett(
            List<Verkaufsposition> positionen,
            Zahlungsart zahlungsart,
            BigDecimal rabatt) {

        if (positionen == null) {
            throw new IllegalArgumentException("Positionen dürfen nicht null sein.");
        }
        if (positionen.isEmpty()) {
            throw new UngueltigeEingabeException("Der Warenkorb darf nicht leer sein.");
        }
        if (zahlungsart == null) {
            throw new IllegalArgumentException("Zahlungsart darf nicht null sein.");
        }
//        if (gesamtsumme == null) {
//            throw new IllegalArgumentException("Gesamtsumme darf nicht null sein.");
//        }
//        if (gesamtsumme.signum() < 0) {
//            throw new IllegalStateException("Gesamtsumme muss größer oder gleich 0 sein.");
//        }
        if (rabatt == null) {
            throw new IllegalArgumentException("Rabatt darf nicht null sein.");
        }
        if (rabatt.signum() < 0) {
            throw new IllegalStateException("Rabatt darf nicht kleiner als 0 sein.");
        }

        // Summenberechnung gehört in den Service
        BigDecimal zwischensumme = positionen.stream()
                .map(p -> p.getEinzelpreis().multiply(BigDecimal.valueOf(p.getMenge())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rabattBetrag = rabatt.compareTo(BigDecimal.ZERO) > 0
                ? zwischensumme.multiply(rabatt)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal gesamtsumme = zwischensumme.subtract(rabattBetrag);

        // Bestandsabbuch – innerhalb der Transaktion!
        for (Verkaufsposition pos : positionen) {
            Artikel artikel = pos.getArtikel();
            if (artikel.getBestand() < 999) {
                int neuerBestand = artikel.getBestand() - pos.getMenge();
                if (neuerBestand < 0) {
                    throw new UngueltigeEingabeException(
                            "Nicht genug Bestand für: " + artikel.getName());
                }
                artikel.setBestand(neuerBestand);
                artikelRepository.save(artikel);
            }
        }

        Verkauf verkauf = new Verkauf();
        verkauf.setTimestamp(LocalDateTime.now());
        verkauf.setStatus(Status.ABGESCHLOSSEN);
        verkauf.setZahlungsart(zahlungsart);
        verkauf.setRabatt(rabatt);
        verkauf.setGesamtsumme(gesamtsumme);

        securityUtils.getEingeloggterUser()
                .ifPresent(verkauf::setKassierer);

        // Positionen verknüpfen
        for (Verkaufsposition pos : positionen) {
            pos.setVerkauf(verkauf);
        }
        verkauf.setPositionen(positionen);

        return verkaufRepository.save(verkauf);
    }


}