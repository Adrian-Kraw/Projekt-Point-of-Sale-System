package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.KategorieRepository;
import de.fhswf.kassensystem.repository.MehrwertsteuerRepository;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Befüllt die Datenbank beim ersten Start mit Stammdaten.
 * Alle Artikel, Kategorien und MwSt-Sätze werden hier zentral gepflegt.
 * Idempotent: läuft bei jedem Start, fügt aber nur ein wenn der Datensatz
 * noch nicht existiert.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final KategorieRepository kategorieRepository;
    private final MehrwertsteuerRepository mehrwertsteuerRepository;
    private final ArtikelRepository artikelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(KategorieRepository kategorieRepository,
                           MehrwertsteuerRepository mehrwertsteuerRepository,
                           ArtikelRepository artikelRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.kategorieRepository    = kategorieRepository;
        this.mehrwertsteuerRepository = mehrwertsteuerRepository;
        this.artikelRepository      = artikelRepository;
        this.userRepository         = userRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        legeSteuersaetzeAn();
        legeKategorienAn();
        legeArtikelAn();
        legeStandardUserAn();
    }

    // ═══════════════════════════════════════════════════════════
    // MEHRWERTSTEUER
    // ═══════════════════════════════════════════════════════════

    private void legeSteuersaetzeAn() {
        neueMwst("Reduzierter Steuersatz", new BigDecimal("7.00"));
        neueMwst("Normaler Steuersatz",    new BigDecimal("19.00"));
    }

    private void neueMwst(String bezeichnung, BigDecimal satz) {
        if (mehrwertsteuerRepository.findBySatz(satz).isEmpty()) {
            Mehrwertsteuer mwst = new Mehrwertsteuer();
            mwst.setBezeichnung(bezeichnung);
            mwst.setSatz(satz);
            mehrwertsteuerRepository.save(mwst);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // KATEGORIEN
    // ═══════════════════════════════════════════════════════════

    private void legeKategorienAn() {
        neueKategorie("Brot und Brötchen");
        neueKategorie("Kuchen");
        neueKategorie("Teilchen");
        neueKategorie("Sandwiches und belegte Brötchen");
        neueKategorie("Heiße Getränke");
        neueKategorie("Kalte Getränke");
    }

    private void neueKategorie(String name) {
        if (kategorieRepository.findByName(name).isEmpty()) {
            Kategorie kategorie = new Kategorie();
            kategorie.setName(name);
            kategorieRepository.save(kategorie);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ARTIKEL
    // Änderungen an Preisen oder Beständen hier vornehmen.
    // Neue Artikel: neuerArtikel(...) aufrufen.
    // ═══════════════════════════════════════════════════════════

    private void legeArtikelAn() {
        legeBrotUndBroetchenAn();
        legeKuchenAn();
        legeTeilchenAn();
        legeSandwichesAn();
        legeHeisseGetraenkeAn();
        legeKalteGetraenkeAn();
    }

    private void legeBrotUndBroetchenAn() {
        String kat = "Brot und Brötchen";
        neuerArtikel("Roggenbrot",         new BigDecimal("3.49"), kat, 7,  20, 5);
        neuerArtikel("Weizenbrot",         new BigDecimal("3.29"), kat, 7,  20, 5);
        neuerArtikel("Körnerbrot",         new BigDecimal("3.79"), kat, 7,  20, 5);
        neuerArtikel("Sauerteigbrot",      new BigDecimal("3.59"), kat, 7,  20, 5);
        neuerArtikel("Vollkornbrot",       new BigDecimal("4.29"), kat, 7,  15, 3);
        neuerArtikel("Normales Brötchen",  new BigDecimal("0.49"), kat, 7,  50, 20);
        neuerArtikel("Mohnbrötchen",       new BigDecimal("0.59"), kat, 7,  30, 10);
        neuerArtikel("Sesambrötchen",      new BigDecimal("0.59"), kat, 7,  30, 10);
        neuerArtikel("Körnerbrötchen",     new BigDecimal("0.59"), kat, 7,  30, 10);
        neuerArtikel("Laugenbrötchen",     new BigDecimal("0.69"), kat, 7,  30, 10);
    }

    private void legeKuchenAn() {
        String kat = "Kuchen";
        neuerArtikel("Erdbeerkuchen",              new BigDecimal("3.49"), kat, 7, 10, 3);
        neuerArtikel("Mohnkuchen",                 new BigDecimal("2.99"), kat, 7, 10, 3);
        neuerArtikel("Streuselkuchen",             new BigDecimal("2.79"), kat, 7, 10, 3);
        neuerArtikel("Bienenstich",                new BigDecimal("3.29"), kat, 7, 10, 3);
        neuerArtikel("Marmorkuchen",               new BigDecimal("2.49"), kat, 7, 10, 3);
        neuerArtikel("Schwarzwälder Kirschtorte",  new BigDecimal("3.99"), kat, 7,  8, 2);
        neuerArtikel("Käsekuchen",                 new BigDecimal("3.29"), kat, 7, 10, 3);
        neuerArtikel("Apfelkuchen",                new BigDecimal("2.99"), kat, 7, 10, 3);
        neuerArtikel("Donauwelle",                 new BigDecimal("2.79"), kat, 7, 10, 3);
        neuerArtikel("Himbeertorte",               new BigDecimal("3.99"), kat, 7,  8, 2);
    }

    private void legeTeilchenAn() {
        String kat = "Teilchen";
        neuerArtikel("Puddingteilchen",  new BigDecimal("1.29"), kat, 7, 20, 5);
        neuerArtikel("Apfeltasche",      new BigDecimal("1.29"), kat, 7, 20, 5);
        neuerArtikel("Nussecke",         new BigDecimal("1.49"), kat, 7, 20, 5);
        neuerArtikel("Croissant",        new BigDecimal("1.19"), kat, 7, 25, 8);
        neuerArtikel("Schokocroissant",  new BigDecimal("1.39"), kat, 7, 25, 8);
        neuerArtikel("Berliner",         new BigDecimal("1.19"), kat, 7, 25, 8);
        neuerArtikel("Plunderteilchen",  new BigDecimal("1.29"), kat, 7, 20, 5);
        neuerArtikel("Zimtschnecke",     new BigDecimal("1.49"), kat, 7, 20, 5);
        neuerArtikel("Mandelkipferl",    new BigDecimal("1.19"), kat, 7, 20, 5);
        neuerArtikel("Quarkbällchen",    new BigDecimal("0.99"), kat, 7, 25, 8);
        neuerArtikel("Muffin",           new BigDecimal("1.19"), kat, 7, 25, 8);
    }

    private void legeSandwichesAn() {
        String kat = "Sandwiches und belegte Brötchen";
        neuerArtikel("Käsebrötchen",       new BigDecimal("2.49"), kat, 7, 15, 5);
        neuerArtikel("Schnitzelbrötchen",  new BigDecimal("3.49"), kat, 7, 15, 5);
        neuerArtikel("Schinkenbrötchen",   new BigDecimal("2.49"), kat, 7, 15, 5);
        neuerArtikel("Salami-Brötchen",    new BigDecimal("2.29"), kat, 7, 15, 5);
        neuerArtikel("Thunfisch-Sandwich", new BigDecimal("3.29"), kat, 7, 10, 3);
        neuerArtikel("Caprese-Sandwich",   new BigDecimal("3.49"), kat, 7, 10, 3);
        neuerArtikel("Ei-Brötchen",        new BigDecimal("2.29"), kat, 7, 15, 5);
        neuerArtikel("Lachsbrötchen",      new BigDecimal("3.99"), kat, 7, 10, 3);
    }

    private void legeHeisseGetraenkeAn() {
        String kat = "Heiße Getränke";
        neuerArtikel("Kaffee",          new BigDecimal("2.49"), kat, 19, 30, 10);
        neuerArtikel("Cappuccino",      new BigDecimal("2.49"), kat, 19, 30, 10);
        neuerArtikel("Latte Macchiato", new BigDecimal("2.49"), kat, 19, 30, 10);
        neuerArtikel("Matcha Latte",    new BigDecimal("2.49"), kat, 19, 20,  5);
        neuerArtikel("Kakao",           new BigDecimal("2.49"), kat, 19, 40,  8);
        neuerArtikel("Kräutertee",      new BigDecimal("2.49"), kat, 19, 20,  8);
        neuerArtikel("Grüner Tee",      new BigDecimal("2.49"), kat, 19, 30,  8);
        neuerArtikel("Schwarzer Tee",   new BigDecimal("2.49"), kat, 19, 60,  8);
        neuerArtikel("Früchtetee",      new BigDecimal("2.49"), kat, 19, 50,  8);
        neuerArtikel("Espresso",        new BigDecimal("2.49"), kat, 19, 40, 10);
    }

    private void legeKalteGetraenkeAn() {
        String kat = "Kalte Getränke";
        neuerArtikel("Mineralwasser",  new BigDecimal("1.49"), kat, 19, 60, 10);
        neuerArtikel("Stilles Wasser", new BigDecimal("1.49"), kat, 19, 80, 10);
        neuerArtikel("Cola",           new BigDecimal("1.49"), kat, 19, 40, 10);
        neuerArtikel("Cola Zero",      new BigDecimal("1.49"), kat, 19, 60, 10);
        neuerArtikel("Cola Light",     new BigDecimal("1.49"), kat, 19, 30, 10);
        neuerArtikel("Sprite",         new BigDecimal("1.49"), kat, 19, 40, 10);
        neuerArtikel("Fanta",          new BigDecimal("1.49"), kat, 19, 40, 10);
        neuerArtikel("Apfelschorle",   new BigDecimal("1.49"), kat, 19, 50, 10);
        neuerArtikel("Capri Sonne",    new BigDecimal("1.49"), kat, 19, 60, 15);
        neuerArtikel("Durstlöscher",   new BigDecimal("1.49"), kat, 19, 80, 15);
    }

    private void neuerArtikel(String name, BigDecimal preis, String kategorienName,
                              int mwstSatz, int bestand, int minimalbestand) {
        if (artikelRepository.findByNameContainingIgnoreCase(name).stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(name))) {
            return;
        }
        Kategorie kategorie = kategorieRepository.findByName(kategorienName)
                .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + kategorienName));
        Mehrwertsteuer mwst = mehrwertsteuerRepository
                .findBySatz(new BigDecimal(mwstSatz + ".00"))
                .orElseThrow(() -> new RuntimeException("MwSt-Satz nicht gefunden: " + mwstSatz));

        Artikel artikel = new Artikel();
        artikel.setName(name);
        artikel.setPreis(preis);
        artikel.setKategorie(kategorie);
        artikel.setMehrwertsteuer(mwst);
        artikel.setBestand(bestand);
        artikel.setMinimalbestand(minimalbestand);
        artikel.setAktiv(true);
        artikelRepository.save(artikel);
    }

    // ═══════════════════════════════════════════════════════════
    // STANDARD-USER
    // ═══════════════════════════════════════════════════════════

    private void legeStandardUserAn() {
        if (userRepository.findByBenutzername("manager") == null) {
            User manager = new User();
            manager.setBenutzername("manager");
            manager.setName("Manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRolle(Rolle.MANAGER);
            manager.setAktiv(true);
            userRepository.save(manager);
        }
        if (userRepository.findByBenutzername("kassierer") == null) {
            User kassierer = new User();
            kassierer.setBenutzername("kassierer");
            kassierer.setName("Kassierer");
            kassierer.setPassword(passwordEncoder.encode("kassierer123"));
            kassierer.setRolle(Rolle.KASSIERER);
            kassierer.setAktiv(true);
            userRepository.save(kassierer);
        }
    }
}
