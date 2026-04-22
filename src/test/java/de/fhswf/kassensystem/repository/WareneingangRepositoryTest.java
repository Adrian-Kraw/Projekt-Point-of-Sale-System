package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("WareneingangRepository Tests")
class WareneingangRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WareneingangRepository wareneingangRepository;

    private Artikel testArtikel;
    private User testUser;

    @BeforeEach
    void setUp() {
        Kategorie kategorie = new Kategorie();
        kategorie.setName("Getränke");
        entityManager.persist(kategorie);

        Mehrwertsteuer mwst = new Mehrwertsteuer();
        mwst.setBezeichnung("Regelsteuersatz");
        mwst.setSatz(new BigDecimal("19.00"));
        entityManager.persist(mwst);

        testArtikel = new Artikel();
        testArtikel.setName("Kaffee");
        testArtikel.setPreis(new BigDecimal("2.50"));
        testArtikel.setKategorie(kategorie);
        testArtikel.setMehrwertsteuer(mwst);
        testArtikel.setBestand(10);
        testArtikel.setAktiv(true);
        entityManager.persist(testArtikel);

        testUser = new User();
        testUser.setBenutzername("testkassierer");
        testUser.setName("Test Kassierer");
        testUser.setPassword("gehasht");
        testUser.setRolle(Rolle.KASSIERER);
        testUser.setAktiv(true);
        entityManager.persist(testUser);

        entityManager.flush();
    }

    private Wareneingang erstelleWareneingang(WareneingangStatus status) {
        Wareneingang wareneingang = new Wareneingang();
        wareneingang.setArtikel(testArtikel);
        wareneingang.setMenge(20);
        wareneingang.setStatus(status);
        wareneingang.setBestelltVon(testUser);
        return wareneingang;
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("Gibt ausstehende Wareneingänge zurück")
        void findByStatus_ausstehend() {
            entityManager.persist(erstelleWareneingang(WareneingangStatus.AUSSTEHEND));
            entityManager.persist(erstelleWareneingang(WareneingangStatus.BESTAETIGT));
            entityManager.flush();

            List<Wareneingang> result = wareneingangRepository
                    .findByStatus(WareneingangStatus.AUSSTEHEND);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus())
                    .isEqualTo(WareneingangStatus.AUSSTEHEND);
        }

        @Test
        @DisplayName("Gibt bestätigte Wareneingänge zurück")
        void findByStatus_bestaetigt() {
            entityManager.persist(erstelleWareneingang(WareneingangStatus.AUSSTEHEND));
            entityManager.persist(erstelleWareneingang(WareneingangStatus.BESTAETIGT));
            entityManager.flush();

            List<Wareneingang> result = wareneingangRepository
                    .findByStatus(WareneingangStatus.BESTAETIGT);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus())
                    .isEqualTo(WareneingangStatus.BESTAETIGT);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine Wareneingänge mit dem Status vorhanden")
        void findByStatus_leereListe() {
            entityManager.persist(erstelleWareneingang(WareneingangStatus.BESTAETIGT));
            entityManager.flush();

            List<Wareneingang> result = wareneingangRepository
                    .findByStatus(WareneingangStatus.AUSSTEHEND);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Gibt mehrere Wareneingänge zurück wenn mehrere mit dem Status vorhanden")
        void findByStatus_mehrTreffer() {
            entityManager.persist(erstelleWareneingang(WareneingangStatus.AUSSTEHEND));
            entityManager.persist(erstelleWareneingang(WareneingangStatus.AUSSTEHEND));
            entityManager.flush();

            List<Wareneingang> result = wareneingangRepository
                    .findByStatus(WareneingangStatus.AUSSTEHEND);

            assertThat(result).hasSize(2);
        }
    }
}