package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Status;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.VerkaufRepository;
import de.fhswf.kassensystem.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerkaufService Tests")
class VerkaufServiceTest {

    @Mock
    private VerkaufRepository verkaufRepository;

    @Mock
    private ArtikelRepository artikelRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private VerkaufService verkaufService;

    private Artikel testArtikel;
    private Verkaufsposition testPosition;

    @BeforeEach
    void setUp() {
        Kategorie kategorie = new Kategorie();
        kategorie.setId(1L);
        kategorie.setName("Getränke");

        Mehrwertsteuer mwst = new Mehrwertsteuer();
        mwst.setId(1L);
        mwst.setSatz(new BigDecimal("19.00"));

        testArtikel = new Artikel();
        testArtikel.setId(1L);
        testArtikel.setName("Kaffee");
        testArtikel.setPreis(new BigDecimal("2.50"));
        testArtikel.setKategorie(kategorie);
        testArtikel.setMehrwertsteuer(mwst);
        testArtikel.setBestand(10);
        testArtikel.setAktiv(true);

        testPosition = new Verkaufsposition();
        testPosition.setArtikel(testArtikel);
        testPosition.setMenge(2);
        testPosition.setEinzelpreis(new BigDecimal("2.50"));
    }

    // -------------------------------------------------------------------------
    // verkaufKomplett
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("verkaufKomplett")
    class VerkaufKomplett {

        @Test
        @DisplayName("Speichert Verkauf und bucht Bestand ab")
        void verkaufKomplett_erfolg() {
            when(securityUtils.getEingeloggterUser()).thenReturn(Optional.empty());
            when(verkaufRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Verkauf result = verkaufService.verkaufKomplett(
                    List.of(testPosition),
                    Zahlungsart.BAR,
                    BigDecimal.ZERO);

            assertThat(result.getStatus()).isEqualTo(Status.ABGESCHLOSSEN);
            assertThat(result.getZahlungsart()).isEqualTo(Zahlungsart.BAR);
            assertThat(result.getGesamtsumme()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(result.getTimestamp()).isNotNull();
            assertThat(testArtikel.getBestand()).isEqualTo(8);
            verify(artikelRepository).save(testArtikel);
            verify(verkaufRepository).save(any());
        }

        @Test
        @DisplayName("Setzt Kassierer wenn Benutzer eingeloggt ist")
        void verkaufKomplett_mitKassierer() {
            de.fhswf.kassensystem.model.User kassierer =
                    new de.fhswf.kassensystem.model.User();
            kassierer.setId(1L);
            kassierer.setBenutzername("testkassierer");

            when(securityUtils.getEingeloggterUser())
                    .thenReturn(Optional.of(kassierer));
            when(verkaufRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Verkauf result = verkaufService.verkaufKomplett(
                    List.of(testPosition),
                    Zahlungsart.BAR,
                    BigDecimal.ZERO);

            assertThat(result.getKassierer()).isEqualTo(kassierer);
        }

        @Test
        @DisplayName("Verknüpft Positionen bidirektional mit Verkauf")
        void verkaufKomplett_positionenVerknuepft() {
            when(securityUtils.getEingeloggterUser()).thenReturn(Optional.empty());
            when(verkaufRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Verkauf result = verkaufService.verkaufKomplett(
                    List.of(testPosition),
                    Zahlungsart.KARTE,
                    BigDecimal.ZERO);

            assertThat(testPosition.getVerkauf()).isEqualTo(result);
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Positionen null sind")
        void verkaufKomplett_positionenNull() {
            assertThatThrownBy(() -> verkaufService.verkaufKomplett(
                    null,
                    Zahlungsart.BAR,
                    BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(verkaufRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Warenkorb leer ist")
        void verkaufKomplett_warenkorbLeer() {
            assertThatThrownBy(() -> verkaufService.verkaufKomplett(
                    List.of(),
                    Zahlungsart.BAR,
                    BigDecimal.ZERO
                    ))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("leer");

            verify(verkaufRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Rabatt null ist")
        void verkaufKomplett_rabattNull() {
            assertThatThrownBy(() -> verkaufService.verkaufKomplett(
                    List.of(testPosition),
                    Zahlungsart.BAR,
                    null
                    ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rabatt");

            verify(verkaufRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalStateException wenn Rabatt negativ ist")
        void verkaufKomplett_rabattNegativ() {
            assertThatThrownBy(() -> verkaufService.verkaufKomplett(
                    List.of(testPosition),
                    Zahlungsart.BAR,
                    new BigDecimal("-0.10")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("negativ");

            verify(verkaufRepository, never()).save(any());
        }

//        @Test
//        @DisplayName("Wirft BestandUnterschrittenException wenn Bestand nicht ausreicht")
//        void verkaufKomplett_bestandUnterschritten() {
//            testArtikel.setBestand(1);
//            testPosition.setMenge(5);
//
//            assertThatThrownBy(() -> verkaufService.verkaufKomplett(
//                    List.of(testPosition),
//                    Zahlungsart.BAR,
//                    BigDecimal.ZERO,
//                    new BigDecimal("5.00")))
//                    .isInstanceOf(BestandUnterschrittenException.class);
//
//            verify(artikelRepository, never()).save(any());
//            verify(verkaufRepository, never()).save(any());
//        }

//        @Test
//        @DisplayName("Bestand wird nicht abgebucht wenn Transaktion fehlschlägt")
//        void verkaufKomplett_keinTeilspeichern() {
//            testArtikel.setBestand(1);
//
//            Verkaufsposition position2 = new Verkaufsposition();
//            Artikel artikel2 = new Artikel();
//            artikel2.setId(2L);
//            artikel2.setName("Tee");
//            artikel2.setBestand(0);
//            position2.setArtikel(artikel2);
//            position2.setMenge(5);
//            position2.setEinzelpreis(new BigDecimal("1.50"));
//
//            assertThatThrownBy(() -> verkaufService.verkaufKomplett(
//                    List.of(testPosition, position2),
//                    Zahlungsart.BAR,
//                    BigDecimal.ZERO,
//                    new BigDecimal("6.50")))
//                    .isInstanceOf(BestandUnterschrittenException.class);
//
//            verify(artikelRepository, never()).save(any());
//            verify(verkaufRepository, never()).save(any());
//        }
    }
 }