package de.fhswf.kassensystem.views.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Wiederverwendbare Statistik-Karte mit Label, Zahlenwert und Icon.
 * Wird in ArtikelView und LagerView verwendet.
 */
public class StatistikKarte extends VerticalLayout {

    public StatistikKarte(String label, String wert, String iconName, boolean warnung) {
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("flex", "1").set("background", "white")
                .set("border-radius", "1.25rem").set("padding", "1.5rem").set("gap", "0.5rem");

        add(buildLabel(label, warnung), buildWertZeile(wert, iconName, warnung));
    }

    private Span buildLabel(String label, boolean warnung) {
        Span lbl = new Span(label);
        lbl.getStyle()
                .set("font-size", "0.65rem").set("font-weight", "800")
                .set("text-transform", "uppercase").set("letter-spacing", "0.1em")
                .set("color", warnung ? "#ba1a1a" : "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return lbl;
    }

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

    private com.vaadin.flow.component.html.Div buildIconBox(String iconName, boolean warnung) {
        com.vaadin.flow.component.html.Div box = new com.vaadin.flow.component.html.Div();
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
