package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.fhswf.kassensystem.model.Artikel;

/**
 * Baut eine einzelne Zeile der Bestandstabelle.
 */
class LagerZeileFactory {

    static final String BREITE_ARTIKEL   = "30%";
    static final String BREITE_KATEGORIE = "20%";
    static final String BREITE_BESTAND   = "15%";
    static final String BREITE_MINIMAL   = "15%";
    static final String BREITE_STATUS    = "10%";
    static final String BREITE_AKTION    = "10%";

    private LagerZeileFactory() {}

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
                .set("padding", "1rem 2rem").set("gap", "0").set("transition", "background 0.15s");

        boolean kritisch = "kritisch".equals(status);
        String normalBg  = zebra ? "rgba(245,242,255,0.3)" : "white";

        zeile.add(
                buildZelle(artikel.getName(),                BREITE_ARTIKEL,   kritisch ? "#ba1a1a" : "#1a1a2e", true),
                buildZelle(artikel.getKategorie().getName(), BREITE_KATEGORIE, "#50453e", false),
                buildZelle(bestand + " Stk.",               BREITE_BESTAND,   kritisch ? "#ba1a1a" : "#50453e", kritisch),
                buildZelle(minimal + " Stk.",               BREITE_MINIMAL,   "#82746d", false),
                buildStatusZelle(status),
                buildAktionZelle()
        );

        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.background = '#f0eeff';" +
                        "  const btn = this.querySelector('.zeilen-aktion');" +
                        "  if (btn) btn.style.opacity = '1';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.background = '" + normalBg + "';" +
                        "  const btn = this.querySelector('.zeilen-aktion');" +
                        "  if (btn) btn.style.opacity = '0';" +
                        "});"
        );
        return zeile;
    }

    private static Span buildZelle(String text, String breite, String color, boolean bold) {
        Span span = new Span(text);
        span.getStyle()
                .set("width", breite).set("font-size", "0.875rem").set("color", color)
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        if (bold) span.getStyle().set("font-weight", "700");
        return span;
    }

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
                .set("width", BREITE_STATUS).set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");
        return zelle;
    }

    private static Div buildAktionZelle() {
        Span icon = new Span("more_vert");
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.addClassName("zeilen-aktion");
        btn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("padding", "0.25rem").set("border-radius", "9999px").set("color", "#82746d")
                .set("min-width", "unset").set("opacity", "0").set("transition", "opacity 0.15s");

        Div zelle = new Div(btn);
        zelle.getStyle().set("width", BREITE_AKTION).set("display", "flex").set("justify-content", "flex-end");
        return zelle;
    }
}