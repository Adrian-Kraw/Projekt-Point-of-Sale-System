package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.html.Span;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Statische Hilfsmethoden für alle Berichte-Panels.
 */
class BerichteUtils {

    private BerichteUtils() {}

    static BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    static String fp(BigDecimal v) {
        if (v == null) return "0,00€";
        return String.format("%,.2f€", v).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    static int pct(BigDecimal val, BigDecimal max) {
        if (max.compareTo(BigDecimal.ZERO) == 0) return 0;
        return val.multiply(BigDecimal.valueOf(100))
                .divide(max, 0, RoundingMode.HALF_UP).intValue();
    }

    static Span leerSpan(String text) {
        Span s = new Span(text);
        s.getStyle().set("font-size", "0.8rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return s;
    }
}
