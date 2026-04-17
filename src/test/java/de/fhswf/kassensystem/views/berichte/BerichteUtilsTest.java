package de.fhswf.kassensystem.views.berichte;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für {@link BerichteUtils}.
 *
 * <p>Prüft die drei statischen Hilfsmethoden:
 * {@link BerichteUtils#safe(BigDecimal)}, {@link BerichteUtils#fp(BigDecimal)}
 * und {@link BerichteUtils#pct(BigDecimal, BigDecimal)} – inklusive Grenzfälle
 * wie negative Beträge, sehr große Beträge und Rundungsverhalten.
 *
 * <p>Voraussetzung: {@code BerichteUtils.fp()} muss {@link java.util.Locale#GERMANY}
 * verwenden damit die Formatierung unabhängig vom System-Locale korrekt ist.
 */
class BerichteUtilsTest {

    /**
     * Nicht-null-Wert: safe() gibt denselben Wert zurück.
     */
    @Test
    void safe_MitWert_GibtWertZurueck() {
        assertEquals(new BigDecimal("42.50"), BerichteUtils.safe(new BigDecimal("42.50")));
    }

    /**
     * null: safe() gibt BigDecimal.ZERO zurück.
     */
    @Test
    void safe_MitNull_GibtZeroZurueck() {
        assertEquals(BigDecimal.ZERO, BerichteUtils.safe(null));
    }

    /**
     * Normaler Betrag: deutsches Dezimalformat mit Euro-Zeichen wird erwartet.
     */
    @Test
    void fp_NormalerBetrag_KorrekteFormatierung() {
        assertEquals("2,49€", BerichteUtils.fp(new BigDecimal("2.49")));
    }

    /**
     * Betrag über 1000: Tausenderpunkt und Dezimalkomma korrekt gesetzt.
     */
    @Test
    void fp_TausenderTrennzeichen_KorrekteFormatierung() {
        assertEquals("1.234,56€", BerichteUtils.fp(new BigDecimal("1234.56")));
    }

    /**
     * null: "0,00€" als Nullwert-Darstellung wird erwartet.
     */
    @Test
    void fp_Null_GibtNullwertZurueck() {
        assertEquals("0,00€", BerichteUtils.fp(null));
    }

    /**
     * Negativer Betrag: Minuszeichen wird korrekt vorangestellt.
     */
    @Test
    void fp_NegativerBetrag_KorrekteFormatierung() {
        assertEquals("-2,49€", BerichteUtils.fp(new BigDecimal("-2.49")));
    }

    /**
     * Sehr großer Betrag: mehrere Tausendertrennpunkte werden korrekt gesetzt.
     */
    @Test
    void fp_SehrGroßerBetrag_KorrekteFormatierung() {
        assertEquals("1.000.000,00€", BerichteUtils.fp(new BigDecimal("1000000.00")));
    }

    /**
     * Betrag mit mehr als 2 Nachkommastellen: wird auf 2 Stellen gerundet.
     */
    @Test
    void fp_MehrereNachkommastellen_WirdGerundet() {
        assertEquals("2,50€", BerichteUtils.fp(new BigDecimal("2.499")));
    }

    /**
     * 50 von 100: 50 Prozent wird erwartet.
     */
    @Test
    void pct_HaelfteDerMax_Gibt50Zurueck() {
        assertEquals(50, BerichteUtils.pct(new BigDecimal("50"), new BigDecimal("100")));
    }

    /**
     * Maximalwert ist 0: Division durch null wird vermieden, 0 wird zurückgegeben.
     */
    @Test
    void pct_MaxIstNull_GibtNullZurueck() {
        assertEquals(0, BerichteUtils.pct(new BigDecimal("50"), BigDecimal.ZERO));
    }

    /**
     * Voller Betrag: 100 Prozent wird erwartet.
     */
    @Test
    void pct_VollerBetrag_Gibt100Zurueck() {
        assertEquals(100, BerichteUtils.pct(new BigDecimal("100"), new BigDecimal("100")));
    }

    /**
     * Zähler ist 0: 0 Prozent wird erwartet.
     */
    @Test
    void pct_ZaehlerIstNull_Gibt0Zurueck() {
        assertEquals(0, BerichteUtils.pct(BigDecimal.ZERO, new BigDecimal("100")));
    }

    /**
     * Negativer Zähler: ein negativer Prozentwert wird erwartet.
     */
    @Test
    void pct_NegativerZaehler_GibtNegativenWertZurueck() {
        assertEquals(-50, BerichteUtils.pct(new BigDecimal("-50"), new BigDecimal("100")));
    }
}