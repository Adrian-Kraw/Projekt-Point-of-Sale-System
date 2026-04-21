package de.fhswf.kassensystem.views.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Wiederverwendbare Statistik-Karte mit Label, Zahlenwert und Icon-Box.
 *
 * <p>Wird in {@link de.fhswf.kassensystem.views.artikel.ArtikelView}
 * und implizit über {@link de.fhswf.kassensystem.views.lager.LagerView} eingesetzt.
 * Im Warnmodus ({@code warnung = true}) werden Schrift und Icon-Hintergrund rot dargestellt.
 *
 * @author Adrian Krawietz
 */
public class StatistikKarte extends VerticalLayout {

    /**
     * Erstellt eine Statistik-Karte.
     *
     * @param label    Beschriftung der Karte (z.B. "Gesamtartikel")
     * @param wert     anzuzeigender Zahlenwert als String (z.B. "42")
     * @param iconName Material-Symbols-Icon-Name (z.B. "inventory_2")
     * @param warnung  {@code true} für rote Warndarstellung (z.B. bei niedrigem Bestand)
     */
    public StatistikKarte(String label, String wert, String iconName, boolean warnung) {
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("flex", "1").set("background", "white")
                .set("border-radius", "1.25rem").set("padding", "1.5rem").set("gap", "0.5rem");

        add(buildLabel(label, warnung), buildWertZeile(wert, iconName, warnung));
    }

    /**
     * Erstellt den Beschriftungs-Span in Großbuchstaben.
     *
     * @param label   der Beschriftungstext
     * @param warnung {@code true} für rote Farbe
     */
    private Span buildLabel(String label, boolean warnung) {
        Span lbl = new Span(label);
        lbl.getStyle()
                .set("font-size", "0.65rem").set("font-weight", "800")
                .set("text-transform", "uppercase").set("letter-spacing", "0.1em")
                .set("color", warnung ? "#ba1a1a" : "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return lbl;
    }

    /**
     * Erstellt die untere Zeile mit dem großen Zahlenwert links und der Icon-Box rechts.
     *
     * @param wert     der anzuzeigende Wert
     * @param iconName Icon-Name
     * @param warnung  {@code true} für rote Schriftfarbe
     */
    private HorizontalLayout buildWertZeile(String wert, String iconName, boolean warnung) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("justify-content", "space-between").set("align-items", "flex-end")
                .set("width", "100%");

        Span wertSpan = new Span(wert);
        wertSpan.getStyle()
                .set("font-size", "2.5rem").set("font-weight", "900")
                .set("color", warnung ? "#ba1a1a" : "#1a1a2e")
                .set("letter-spacing", "-0.025em").set("line-height", "1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        zeile.add(wertSpan, buildIconBox(iconName, warnung));
        return zeile;
    }

    /**
     * Erstellt die runde Icon-Box rechts unten in der Karte.
     *
     * @param iconName Icon-Name
     * @param warnung  {@code true} für roten Hintergrund und rote Icon-Farbe
     */
    private Div buildIconBox(String iconName, boolean warnung) {
        Div box = new Div();
        box.getStyle()
                .set("width", "2.5rem").set("height", "2.5rem").set("border-radius", "9999px")
                .set("background", warnung ? "#ffdad6" : "#efecff")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");

        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle()
                .set("font-size", "1.1rem")
                .set("color", warnung ? "#ba1a1a" : "#553722");
        box.add(icon);
        return box;
    }
}
