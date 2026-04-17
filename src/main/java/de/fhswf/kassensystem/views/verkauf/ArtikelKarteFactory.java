package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.fhswf.kassensystem.model.Artikel;

import java.util.Base64;
import java.util.function.Consumer;

/**
 * Fabrikklasse für Artikel-Karten im Kassier-Grid der {@link VerkaufView}.
 *
 * <p>Jede Karte zeigt ein Kategorie-Icon (Tabler Icons), den Artikelnamen,
 * den Preis und einen Live-Bestandsbadge. Ausverkaufte Artikel werden
 * ausgegraut und mit einem "Ausverkauft"-Badge versehen.
 *
 * <p>Icon-Zuordnung nach Kategorienamen (Tabler Icons):
 * Brot/Brötchen → ti-bread, Kuchen → ti-cake, Teilchen → ti-cookie,
 * Sandwiches → ti-baguette, Heiße Getränke → ti-coffee, Kalte Getränke → ti-bottle.
 *
 * @author Adrian
 */
public class ArtikelKarteFactory {

    private ArtikelKarteFactory() {}

    /**
     * Erstellt eine Artikelkarte mit angegebenem Anzeigebestand.
     *
     * @param artikel          der darzustellende Artikel
     * @param aktuellerBestand der anzuzeigende Bestand (ggf. schon um Warenkorbmenge reduziert)
     * @param ausverkauft      {@code true} wenn der Artikel nicht mehr bestellbar ist
     * @param onKlick          wird beim Klick auf die Karte aufgerufen
     * @return fertige Artikelkarte
     */
    static Div create(Artikel artikel, int aktuellerBestand,
                      boolean ausverkauft, Consumer<Artikel> onKlick) {

        Div karte = new Div();
        karte.getStyle()
                .set("background",    ausverkauft ? "rgba(239,236,255,0.5)" : "white")
                .set("border-radius", "1.25rem").set("padding", "1.25rem")
                .set("display",       "flex").set("flex-direction", "column")
                .set("cursor",        ausverkauft ? "not-allowed" : "pointer")
                .set("transition",    "all 0.2s")
                .set("opacity",       ausverkauft ? "0.6" : "1")
                .set("filter",        ausverkauft ? "grayscale(1)" : "none")
                .set("box-shadow",    "0 2px 8px rgba(0,0,0,0.04)");

        Span bestandBadge = buildBestandBadge(aktuellerBestand);
        bestandBadge.setId("bestand-badge-" + artikel.getId());

        karte.add(
                buildIconWrapper(artikel, ausverkauft),
                buildName(artikel),
                buildPreisZeile(artikel, ausverkauft, bestandBadge)
        );

        if (!ausverkauft) {
            karte.addClickListener(e -> onKlick.accept(artikel));
            karte.getElement().executeJs(
                    "this.addEventListener('mouseenter', () => {" +
                            "  this.style.transform = 'translateY(-2px)';" +
                            "  this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.10)';" +
                            "});" +
                            "this.addEventListener('mouseleave', () => {" +
                            "  this.style.transform = 'none';" +
                            "  this.style.boxShadow = '0 2px 8px rgba(0,0,0,0.04)';" +
                            "});"
            );
        }
        return karte;
    }

    /**
     * Aktualisiert den Bestandsbadge einer Karte per JavaScript – ohne die Karte neu zu bauen.
     *
     * @param kartenContainer der Container der Karte (aus {@code kartenMap})
     * @param artikelId       ID des Artikels (für den Badge-Selektor)
     * @param neuerBestand    der neu anzuzeigende Bestand
     */
    static void aktualisiereBestand(Div kartenContainer, long artikelId, int neuerBestand) {
        kartenContainer.getElement().executeJs(
                "const span = this.querySelector('#bestand-badge-" + artikelId + "');" +
                        "if (span) span.textContent = '" +
                        (neuerBestand >= 999 ? "∞" : "Bestand: " + neuerBestand) + "';"
        );
    }

    // ── Private Hilfsmethoden ───────────────────────────────────────────────

    /**
     * Erstellt den Icon-Bereich der Karte – entweder mit Artikelbild oder Kategorie-Icon.
     * Im ausverkauften Zustand wird zusätzlich ein "Ausverkauft"-Badge eingeblendet.
     */
    private static Div buildIconWrapper(Artikel artikel, boolean ausverkauft) {
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("width", "100%").set("height", "7rem").set("background", "#efecff")
                .set("border-radius", "0.75rem").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("margin-bottom", "1rem").set("position", "relative").set("overflow", "hidden");

        if (artikel.getBild() != null && artikel.getBild().length > 0) {
            String base64 = Base64.getEncoder().encodeToString(artikel.getBild());
            com.vaadin.flow.component.html.Image img =
                    new com.vaadin.flow.component.html.Image(
                            "data:image/jpeg;base64," + base64, artikel.getName());
            img.getStyle().set("width", "100%").set("height", "100%")
                    .set("object-fit", "cover").set("border-radius", "0.75rem");
            wrapper.add(img);
        } else {
            wrapper.add(buildTablerIcon(iconFuerKategorie(artikel.getKategorie().getName())));
        }

        if (ausverkauft) wrapper.add(buildAusverkauftBadge());
        return wrapper;
    }

    /**
     * Erstellt einen Tabler-Icon-Span für den Kategorie-Platzhalter.
     *
     * @param iconName Tabler-Icon-Klasse (z.B. "ti-bread")
     */
    private static Span buildTablerIcon(String iconName) {
        Span icon = new Span();
        icon.addClassName("ti");
        icon.addClassName(iconName);
        icon.getStyle()
                .set("font-size", "3rem").set("color", "#553722").set("line-height", "1")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        return icon;
    }

    /**
     * Erstellt den "Ausverkauft"-Badge der oben links über das Bild gelegt wird.
     */
    private static Div buildAusverkauftBadge() {
        Span text = new Span("Ausverkauft");
        text.getStyle()
                .set("font-size", "0.625rem").set("font-weight", "700")
                .set("text-transform", "uppercase").set("color", "#1a1a2e");
        Div badge = new Div(text);
        badge.getStyle()
                .set("position", "absolute").set("top", "0.5rem").set("left", "0.5rem")
                .set("background", "white").set("border-radius", "9999px")
                .set("padding", "0.25rem 0.75rem");
        return badge;
    }

    /**
     * Erstellt den Artikelnamen als H3-Überschrift.
     */
    private static H3 buildName(Artikel artikel) {
        H3 name = new H3(artikel.getName());
        name.getStyle()
                .set("margin", "0 0 0.5rem 0").set("font-size", "1rem").set("font-weight", "700")
                .set("color", "#1a1a2e").set("font-family", "'Plus Jakarta Sans', sans-serif");
        return name;
    }

    /**
     * Erstellt den Bestandsbadge.
     *
     * @param bestand aktueller Bestand (999 oder höher = unendlich)
     */
    private static Span buildBestandBadge(int bestand) {
        String text = bestand >= 999 ? "∞" : "Bestand: " + bestand;
        Span badge = new Span(text);
        badge.getStyle()
                .set("font-size", "0.7rem").set("font-weight", "500").set("color", "#82746d")
                .set("background", "#efecff").set("padding", "0.2rem 0.6rem")
                .set("border-radius", "0.5rem").set("font-family", "'Plus Jakarta Sans', sans-serif");
        return badge;
    }

    /**
     * Erstellt die untere Preiszeile mit Preis links und Bestandsbadge rechts.
     *
     * @param artikel      Artikel dessen Preis angezeigt wird
     * @param ausverkauft  {@code true} für grau-dargestellten Preis
     * @param bestandBadge der Live-Bestandsbadge
     */
    private static HorizontalLayout buildPreisZeile(Artikel artikel,
                                                    boolean ausverkauft,
                                                    Span bestandBadge) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setPadding(false);
        row.setSpacing(false);
        row.getStyle().set("margin-top", "auto");

        String preisFormatiert = String.format("%,.2f€", artikel.getPreis())
                .replace(",", "X").replace(".", ",").replace("X", ".");
        Span preis = new Span(preisFormatiert);
        preis.getStyle()
                .set("font-size", "1.25rem").set("font-weight", "800")
                .set("color", ausverkauft ? "#82746d" : "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        row.add(preis, bestandBadge);
        return row;
    }

    /**
     * Ordnet Kategorienamen den Tabler-Icon-Klassen zu.
     *
     * @param kategorie Kategorienname des Artikels (case-insensitiv)
     * @return Tabler-Icon-Klasse (z.B. "ti-bread"), Fallback "ti-tag"
     */
    public static String iconFuerKategorie(String kategorie) {
        if (kategorie == null) return "ti-tag";
        return switch (kategorie.toLowerCase().trim()) {
            case "brot und brötchen"                         -> "ti-bread";
            case "kuchen"                                    -> "ti-cake";
            case "teilchen", "gebäck"                        -> "ti-cookie";
            case "sandwiches und belegte brötchen", "snacks" -> "ti-baguette";
            case "heiße getränke", "heißgetränke",
                 "heiße getraenke"                           -> "ti-coffee";
            case "kalte getränke", "kaltgetränke",
                 "kalte getraenke"                           -> "ti-bottle";
            default                                          -> "ti-tag";
        };
    }
}