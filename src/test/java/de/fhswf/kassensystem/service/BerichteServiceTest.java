package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.dto.ArtikelStatistikDTO;
import de.fhswf.kassensystem.model.dto.TagesabschlussDTO;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BerichteService Tests")
class BerichteServiceTest {

    @Mock
    private VerkaufRepository verkaufRepository;

    @InjectMocks
    private BerichteService berichteService;

    private Verkauf testVerkaufBar;
    private Verkauf testVerkaufKarte;
    private Artikel testArtikel;

    @BeforeEach
    void setUp() {
        Kategorie kategorie = new Kategorie();
        kategorie.setId(1L);
        kategorie.setName("Getränke");

        testArtikel = new Artikel();
        testArtikel.setId(1L);
        testArtikel.setName("Kaffee");
        testArtikel.setKategorie(kategorie);

        Verkaufsposition position = new Verkaufsposition();
        position.setArtikel(testArtikel);
        position.setMenge(2);
        position.setEinzelpreis(new BigDecimal("2.50"));

        testVerkaufBar = new Verkauf();
        testVerkaufBar.setId(1L);
        testVerkaufBar.setTimestamp(LocalDateTime.now());
        testVerkaufBar.setGesamtsumme(new BigDecimal("5.00"));
        testVerkaufBar.setZahlungsart(Zahlungsart.BAR);
        testVerkaufBar.setRabatt(BigDecimal.ZERO);
        testVerkaufBar.setStatus(Status.ABGESCHLOSSEN);
        testVerkaufBar.setPositionen(List.of(position));

        testVerkaufKarte = new Verkauf();
        testVerkaufKarte.setId(2L);
        testVerkaufKarte.setTimestamp(LocalDateTime.now());
        testVerkaufKarte.setGesamtsumme(new BigDecimal("3.00"));
        testVerkaufKarte.setZahlungsart(Zahlungsart.KARTE);
        testVerkaufKarte.setRabatt(BigDecimal.ZERO);
        testVerkaufKarte.setStatus(Status.ABGESCHLOSSEN);
        testVerkaufKarte.setPositionen(List.of(position));
    }

    // -------------------------------------------------------------------------
    // findByTimestampBetween
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByTimestampBetween")
    class FindByTimestampBetween {

        @Test
        @DisplayName("Gibt Verkäufe im Zeitraum zurück")
        void findByTimestampBetween_erfolg() {
            LocalDateTime start = LocalDateTime.now().minusDays(1);
            LocalDateTime end   = LocalDateTime.now();
            when(verkaufRepository.findByTimestampBetween(start, end))
                    .thenReturn(List.of(testVerkaufBar));

            List<Verkauf> result = berichteService.findByTimestampBetween(start, end);

            assertThat(result).hasSize(1).contains(testVerkaufBar);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine Verkäufe im Zeitraum")
        void findByTimestampBetween_leereListe() {
            LocalDateTime start = LocalDateTime.now().minusDays(1);
            LocalDateTime end   = LocalDateTime.now();
            when(verkaufRepository.findByTimestampBetween(start, end))
                    .thenReturn(List.of());

            List<Verkauf> result = berichteService.findByTimestampBetween(start, end);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn start null ist")
        void findByTimestampBetween_startNull() {
            assertThatThrownBy(() -> berichteService
                    .findByTimestampBetween(null, LocalDateTime.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn end null ist")
        void findByTimestampBetween_endNull() {
            assertThatThrownBy(() -> berichteService
                    .findByTimestampBetween(LocalDateTime.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn start nach end liegt")
        void findByTimestampBetween_startNachEnd() {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end   = LocalDateTime.now().minusDays(1);

            assertThatThrownBy(() -> berichteService.findByTimestampBetween(start, end))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("Start");
        }
    }

    // -------------------------------------------------------------------------
    // getTagesabschluss
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getTagesabschluss")
    class GetTagesabschluss {

        @Test
        @DisplayName("Berechnet Tagesabschluss korrekt")
        void getTagesabschluss_erfolg() {
            when(verkaufRepository.findByTimestampBetween(any(), any()))
                    .thenReturn(List.of(testVerkaufBar, testVerkaufKarte));

            TagesabschlussDTO result = berichteService.getTagesabschluss(LocalDate.now());

            assertThat(result.getGesamtumsatz())
                    .isEqualByComparingTo(new BigDecimal("8.00"));
            assertThat(result.getUmsatzBar())
                    .isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(result.getUmsatzKarte())
                    .isEqualByComparingTo(new BigDecimal("3.00"));
            assertThat(result.getAnzahlTransaktionen()).isEqualTo(2);
        }

        @Test
        @DisplayName("Gibt leeren Tagesabschluss zurück wenn keine Verkäufe")
        void getTagesabschluss_keineVerkaeufe() {
            when(verkaufRepository.findByTimestampBetween(any(), any()))
                    .thenReturn(List.of());

            TagesabschlussDTO result = berichteService.getTagesabschluss(LocalDate.now());

            assertThat(result.getGesamtumsatz())
                    .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getUmsatzBar())
                    .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getUmsatzKarte())
                    .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getAnzahlTransaktionen()).isEqualTo(0);
        }

        @Test
        @DisplayName("Behandelt null-Gesamtsumme defensiv als 0")
        void getTagesabschluss_nullGesamtsumme() {
            testVerkaufBar.setGesamtsumme(null);
            when(verkaufRepository.findByTimestampBetween(any(), any()))
                    .thenReturn(List.of(testVerkaufBar));

            TagesabschlussDTO result = berichteService.getTagesabschluss(LocalDate.now());

            assertThat(result.getGesamtumsatz())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Datum null ist")
        void getTagesabschluss_datumNull() {
            assertThatThrownBy(() -> berichteService.getTagesabschluss(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    // -------------------------------------------------------------------------
    // getArtikelStatistik
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getArtikelStatistik")
    class GetArtikelStatistik {

        @Test
        @DisplayName("Berechnet Artikelstatistik korrekt")
        void getArtikelStatistik_erfolg() {
            when(verkaufRepository.findByTimestampBetween(any(), any()))
                    .thenReturn(List.of(testVerkaufBar));

            List<ArtikelStatistikDTO> result = berichteService.getArtikelStatistik(7);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getArtikel()).isEqualTo(testArtikel);
            assertThat(result.get(0).getAnzahlVerkauft()).isEqualTo(2);
            assertThat(result.get(0).getGesamtumsatz())
                    .isEqualByComparingTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("Sortiert nach Verkaufsmenge absteigend")
        void getArtikelStatistik_sortiertAbsteigend() {
            Artikel artikel2 = new Artikel();
            artikel2.setId(2L);
            artikel2.setName("Tee");

            Verkaufsposition position2 = new Verkaufsposition();
            position2.setArtikel(artikel2);
            position2.setMenge(5);
            position2.setEinzelpreis(new BigDecimal("1.50"));

            Verkauf verkaufMitMehr = new Verkauf();
            verkaufMitMehr.setPositionen(List.of(position2));
            verkaufMitMehr.setGesamtsumme(new BigDecimal("7.50"));
            verkaufMitMehr.setZahlungsart(Zahlungsart.BAR);
            verkaufMitMehr.setStatus(Status.ABGESCHLOSSEN);

            when(verkaufRepository.findByTimestampBetween(any(), any()))
                    .thenReturn(List.of(testVerkaufBar, verkaufMitMehr));

            List<ArtikelStatistikDTO> result = berichteService.getArtikelStatistik(7);

            assertThat(result.get(0).getAnzahlVerkauft())
                    .isGreaterThan(result.get(1).getAnzahlVerkauft());
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine Verkäufe")
        void getArtikelStatistik_keineVerkaeufe() {
            when(verkaufRepository.findByTimestampBetween(any(), any()))
                    .thenReturn(List.of());

            List<ArtikelStatistikDTO> result = berichteService.getArtikelStatistik(7);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Überspringt Positionen mit null-Artikel")
        void getArtikelStatistik_nullArtikel() {
            Verkaufsposition positionOhneArtikel = new Verkaufsposition();
            positionOhneArtikel.setArtikel(null);
            positionOhneArtikel.setMenge(1);
            positionOhneArtikel.setEinzelpreis(new BigDecimal("1.00"));

            Verkauf verkaufMitNullArtikel = new Verkauf();
            verkaufMitNullArtikel.setId(3L);
            verkaufMitNullArtikel.setTimestamp(LocalDateTime.now());
            verkaufMitNullArtikel.setGesamtsumme(new BigDecimal("1.00"));
            verkaufMitNullArtikel.setZahlungsart(Zahlungsart.BAR);
            verkaufMitNullArtikel.setRabatt(BigDecimal.ZERO);
            verkaufMitNullArtikel.setStatus(Status.ABGESCHLOSSEN);
            verkaufMitNullArtikel.setPositionen(List.of(positionOhneArtikel));

            when(verkaufRepository.findByTimestampBetween(any(), any()))
                    .thenReturn(List.of(verkaufMitNullArtikel));

            List<ArtikelStatistikDTO> result = berichteService.getArtikelStatistik(7);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn tage <= 0")
        void getArtikelStatistik_tageNull() {
            assertThatThrownBy(() -> berichteService.getArtikelStatistik(0))
                    .isInstanceOf(UngueltigeEingabeException.class);
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn tage negativ")
        void getArtikelStatistik_tageNegativ() {
            assertThatThrownBy(() -> berichteService.getArtikelStatistik(-1))
                    .isInstanceOf(UngueltigeEingabeException.class);
        }
    }
}