package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("VerkaufRepository Tests")
class VerkaufRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VerkaufRepository verkaufRepository;

    private User testKassierer;
    private Artikel testArtikel;

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

        testKassierer = new User();
        testKassierer.setBenutzername("testkassierer");
        testKassierer.setName("Test Kassierer");
        testKassierer.setPassword("gehasht");
        testKassierer.setRolle(Rolle.KASSIERER);
        testKassierer.setAktiv(true);
        entityManager.persist(testKassierer);

        entityManager.flush();
    }

    private Verkauf erstelleVerkauf(LocalDateTime timestamp, Status status, Zahlungsart zahlungsart) {
        Verkaufsposition position = new Verkaufsposition();
        position.setArtikel(testArtikel);
        position.setMenge(1);
        position.setEinzelpreis(new BigDecimal("2.50"));

        Verkauf verkauf = new Verkauf();
        verkauf.setTimestamp(timestamp);
        verkauf.setStatus(status);
        verkauf.setZahlungsart(zahlungsart);
        verkauf.setGesamtsumme(new BigDecimal("2.50"));
        verkauf.setRabatt(BigDecimal.ZERO);
        verkauf.setKassierer(testKassierer);
        verkauf.setPositionen(List.of(position));
        position.setVerkauf(verkauf);

        return verkauf;
    }

    // -------------------------------------------------------------------------
    // findByTimestampBetween
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByTimestampBetween")
    class FindByTimestampBetween {

        @Test
        @DisplayName("Gibt abgeschlossene Verkäufe im Zeitraum zurück")
        void findByTimestampBetween_erfolg() {
            Verkauf verkauf = erstelleVerkauf(
                    LocalDateTime.now(),
                    Status.ABGESCHLOSSEN,
                    Zahlungsart.BAR);
            entityManager.persist(verkauf);
            entityManager.flush();

            List<Verkauf> result = verkaufRepository.findByTimestampBetween(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1));

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Gibt keine stornierten Verkäufe zurück")
        void findByTimestampBetween_storniertWirdGefiltert() {
            Verkauf storniert = erstelleVerkauf(
                    LocalDateTime.now(),
                    Status.STORNIERT,
                    Zahlungsart.BAR);
            entityManager.persist(storniert);
            entityManager.flush();

            List<Verkauf> result = verkaufRepository.findByTimestampBetween(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Gibt keine offenen Verkäufe zurück")
        void findByTimestampBetween_offenWirdGefiltert() {
            Verkauf offen = erstelleVerkauf(
                    LocalDateTime.now(),
                    Status.OFFEN,
                    Zahlungsart.BAR);
            entityManager.persist(offen);
            entityManager.flush();

            List<Verkauf> result = verkaufRepository.findByTimestampBetween(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Gibt keine Verkäufe außerhalb des Zeitraums zurück")
        void findByTimestampBetween_ausserhalb() {
            Verkauf gestern = erstelleVerkauf(
                    LocalDateTime.now().minusDays(2),
                    Status.ABGESCHLOSSEN,
                    Zahlungsart.BAR);
            entityManager.persist(gestern);
            entityManager.flush();

            List<Verkauf> result = verkaufRepository.findByTimestampBetween(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Lädt Positionen und Artikel mit JOIN FETCH")
        void findByTimestampBetween_positionenGeladen() {
            Verkauf verkauf = erstelleVerkauf(
                    LocalDateTime.now(),
                    Status.ABGESCHLOSSEN,
                    Zahlungsart.BAR);
            entityManager.persist(verkauf);
            entityManager.flush();
            entityManager.clear();

            List<Verkauf> result = verkaufRepository.findByTimestampBetween(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1));

            assertThat(result.get(0).getPositionen()).isNotEmpty();
            assertThat(result.get(0).getPositionen().get(0).getArtikel()).isNotNull();
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine Verkäufe im Zeitraum")
        void findByTimestampBetween_leereListe() {
            List<Verkauf> result = verkaufRepository.findByTimestampBetween(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1));

            assertThat(result).isEmpty();
        }
    }
}