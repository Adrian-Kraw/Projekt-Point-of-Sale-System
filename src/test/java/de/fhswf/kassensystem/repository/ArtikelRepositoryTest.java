package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ArtikelRepository Tests")
class ArtikelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArtikelRepository artikelRepository;

    private Kategorie testKategorie;
    private Mehrwertsteuer testMwst;

    @BeforeEach
    void setUp() {
        testKategorie = new Kategorie();
        testKategorie.setName("Getränke");
        entityManager.persist(testKategorie);

        testMwst = new Mehrwertsteuer();
        testMwst.setBezeichnung("Regelsteuersatz");
        testMwst.setSatz(new BigDecimal("19.00"));
        entityManager.persist(testMwst);

        entityManager.flush();
    }

    private Artikel erstelleArtikel(String name, int bestand, int minimalbestand, boolean aktiv) {
        Artikel artikel = new Artikel();
        artikel.setName(name);
        artikel.setPreis(new BigDecimal("2.50"));
        artikel.setKategorie(testKategorie);
        artikel.setMehrwertsteuer(testMwst);
        artikel.setBestand(bestand);
        artikel.setMinimalbestand(minimalbestand);
        artikel.setAktiv(aktiv);
        return artikel;
    }

    @Nested
    @DisplayName("findArtikelUnterMinimalbestand")
    class FindArtikelUnterMinimalbestand {

        @Test
        @DisplayName("Gibt Artikel zurück deren Bestand unter Minimalbestand liegt")
        void findArtikelUnterMinimalbestand_erfolg() {
            entityManager.persist(erstelleArtikel("Kaffee", 2, 10, true));
            entityManager.persist(erstelleArtikel("Tee", 15, 10, true));
            entityManager.flush();

            List<Artikel> result = artikelRepository.findArtikelUnterMinimalbestand();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Kaffee");
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn alle Artikel ausreichend Bestand haben")
        void findArtikelUnterMinimalbestand_alleAusreichend() {
            entityManager.persist(erstelleArtikel("Kaffee", 15, 10, true));
            entityManager.flush();

            List<Artikel> result = artikelRepository.findArtikelUnterMinimalbestand();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn Bestand genau dem Minimalbestand entspricht")
        void findArtikelUnterMinimalbestand_genauMinimal() {
            entityManager.persist(erstelleArtikel("Kaffee", 10, 10, true));
            entityManager.flush();

            List<Artikel> result = artikelRepository.findArtikelUnterMinimalbestand();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Gibt auch deaktivierte Artikel zurück die unter Minimalbestand liegen")
        void findArtikelUnterMinimalbestand_deaktiviert() {
            entityManager.persist(erstelleArtikel("Kaffee", 2, 10, false));
            entityManager.flush();

            List<Artikel> result = artikelRepository.findArtikelUnterMinimalbestand();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine Artikel vorhanden")
        void findArtikelUnterMinimalbestand_leereDatenbank() {
            List<Artikel> result = artikelRepository.findArtikelUnterMinimalbestand();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByNameContainingIgnoreCase")
    class FindByNameContainingIgnoreCase {

        @Test
        @DisplayName("Findet Artikel anhand eines Namensfragments")
        void findByNameContainingIgnoreCase_erfolg() {
            entityManager.persist(erstelleArtikel("Kaffee", 10, 5, true));
            entityManager.persist(erstelleArtikel("Tee", 10, 5, true));
            entityManager.flush();

            List<Artikel> result = artikelRepository
                    .findByNameContainingIgnoreCase("Kaff");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Kaffee");
        }

        @Test
        @DisplayName("Suche ist unabhängig von Groß- und Kleinschreibung")
        void findByNameContainingIgnoreCase_grossKleinschreibung() {
            entityManager.persist(erstelleArtikel("Kaffee", 10, 5, true));
            entityManager.flush();

            List<Artikel> result = artikelRepository
                    .findByNameContainingIgnoreCase("kaffee");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn kein Treffer")
        void findByNameContainingIgnoreCase_keinTreffer() {
            entityManager.persist(erstelleArtikel("Kaffee", 10, 5, true));
            entityManager.flush();

            List<Artikel> result = artikelRepository
                    .findByNameContainingIgnoreCase("xyz");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Gibt mehrere Treffer zurück wenn Fragment in mehreren Namen vorkommt")
        void findByNameContainingIgnoreCase_mehrTreffer() {
            entityManager.persist(erstelleArtikel("Kaffee", 10, 5, true));
            entityManager.persist(erstelleArtikel("Kaffeekuchen", 10, 5, true));
            entityManager.persist(erstelleArtikel("Tee", 10, 5, true));
            entityManager.flush();

            List<Artikel> result = artikelRepository
                    .findByNameContainingIgnoreCase("Kaffe");

            assertThat(result).hasSize(2);
        }
    }
}