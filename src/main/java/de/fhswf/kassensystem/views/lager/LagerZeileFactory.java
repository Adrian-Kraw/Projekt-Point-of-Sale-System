package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.fhswf.kassensystem.model.Artikel;

/**
 * Fabrikklasse für einzelne Zeilen der Bestandstabelle in der Lagerverwaltung.
 *
 * <p>Jede Zeile enthält: Artikelname (rot bei kritischem Bestand), Kategorie,
 * Bestand, Minimalgrenze und einen farbigen Ampelpunkt (grün/orange/rot).
 * Zeilen-Hover-Effekt wird per JavaScript gesetzt.
 *
 * @author Adrian Krawietz
 */
class LagerZeileFactory {

    static final String BREITE_ARTIKEL   = "35%";
    static final String BREITE_KATEGORIE = "25%";
    static final String BREITE_BESTAND   = "15%";
    static final String BREITE_MINIMAL   = "15%";
    static final String BREITE_STATUS    = "10%";

    private LagerZeileFactory() {}

    /**
     * Erstellt eine vollständig gestylte Bestandszeile für den übergebenen Artikel.
     *
     * @param artikel der darzustellende Artikel
     * @param zebra   {@code true} für abwechselnden Zeilenhintergrund
     * @return fertiges Zeilen-Layout
     */
    static HorizontalLayout create(Artikel artikel, boolean zebra) {
        int bestand = artikel.getBestand();
        int minimal = artikel.getMinimalbestand();
        String status = bestand < minimal ? "kritisch"
                : bestand < (int) Math.ceil(minimal * 1.25) ? "warn"
                : "ok";

        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("background", zebra ? "rgba(245,242,255,0.3)" : "white")
                .set("padding", "0").set("gap", "0").set("transition", "background 0.15s");

        boolean kritisch = "kritisch".equals(status);
        String normalBg  = zebra ? "rgba(245,242,255,0.3)" : "white";

        Span artikelZelle = buildZelle(artikel.getName(), BREITE_ARTIKEL, kritisch ? "#ba1a1a" : "#1a1a2e", true);
        artikelZelle.getStyle().set("padding-left", "2rem").set("padding-top", "1rem").set("padding-bottom", "1rem");

        Span kategorieZelle = buildZelle(artikel.getKategorie().getName(), BREITE_KATEGORIE, "#50453e", false);
        kategorieZelle.getStyle().set("padding-top", "1rem").set("padding-bottom", "1rem");

        Span bestandZelle = buildZelle(bestand + " Stk.", BREITE_BESTAND, kritisch ? "#ba1a1a" : "#50453e", kritisch);
        bestandZelle.getStyle().set("padding-top", "1rem").set("padding-bottom", "1rem");

        Span minimalZelle = buildZelle(minimal + " Stk.", BREITE_MINIMAL, "#82746d", false);
        minimalZelle.getStyle().set("padding-top", "1rem").set("padding-bottom", "1rem");

        Div statusZelle = buildStatusZelle(status);
        statusZelle.getStyle().set("padding-right", "2rem").set("padding-top", "1rem").set("padding-bottom", "1rem").set("justify-content", "center");

        zeile.add(artikelZelle, kategorieZelle, bestandZelle, minimalZelle, statusZelle);

        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.background = '#f0eeff';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.background = '" + normalBg + "';" +
                        "});"
        );
        return zeile;
    }

    /**
     * Erstellt eine einfache Text-Zelle mit definierter Breite und Farbe.
     */
    private static Span buildZelle(String text, String breite, String color, boolean bold) {
        Span span = new Span(text);
        span.getStyle()
                .set("flex", "0 0 " + breite).set("min-width", "0")
                .set("font-size", "0.875rem").set("color", color)
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        if (bold) span.getStyle().set("font-weight", "700");
        return span;
    }

    /**
     * Erstellt die farbige Ampelpunkt-Zelle.
     *
     * @param status "ok", "warn" oder "kritisch"
     */
    private static Div buildStatusZelle(String status) {
        String farbe = switch (status) {
            case "ok"       -> "#22c55e";
            case "warn"     -> "#fb923c";
            case "kritisch" -> "#dc2626";
            default         -> "#82746d";
        };
        Div punkt = new Div();
        punkt.getStyle()
                .set("width", "0.75rem").set("height", "0.75rem").set("border-radius", "9999px")
                .set("background", farbe).set("box-shadow", "0 0 6px " + farbe);

        Div zelle = new Div(punkt);
        zelle.getStyle()
                .set("flex", "0 0 " + BREITE_STATUS).set("min-width", "0")
                .set("display", "flex")
                .set("align-items", "center").set("justify-content", "flex-start");
        return zelle;
    }

}