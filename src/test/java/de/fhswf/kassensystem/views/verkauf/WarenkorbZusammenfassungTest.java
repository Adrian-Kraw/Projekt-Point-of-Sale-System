package de.fhswf.kassensystem.views.verkauf;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für {@link WarenkorbZusammenfassung#format(BigDecimal)}.
 *
 * <p>Prüft die statische Formatierungsmethode für Währungsbeträge
 * im deutschen Format – inklusive Grenzfälle wie negative Beträge,
 * sehr große Beträge, Rundungsverhalten und null.
 *
 * <p>Voraussetzung: {@code format()} muss {@link java.util.Locale#GERMANY}
 * verwenden und einen null-Check enthalten damit alle Tests korrekt laufen.
 *
 * @author Adrian Krawietz
 */
class WarenkorbZusammenfassungTest {

    /**
     * Normaler Betrag: deutsches Dezimalformat mit Euro-Zeichen wird erwartet.
     */
    @Test
    void format_NormalerBetrag_KorrekteFormatierung() {
        assertEquals("2,49€", WarenkorbZusammenfassung.format(new BigDecimal("2.49")));
    }

    /**
     * Betrag über 1000: Tausenderpunkt und Dezimalkomma korrekt gesetzt.
     */
    @Test
    void format_TausenderTrennzeichen_KorrekteFormatierung() {
        assertEquals("1.234,56€", WarenkorbZusammenfassung.format(new BigDecimal("1234.56")));
    }

    /**
     * Nullbetrag: "0,00€" wird erwartet.
     */
    @Test
    void format_NullBetrag_GibtNullwertZurueck() {
        assertEquals("0,00€", WarenkorbZusammenfassung.format(BigDecimal.ZERO));
    }

    /**
     * null als Eingabe: "0,00€" als sicherer Fallback wird erwartet, keine NullPointerException.
     * Voraussetzung: null-Check in der Methode muss vorhanden sein.
     */
    @Test
    void format_Null_GibtNullwertZurueck() {
        assertEquals("0,00€", WarenkorbZusammenfassung.format(null));
    }

    /**
     * Negativer Betrag: Minuszeichen wird korrekt vorangestellt, z.B. bei Storno.
     */
    @Test
    void format_NegativerBetrag_KorrekteFormatierung() {
        assertEquals("-2,49€", WarenkorbZusammenfassung.format(new BigDecimal("-2.49")));
    }

    /**
     * Sehr großer Betrag: mehrere Tausendertrennpunkte werden korrekt gesetzt.
     */
    @Test
    void format_SehrGroßerBetrag_KorrekteFormatierung() {
        assertEquals("1.000.000,00€", WarenkorbZusammenfassung.format(new BigDecimal("1000000.00")));
    }

    /**
     * Betrag mit mehr als 2 Nachkommastellen: wird auf 2 Stellen gerundet.
     */
    @Test
    void format_MehrereNachkommastellen_WirdGerundet() {
        assertEquals("2,50€", WarenkorbZusammenfassung.format(new BigDecimal("2.499")));
    }
}