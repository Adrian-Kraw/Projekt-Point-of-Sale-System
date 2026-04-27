package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import de.fhswf.kassensystem.exception.WareneingangBereitsBestaetigt;
import de.fhswf.kassensystem.exception.WareneingangNotFoundException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import de.fhswf.kassensystem.repository.ArtikelRepository;
import de.fhswf.kassensystem.repository.WareneingangRepository;
import de.fhswf.kassensystem.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LagerService Tests")
class LagerServiceTest {

    @Mock
    private ArtikelRepository artikelRepository;

    @Mock
    private WareneingangRepository wareneingangRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private LagerService lagerService;

    private Artikel testArtikel;
    private Wareneingang testWareneingang;

    @BeforeEach
    void setUp() {
        Kategorie kategorie = new Kategorie();
        kategorie.setId(1L);
        kategorie.setName("Getränke");

        testArtikel = new Artikel();
        testArtikel.setId(1L);
        testArtikel.setName("Kaffee");
        testArtikel.setBestand(10);
        testArtikel.setMinimalbestand(5);
        testArtikel.setKategorie(kategorie);
        testArtikel.setAktiv(true);

        testWareneingang = new Wareneingang();
        testWareneingang.setId(1L);
        testWareneingang.setArtikel(testArtikel);
        testWareneingang.setMenge(20);
        testWareneingang.setStatus(WareneingangStatus.AUSSTEHEND);
    }

    // -------------------------------------------------------------------------
    // getMinimalbestandWarnliste
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getMinimalbestandWarnliste")
    class GetMinimalbestandWarnliste {

        @Test
        @DisplayName("Gibt Artikel zurück die unter Minimalbestand und nicht bestellt sind")
        void getMinimalbestandWarnliste_erfolg() {
            testArtikel.setBestand(2);
            when(wareneingangRepository.findByStatus(WareneingangStatus.AUSSTEHEND))
                    .thenReturn(List.of());
            when(artikelRepository.findArtikelUnterMinimalbestand())
                    .thenReturn(List.of(testArtikel));

            List<Artikel> result = lagerService.getMinimalbestandWarnliste();

            assertThat(result).hasSize(1).contains(testArtikel);
        }

        @Test
        @DisplayName("Filtert Artikel heraus die bereits bestellt sind")
        void getMinimalbestandWarnliste_bereitsBestelltGefiltert() {
            testArtikel.setBestand(2);
            when(wareneingangRepository.findByStatus(WareneingangStatus.AUSSTEHEND))
                    .thenReturn(List.of(testWareneingang));
            when(artikelRepository.findArtikelUnterMinimalbestand())
                    .thenReturn(List.of(testArtikel));

            List<Artikel> result = lagerService.getMinimalbestandWarnliste();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn kein Artikel unter Minimalbestand")
        void getMinimalbestandWarnliste_leereListe() {
            when(wareneingangRepository.findByStatus(WareneingangStatus.AUSSTEHEND))
                    .thenReturn(List.of());
            when(artikelRepository.findArtikelUnterMinimalbestand())
                    .thenReturn(List.of());

            List<Artikel> result = lagerService.getMinimalbestandWarnliste();

            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // bestellungAufgeben
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("bestellungAufgeben")
    class BestellungAufgeben {

//        @Test
//        @DisplayName("Speichert Wareneingang mit Status AUSSTEHEND")
//        void bestellungAufgeben_erfolg() {
//            when(securityUtils.getEingeloggterUser()).thenReturn();
//
//            lagerService.bestellungAufgeben(testWareneingang);
//
//            assertThat(testWareneingang.getStatus())
//                    .isEqualTo(WareneingangStatus.AUSSTEHEND);
//            assertThat(testWareneingang.getBestelltAm())
//                    .isNotNull();
//            verify(wareneingangRepository).save(testWareneingang);
//        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Wareneingang null ist")
        void bestellungAufgeben_nullWareneingang() {
            assertThatThrownBy(() -> lagerService.bestellungAufgeben(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(wareneingangRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Artikel null ist")
        void bestellungAufgeben_artikelNull() {
            testWareneingang.setArtikel(null);

            assertThatThrownBy(() -> lagerService.bestellungAufgeben(testWareneingang))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Artikel");

            verify(wareneingangRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Menge 0 ist")
        void bestellungAufgeben_mengeNull() {
            testWareneingang.setMenge(0);

            assertThatThrownBy(() -> lagerService.bestellungAufgeben(testWareneingang))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("Menge");

            verify(wareneingangRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Menge negativ ist")
        void bestellungAufgeben_mengeNegativ() {
            testWareneingang.setMenge(-1);

            assertThatThrownBy(() -> lagerService.bestellungAufgeben(testWareneingang))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("Menge");

            verify(wareneingangRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // lieferungBestaetigen
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("lieferungBestaetigen")
    class LieferungBestaetigen {

        @Test
        @DisplayName("Erhöht Bestand und setzt Status auf BESTAETIGT")
        void lieferungBestaetigen_erfolg() {
            when(wareneingangRepository.findById(1L))
                    .thenReturn(Optional.of(testWareneingang));

            lagerService.lieferungBestaetigen(1L);

            assertThat(testArtikel.getBestand()).isEqualTo(30);
            assertThat(testWareneingang.getStatus())
                    .isEqualTo(WareneingangStatus.BESTAETIGT);
            assertThat(testWareneingang.getDatum()).isNotNull();
            verify(artikelRepository).save(testArtikel);
            verify(wareneingangRepository).save(testWareneingang);
        }

        @Test
        @DisplayName("Wirft WareneingangNotFoundException wenn ID nicht existiert")
        void lieferungBestaetigen_nichtGefunden() {
            when(wareneingangRepository.findById(99L))
                    .thenReturn(empty());

            assertThatThrownBy(() -> lagerService.lieferungBestaetigen(99L))
                    .isInstanceOf(WareneingangNotFoundException.class);

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft WareneingangBereitsBestaetigt bei Doppelbestätigung")
        void lieferungBestaetigen_bereitsBestaetigt() {
            testWareneingang.setStatus(WareneingangStatus.BESTAETIGT);
            when(wareneingangRepository.findById(1L))
                    .thenReturn(Optional.of(testWareneingang));

            assertThatThrownBy(() -> lagerService.lieferungBestaetigen(1L))
                    .isInstanceOf(WareneingangBereitsBestaetigt.class);

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalStateException wenn Artikel null ist")
        void lieferungBestaetigen_artikelNull() {
            testWareneingang.setArtikel(null);
            when(wareneingangRepository.findById(1L))
                    .thenReturn(Optional.of(testWareneingang));

            assertThatThrownBy(() -> lagerService.lieferungBestaetigen(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Artikel");

            verify(artikelRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // lieferungStornieren
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("lieferungStornieren")
    class LieferungStornieren {

        @Test
        @DisplayName("Löscht ausstehenden Wareneingang")
        void lieferungStornieren_erfolg() {
            when(wareneingangRepository.findById(1L))
                    .thenReturn(Optional.of(testWareneingang));

            lagerService.lieferungStornieren(1L);

            assertThat(testWareneingang.getStatus()).isEqualTo(WareneingangStatus.STORNIERT);
            verify(wareneingangRepository).save(testWareneingang);
            verify(wareneingangRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Wirft WareneingangNotFoundException wenn ID nicht existiert")
        void lieferungStornieren_nichtGefunden() {
            when(wareneingangRepository.findById(99L))
                    .thenReturn(empty());

            assertThatThrownBy(() -> lagerService.lieferungStornieren(99L))
                    .isInstanceOf(WareneingangNotFoundException.class);

            verify(wareneingangRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Wirft WareneingangBereitsBestaetigt wenn bereits bestätigt")
        void lieferungStornieren_bereitsBestaetigt() {
            testWareneingang.setStatus(WareneingangStatus.BESTAETIGT);
            when(wareneingangRepository.findById(1L))
                    .thenReturn(Optional.of(testWareneingang));

            assertThatThrownBy(() -> lagerService.lieferungStornieren(1L))
                    .isInstanceOf(WareneingangBereitsBestaetigt.class);

            verify(wareneingangRepository, never()).deleteById(any());
        }
    }

    // -------------------------------------------------------------------------
    // getAusstehendeLieferungen
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAusstehendeLieferungen")
    class GetAusstehendeLieferungen {

        @Test
        @DisplayName("Gibt alle ausstehenden Wareneingänge zurück")
        void getAusstehendeLieferungen_erfolg() {
            when(wareneingangRepository.findByStatus(WareneingangStatus.AUSSTEHEND))
                    .thenReturn(List.of(testWareneingang));

            List<Wareneingang> result = lagerService.getAusstehendeLieferungen();

            assertThat(result).hasSize(1).contains(testWareneingang);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine ausstehenden Wareneingänge")
        void getAusstehendeLieferungen_leereListe() {
            when(wareneingangRepository.findByStatus(WareneingangStatus.AUSSTEHEND))
                    .thenReturn(List.of());

            List<Wareneingang> result = lagerService.getAusstehendeLieferungen();

            assertThat(result).isEmpty();
        }
    }
}