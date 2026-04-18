package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.ArtikelNotFoundException;
import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import de.fhswf.kassensystem.repository.ArtikelRepository;
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
@DisplayName("ArtikelService Tests")
class ArtikelServiceTest {

    @Mock
    private ArtikelRepository artikelRepository;

    @InjectMocks
    private ArtikelService artikelService;

    private Artikel testArtikel;

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
    }

    @Nested
    @DisplayName("findArtikelById")
    class FindArtikelById {

        @Test
        @DisplayName("Gibt Artikel zurück wenn ID existiert")
        void findArtikelById_erfolg() {
            when(artikelRepository.findById(1L)).thenReturn(Optional.of(testArtikel));

            Artikel result = artikelService.findArtikelById(1L);

            assertThat(result).isEqualTo(testArtikel);
            verify(artikelRepository).findById(1L);
        }

        @Test
        @DisplayName("Wirft ArtikelNotFoundException wenn ID nicht existiert")
        void findArtikelById_nichtGefunden() {
            when(artikelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artikelService.findArtikelById(99L))
                    .isInstanceOf(ArtikelNotFoundException.class);
        }

        @Test
        @DisplayName("Wirft ArtikelNotFoundException wenn ID null ist")
        void findArtikelById_nullId() {
            assertThatThrownBy(() -> artikelService.findArtikelById(null))
                    .isInstanceOf(ArtikelNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAllArtikel")
    class FindAllArtikel {

        @Test
        @DisplayName("Gibt alle Artikel zurück")
        void findAllArtikel_erfolg() {
            when(artikelRepository.findAll()).thenReturn(List.of(testArtikel));

            List<Artikel> result = artikelService.findAllArtikel();

            assertThat(result).hasSize(1).contains(testArtikel);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine Artikel vorhanden")
        void findAllArtikel_leereListe() {
            when(artikelRepository.findAll()).thenReturn(List.of());

            List<Artikel> result = artikelService.findAllArtikel();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("Gibt Artikel zurück wenn Name gefunden")
        void findByName_erfolg() {
            when(artikelRepository.findByNameContainingIgnoreCase("Kaffee"))
                    .thenReturn(List.of(testArtikel));

            List<Artikel> result = artikelService.findByName("Kaffee");

            assertThat(result).hasSize(1).contains(testArtikel);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn kein Treffer")
        void findByName_keinTreffer() {
            when(artikelRepository.findByNameContainingIgnoreCase("xyz"))
                    .thenReturn(List.of());

            List<Artikel> result = artikelService.findByName("xyz");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Name null ist")
        void findByName_nullName() {
            assertThatThrownBy(() -> artikelService.findByName(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("createArtikel")
    class CreateArtikel {

        @Test
        @DisplayName("Speichert und gibt neuen Artikel zurück")
        void createArtikel_erfolg() {
            when(artikelRepository.save(testArtikel)).thenReturn(testArtikel);

            Artikel result = artikelService.createArtikel(testArtikel);

            assertThat(result).isEqualTo(testArtikel);
            verify(artikelRepository).save(testArtikel);
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Artikel null ist")
        void createArtikel_nullArtikel() {
            assertThatThrownBy(() -> artikelService.createArtikel(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Name null ist")
        void createArtikel_nameNull() {
            testArtikel.setName(null);

            assertThatThrownBy(() -> artikelService.createArtikel(testArtikel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Name leer ist")
        void createArtikel_nameLeer() {
            testArtikel.setName("   ");

            assertThatThrownBy(() -> artikelService.createArtikel(testArtikel))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("leer");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Preis null ist")
        void createArtikel_preisNull() {
            testArtikel.setPreis(null);

            assertThatThrownBy(() -> artikelService.createArtikel(testArtikel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Preis negativ ist")
        void createArtikel_preisNegativ() {
            testArtikel.setPreis(new BigDecimal("-1.00"));

            assertThatThrownBy(() -> artikelService.createArtikel(testArtikel))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("Artikelpreis");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Akzeptiert Preis von 0")
        void createArtikel_preisNull_akzeptiert() {
            testArtikel.setPreis(BigDecimal.ZERO);
            when(artikelRepository.save(testArtikel)).thenReturn(testArtikel);

            Artikel result = artikelService.createArtikel(testArtikel);

            assertThat(result).isEqualTo(testArtikel);
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Kategorie null ist")
        void createArtikel_kategorieNull() {
            testArtikel.setKategorie(null);

            assertThatThrownBy(() -> artikelService.createArtikel(testArtikel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Kategorie");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Mehrwertsteuer null ist")
        void createArtikel_mehrwertsteuerNull() {
            testArtikel.setMehrwertsteuer(null);

            assertThatThrownBy(() -> artikelService.createArtikel(testArtikel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Mehrwertsteuer");

            verify(artikelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateArtikel")
    class UpdateArtikel {

        @Test
        @DisplayName("Aktualisiert und gibt Artikel zurück")
        void updateArtikel_erfolg() {
            when(artikelRepository.save(testArtikel)).thenReturn(testArtikel);

            Artikel result = artikelService.updateArtikel(testArtikel);

            assertThat(result).isEqualTo(testArtikel);
            verify(artikelRepository).save(testArtikel);
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Artikel null ist")
        void updateArtikel_nullArtikel() {
            assertThatThrownBy(() -> artikelService.updateArtikel(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn ID null ist")
        void updateArtikel_idNull() {
            testArtikel.setId(null);

            assertThatThrownBy(() -> artikelService.updateArtikel(testArtikel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Name null ist")
        void updateArtikel_nameNull() {
            testArtikel.setName(null);

            assertThatThrownBy(() -> artikelService.updateArtikel(testArtikel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Name leer ist")
        void updateArtikel_nameLeer() {
            testArtikel.setName("   ");

            assertThatThrownBy(() -> artikelService.updateArtikel(testArtikel))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("leer");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Preis null ist")
        void updateArtikel_preisNull() {
            testArtikel.setPreis(null);

            assertThatThrownBy(() -> artikelService.updateArtikel(testArtikel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(artikelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Preis negativ ist")
        void updateArtikel_preisNegativ() {
            testArtikel.setPreis(new BigDecimal("-1.00"));

            assertThatThrownBy(() -> artikelService.updateArtikel(testArtikel))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("Artikelpreis");

            verify(artikelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteArtikel")
    class DeleteArtikel {

        @Test
        @DisplayName("Deaktiviert Artikel statt ihn zu löschen")
        void deleteArtikel_erfolg() {
            when(artikelRepository.findById(1L)).thenReturn(Optional.of(testArtikel));
            when(artikelRepository.save(testArtikel)).thenReturn(testArtikel);

            artikelService.deleteArtikel(1L);

            assertThat(testArtikel.isAktiv()).isFalse();
            verify(artikelRepository).save(testArtikel);
        }

        @Test
        @DisplayName("Wirft ArtikelNotFoundException wenn ID nicht existiert")
        void deleteArtikel_nichtGefunden() {
            when(artikelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artikelService.deleteArtikel(99L))
                    .isInstanceOf(ArtikelNotFoundException.class);

            verify(artikelRepository, never()).save(any());
        }
    }
}