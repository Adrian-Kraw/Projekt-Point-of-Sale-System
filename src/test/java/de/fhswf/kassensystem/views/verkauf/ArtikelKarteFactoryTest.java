package de.fhswf.kassensystem.views.verkauf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für {@link ArtikelKarteFactory#iconFuerKategorie(String)}.
 *
 * <p>Prüft das Kategorie-zu-Tabler-Icon-Mapping für alle bekannten
 * Kategorien sowie Grenzfälle (unbekannte Kategorie, null, leerer String,
 * Leerzeichen, Groß-/Kleinschreibung, führende/nachfolgende Leerzeichen).
 */
class ArtikelKarteFactoryTest {

    /**
     * Kategorie "Brot und Brötchen": erwartetes Icon ist ti-bread.
     */
    @Test
    void iconFuerKategorie_BrotUndBroetchen_GibtBreadZurueck() {
        assertEquals("ti-bread", ArtikelKarteFactory.iconFuerKategorie("Brot und Brötchen"));
    }

    /**
     * Kategorie "Kuchen": erwartetes Icon ist ti-cake.
     */
    @Test
    void iconFuerKategorie_Kuchen_GibtCakeZurueck() {
        assertEquals("ti-cake", ArtikelKarteFactory.iconFuerKategorie("Kuchen"));
    }

    /**
     * Kategorie "Teilchen": erwartetes Icon ist ti-cookie.
     */
    @Test
    void iconFuerKategorie_Teilchen_GibtCookieZurueck() {
        assertEquals("ti-cookie", ArtikelKarteFactory.iconFuerKategorie("Teilchen"));
    }

    /**
     * Kategorie "Heiße Getränke": erwartetes Icon ist ti-coffee.
     */
    @Test
    void iconFuerKategorie_HeisseGetraenke_GibtCoffeeZurueck() {
        assertEquals("ti-coffee", ArtikelKarteFactory.iconFuerKategorie("Heiße Getränke"));
    }

    /**
     * Kategorie "Kalte Getränke": erwartetes Icon ist ti-bottle.
     */
    @Test
    void iconFuerKategorie_KalteGetraenke_GibtBottleZurueck() {
        assertEquals("ti-bottle", ArtikelKarteFactory.iconFuerKategorie("Kalte Getränke"));
    }

    /**
     * Unbekannte Kategorie: Fallback-Icon ti-tag wird erwartet.
     */
    @Test
    void iconFuerKategorie_Unbekannt_GibtTagZurueck() {
        assertEquals("ti-tag", ArtikelKarteFactory.iconFuerKategorie("Unbekannte Kategorie"));
    }

    /**
     * null als Eingabe: Fallback-Icon ti-tag wird erwartet, keine NullPointerException.
     */
    @Test
    void iconFuerKategorie_Null_GibtTagZurueck() {
        assertEquals("ti-tag", ArtikelKarteFactory.iconFuerKategorie(null));
    }

    /**
     * Großschreibung wird ignoriert: "KUCHEN" wird als "kuchen" erkannt,
     * erwartetes Icon ist ti-cake.
     */
    @Test
    void iconFuerKategorie_GrossKleinschreibung_WirdIgnoriert() {
        assertEquals("ti-cake", ArtikelKarteFactory.iconFuerKategorie("KUCHEN"));
    }

    /**
     * Leerer String: kein Treffer im Mapping, Fallback-Icon ti-tag wird erwartet.
     */
    @Test
    void iconFuerKategorie_LeererString_GibtTagZurueck() {
        assertEquals("ti-tag", ArtikelKarteFactory.iconFuerKategorie(""));
    }

    /**
     * Nur Leerzeichen: nach trim() ist der String leer, kein Treffer,
     * Fallback-Icon ti-tag wird erwartet.
     */
    @Test
    void iconFuerKategorie_NurLeerzeichen_GibtTagZurueck() {
        assertEquals("ti-tag", ArtikelKarteFactory.iconFuerKategorie("   "));
    }

    /**
     * Führende und nachfolgende Leerzeichen um eine bekannte Kategorie:
     * trim() entfernt sie, "kuchen" wird erkannt, erwartetes Icon ist ti-cake.
     */
    @Test
    void iconFuerKategorie_LeerzeichenRundumKuchen_GibtCakeZurueck() {
        assertEquals("ti-cake", ArtikelKarteFactory.iconFuerKategorie(" Kuchen "));
    }
}