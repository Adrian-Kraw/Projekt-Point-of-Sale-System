package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.BestandUnterschrittenException;
import de.fhswf.kassensystem.exception.LeerWarenkorbException;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    private UserRepository userRepository;

    public VerkaufService(VerkaufRepository verkaufRepository, UserRepository userRepository) {
        this.verkaufRepository = verkaufRepository;
        this.userRepository = userRepository;
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
     * @param gesamtsumme berechneter Gesamtbetrag nach Rabattabzug inklusive MwSt
     * @return der persistierte {@link Verkauf} inklusive generierter ID
     */
    @Transactional
    public Verkauf verkaufKomplett(
            java.util.List<de.fhswf.kassensystem.model.Verkaufsposition> positionen,
            Zahlungsart zahlungsart,
            BigDecimal rabatt,
            BigDecimal gesamtsumme) {

        if (positionen == null) {
            throw new IllegalArgumentException("Positionen dürfen nicht null sein.");
        }
        if (positionen.isEmpty()) {
            throw new LeerWarenkorbException();
        }
        if (zahlungsart == null) {
            throw new IllegalArgumentException("Zahlungsart darf nicht null sein.");
        }
        if (gesamtsumme == null) {
            throw new IllegalArgumentException("Gesamtsumme darf nicht null sein.");
        }
        if (gesamtsumme.signum() < 0) {
            throw new IllegalArgumentException("Gesamtsumme muss größer oder gleich 0 sein.");
        }
        if (rabatt == null) {
            throw new IllegalArgumentException("Rabatt darf nicht null sein.");
        }
        if (rabatt.signum() < 0) {
            throw new IllegalArgumentException("Rabatt darf nicht kleiner als 0 sein.");
        }

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