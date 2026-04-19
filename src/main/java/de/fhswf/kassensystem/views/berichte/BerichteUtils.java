package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.html.Span;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Statische Hilfsmethoden für alle Berichte-Panels.
 *
 * <p>Enthält null-sichere Berechnungen, Währungsformatierung (DE-Format),
 * Prozentberechnung sowie eine Factory-Methode für Leer-Spans.
 * Nicht instanziierbar.
 *
 * @author Adrian Krawietz
 */
class BerichteUtils {

    private BerichteUtils() {}

    /**
     * Gibt {@code v} zurück, oder {@link BigDecimal#ZERO} wenn {@code v} {@code null} ist.
     *
     * @param v Wert oder {@code null}
     * @return nie {@code null}
     */
    static BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * Formatiert einen {@link BigDecimal} als deutschen Währungsstring (z.B. {@code "1.234,56€"}).
     *
     * @param v Betrag oder {@code null}
     * @return formatierter String, {@code "0,00€"} bei {@code null}
     */
    static String fp(BigDecimal v) {
        if (v == null) return "0,00€";
        return String.format(java.util.Locale.GERMANY, "%,.2f€", v);
    }

    /**
     * Berechnet den prozentualen Anteil von {@code val} an {@code max} (gerundet).
     * Gibt 0 zurück wenn {@code max} gleich null ist.
     *
     * @param val Zähler
     * @param max Nenner (Maximalwert)
     * @return ganzzahliger Prozentwert (0–100)
     */
    static int pct(BigDecimal val, BigDecimal max) {
        if (max.compareTo(BigDecimal.ZERO) == 0) return 0;
        return val.multiply(BigDecimal.valueOf(100))
                .divide(max, 0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * Erstellt einen dezent gestylten Hinweis-Span für den Leer-Zustand eines Panels.
     *
     * @param text der anzuzeigende Hinweistext
     * @return gestylter {@code Span}
     */
    static Span leerSpan(String text) {
        Span s = new Span(text);
        s.getStyle().set("font-size", "0.8rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return s;
    }
}