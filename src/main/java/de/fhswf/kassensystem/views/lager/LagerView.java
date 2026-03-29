package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

/**
 * LagerView zeigt die Bestandsübersicht des Cafés.
 *
 * Aufbau:
 * - Header: Titel + Untertitel
 * - Statistik-Karten: Kritische Artikel, Gesamtartikel, Lager-Aktionen
 * - Nachbestellhinweise: Artikel unter Minimalbestand
 * - Bestandstabelle: alle Artikel mit Status-Ampel
 *
 * Im Prototyp mit Dummy-Daten – später mit LagerService verbunden.
 */
@Route(value = "lager", layout = MainLayout.class)
public class LagerView extends VerticalLayout {

    /*
     * Spaltenbreiten der Bestandstabelle – zentral definiert.
     * Summe muss 100% ergeben.
     */
    private static final String BREITE_ARTIKEL   = "30%";
    private static final String BREITE_KATEGORIE = "20%";
    private static final String BREITE_BESTAND   = "15%";
    private static final String BREITE_MINIMAL   = "15%";
    private static final String BREITE_STATUS    = "10%";
    private static final String BREITE_AKTION    = "10%";

    public LagerView() {
        //setSizeFull();
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box");
//                .set("overflow-y", "auto")
//                .set("height", "100%");

        add(
                buildHeader(),
                buildStatistikKarten(),
                buildNachbestellHinweise(),
                buildBestandsTabelle()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    /**
     * Header mit Icon, Titel und Untertitel.
     */
    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.getStyle()
                .set("gap", "1rem")
                .set("margin-bottom", "2rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "#e2e0fc")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span icon = createIcon("inventory_2");
        icon.getStyle()
                .set("color", "#553722")
                .set("font-size", "1.75rem");
        iconBox.add(icon);

        VerticalLayout titelBlock = new VerticalLayout();
        titelBlock.setPadding(false);
        titelBlock.setSpacing(false);

        H2 titel = new H2("Lagerverwaltung");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.75rem")
                .set("font-weight", "800")
                .set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph untertitel = new Paragraph("Bestandsübersicht & Logistik");
        untertitel.getStyle()
                .set("margin", "0")
                .set("font-size", "0.875rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelBlock.add(titel, untertitel);
        header.add(iconBox, titelBlock);
        return header;
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIK-KARTEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Drei Statistik-Karten: kritische Artikel, Gesamtartikel, Lager-Aktionen.
     */
    private HorizontalLayout buildStatistikKarten() {
        HorizontalLayout reihe = new HorizontalLayout();
        reihe.setWidthFull();
        reihe.setSpacing(false);
        reihe.getStyle()
                .set("gap", "1.5rem")
                .set("margin-bottom", "2rem");

        reihe.add(
                buildKritischKarte(),
                buildGesamtKarte(),
                buildAktionenKarte()
        );
        return reihe;
    }

    /**
     * Karte: Artikel unter Minimalbestand (rot hervorgehoben).
     */
    private VerticalLayout buildKritischKarte() {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1")
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "0.5rem");

        Span label = new Span("ARTIKEL UNTER MINIMALBESTAND");
        label.getStyle()
                .set("font-size", "0.65rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout wertZeile = new HorizontalLayout();
        wertZeile.setWidthFull();
        wertZeile.setAlignItems(FlexComponent.Alignment.BASELINE);
        wertZeile.setPadding(false);
        wertZeile.setSpacing(false);
        wertZeile.getStyle().set("justify-content", "space-between");

        Span wert = new Span("3");
        wert.getStyle()
                .set("font-size", "3rem")
                .set("font-weight", "900")
                .set("color", "#ba1a1a")
                .set("letter-spacing", "-0.025em")
                .set("line-height", "1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("width", "3.5rem")
                .set("height", "3.5rem")
                .set("border-radius", "1rem")
                .set("background", "#ffdad6")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span warnIcon = createIcon("warning");
        warnIcon.getStyle()
                .set("font-size", "1.75rem")
                .set("color", "#ba1a1a");
        iconBox.add(warnIcon);

        wertZeile.add(wert, iconBox);

        Paragraph hinweis = new Paragraph("Nachbestellen empfohlen");
        hinweis.getStyle()
                .set("margin", "0")
                .set("font-size", "0.8rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        karte.add(label, wertZeile, hinweis);
        return karte;
    }

    /**
     * Karte: Gesamtartikel im Lager.
     */
    private VerticalLayout buildGesamtKarte() {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1")
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "0.5rem");

        Span label = new Span("GESAMTARTIKEL");
        label.getStyle()
                .set("font-size", "0.65rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout wertZeile = new HorizontalLayout();
        wertZeile.setWidthFull();
        wertZeile.setAlignItems(FlexComponent.Alignment.BASELINE);
        wertZeile.setPadding(false);
        wertZeile.setSpacing(false);
        wertZeile.getStyle().set("justify-content", "space-between");

        Span wert = new Span("24");
        wert.getStyle()
                .set("font-size", "3rem")
                .set("font-weight", "900")
                .set("color", "#553722")
                .set("letter-spacing", "-0.025em")
                .set("line-height", "1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("width", "3.5rem")
                .set("height", "3.5rem")
                .set("border-radius", "1rem")
                .set("background", "#ffdcc6")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span inventoryIcon = createIcon("inventory");
        inventoryIcon.getStyle()
                .set("font-size", "1.75rem")
                .set("color", "#553722");
        iconBox.add(inventoryIcon);

        wertZeile.add(wert, iconBox);

        Paragraph hinweis = new Paragraph("In 5 Kategorien");
        hinweis.getStyle()
                .set("margin", "0")
                .set("font-size", "0.8rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        karte.add(label, wertZeile, hinweis);
        return karte;
    }

    /**
     * Karte: Lager-Aktionen mit Bestandseingang Button.
     */
    private VerticalLayout buildAktionenKarte() {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1")
                .set("background", "linear-gradient(135deg, #553722, #6f4e37)")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "0.75rem");

        H3 aktionenTitel = new H3("Lager-Aktionen");
        aktionenTitel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "white")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph aktionenText = new Paragraph(
                "Verwalten Sie Ihre Bestände effizient und buchen Sie Eingänge.");
        aktionenText.getStyle()
                .set("margin", "0")
                .set("font-size", "0.8rem")
                .set("color", "rgba(255,220,198,0.8)")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button eingangBtn = new Button();
        eingangBtn.setWidthFull();

        Span plusIcon = createIcon("add_circle");
        plusIcon.getStyle().set("font-size", "1.1rem");

        Span btnText = new Span("Bestandseingang buchen");
        btnText.getStyle()
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        eingangBtn.getElement().appendChild(plusIcon.getElement());
        eingangBtn.getElement().appendChild(btnText.getElement());
        eingangBtn.getStyle()
                .set("background", "#ffdcc6")
                .set("color", "#553722")
                .set("border", "none")
                .set("border-radius", "9999px")
                .set("padding", "1rem 1.5rem")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("gap", "0.5rem")
                .set("margin-top", "0.5rem");

        karte.add(aktionenTitel, aktionenText, eingangBtn);
        return karte;
    }

    // ═══════════════════════════════════════════════════════════
    // NACHBESTELLHINWEISE
    // ═══════════════════════════════════════════════════════════

    /**
     * Roter Warnblock mit Artikeln unter Minimalbestand.
     * Jede Karte zeigt Artikelname, Min/Ist-Bestand und Bestellbutton.
     */
    private VerticalLayout buildNachbestellHinweise() {
        VerticalLayout block = new VerticalLayout();
        block.setWidthFull();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle()
                .set("background", "#fff5f2")
                .set("border-radius", "1.25rem")
                .set("overflow", "hidden")
                .set("margin-bottom", "2rem");

        // Warn-Header
        HorizontalLayout warnHeader = new HorizontalLayout();
        warnHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        warnHeader.setSpacing(false);
        warnHeader.getStyle()
                .set("background", "#ffdad6")
                .set("padding", "1rem 1.5rem")
                .set("gap", "0.75rem");

        Span warnIcon = createIcon("notification_important");
        warnIcon.getStyle().set("color", "#ba1a1a");

        Span warnTitel = new Span("Nachbestellhinweise");
        warnTitel.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", "700")
                .set("color", "#ba1a1a")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        warnHeader.add(warnIcon, warnTitel);

        // Karten-Grid
        HorizontalLayout kartenGrid = new HorizontalLayout();
        kartenGrid.setWidthFull();
        kartenGrid.setSpacing(false);
        kartenGrid.getStyle()
                .set("padding", "1.5rem")
                .set("gap", "1rem")
                .set("flex-wrap", "wrap");

        kartenGrid.add(
                buildNachbestellKarte("Arabica Bohnen (1kg)", "10 Stk", "2 Stk"),
                buildNachbestellKarte("Hafermilch Barista",   "24 Stk", "4 Stk"),
                buildNachbestellKarte("Papierservietten",     "50 Pkt", "0 Stk")
        );

        block.add(warnHeader, kartenGrid);
        return block;
    }

    /**
     * Einzelne Nachbestellkarte mit Artikelname, Bestandsinfo und Bestellbutton.
     *
     * @param name      Artikelname
     * @param minBestand Minimalbestand als String
     * @param istBestand aktueller Bestand als String
     */
    private HorizontalLayout buildNachbestellKarte(String name,
                                                   String minBestand,
                                                   String istBestand) {
        HorizontalLayout karte = new HorizontalLayout();
        karte.setAlignItems(FlexComponent.Alignment.CENTER);
        karte.setSpacing(false);
        karte.getStyle()
                .set("background", "rgba(255,255,255,0.6)")
                .set("border-radius", "0.75rem")
                .set("padding", "1rem 1.25rem")
                .set("gap", "1rem")
                .set("flex", "1")
                .set("min-width", "200px");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("flex", "1");

        Span nameSpan = new Span(name);
        nameSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // ── Ist-Bestand als roter Badge + Minimalbestand-Text ──
        Span badge = new Span(istBestand);
        badge.getStyle()
                .set("background", "#ba1a1a")
                .set("color", "white")
                .set("border-radius", "9999px")
                .set("padding", "0.15rem 0.6rem")
                .set("font-size", "0.75rem")
                .set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span vonText = new Span("von " + minBestand + " (Min)");
        vonText.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout bestandRow = new HorizontalLayout();
        bestandRow.setAlignItems(FlexComponent.Alignment.CENTER);
        bestandRow.setSpacing(false);
        bestandRow.getStyle().set("gap", "0.4rem");
        bestandRow.add(badge, vonText);
        // ─────────────────────────────────────────────────────

        info.add(nameSpan, bestandRow);

        Button bestellBtn = new Button("Wareneingang buchen");
        bestellBtn.getStyle()
                .set("background", "#553722")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "0.75rem")
                .set("padding", "0.6rem 1.25rem")
                .set("font-weight", "700")
                .set("font-size", "0.8rem")
                .set("cursor", "pointer")
                .set("white-space", "nowrap")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("flex-shrink", "0");

        karte.add(info, bestellBtn);
        return karte;
    }



    // ═══════════════════════════════════════════════════════════
    // BESTANDSTABELLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Vollständige Bestandstabelle mit Suchfeld und allen Artikeln.
     */
    private VerticalLayout buildBestandsTabelle() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.getStyle()
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("overflow", "hidden");

        container.add(
                buildTabellenKopf(),
                buildTabelle()
        );
        return container;
    }

    /**
     * Tabellenkopf mit Titel und Suchfeld.
     */
    private HorizontalLayout buildTabellenKopf() {
        HorizontalLayout kopf = new HorizontalLayout();
        kopf.setWidthFull();
        kopf.setAlignItems(FlexComponent.Alignment.CENTER);
        kopf.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopf.setPadding(false);
        kopf.getStyle().set("padding", "1.5rem 2rem");

        H3 titel = new H3("Alle Bestände");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        TextField suchfeld = new TextField();
        suchfeld.setPlaceholder("Artikel oder Kategorie suchen...");
        suchfeld.setPrefixComponent(createIcon("search"));
        suchfeld.addClassName("lager-suchfeld");
        suchfeld.getStyle().set("width", "20rem");

        kopf.add(titel, suchfeld);
        return kopf;
    }

    /**
     * Tabelle mit Header und Datenzeilen.
     */
    private VerticalLayout buildTabelle() {
        VerticalLayout tabelle = new VerticalLayout();
        tabelle.setWidthFull();
        tabelle.setPadding(false);
        tabelle.setSpacing(false);
        tabelle.getStyle().set("gap", "0");

        tabelle.add(buildTabellenHeader());

        /*
         * Dummy-Daten – später durch LagerService.findAll() ersetzen.
         * Status: "ok" = grün, "warn" = orange, "kritisch" = rot
         */
        tabelle.add(buildLagerZeile("Espresso Blend",       "Kaffee",    "42 Pkt",  "15 Pkt", "ok",       false));
        tabelle.add(buildLagerZeile("Vollmilch 3.5%",       "Molkerei",  "18 Pkt",  "12 Pkt", "warn",     true));
        tabelle.add(buildLagerZeile("Croissants (TK)",      "Backwaren", "120 Stk", "50 Stk", "ok",       false));
        tabelle.add(buildLagerZeile("Papierservietten",     "Zubehör",   "0 Pkt",   "50 Pkt", "kritisch", true));
        tabelle.add(buildLagerZeile("Hafermilch Barista",   "Molkerei",  "4 Stk",   "24 Stk", "kritisch", false));
        tabelle.add(buildLagerZeile("Arabica Bohnen (1kg)", "Kaffee",    "2 Stk",   "10 Stk", "kritisch", true));

        return tabelle;
    }

    /**
     * Header-Zeile der Tabelle.
     */
    private HorizontalLayout buildTabellenHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "0.75rem 2rem")
                .set("gap", "0");

        header.add(
                buildHeaderZelle("Artikel",       BREITE_ARTIKEL),
                buildHeaderZelle("Kategorie",     BREITE_KATEGORIE),
                buildHeaderZelle("Bestand",       BREITE_BESTAND),
                buildHeaderZelle("Minimalbestand",BREITE_MINIMAL),
                buildHeaderZelle("Status",        BREITE_STATUS),
                buildHeaderZelle("",              BREITE_AKTION)
        );
        return header;
    }

    /**
     * Einzelne Spaltenüberschrift.
     */
    private Span buildHeaderZelle(String text, String breite) {
        Span zelle = new Span(text);
        zelle.getStyle()
                .set("width", breite)
                .set("font-size", "0.65rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return zelle;
    }

    /**
     * Eine Lager-Datenzeile.
     *
     * Status-Ampel:
     * - "ok"       → grüner Punkt
     * - "warn"     → oranger Punkt
     * - "kritisch" → roter blinkender Punkt
     *
     * @param artikel    Artikelname
     * @param kategorie  Kategorie
     * @param bestand    aktueller Bestand
     * @param minimal    Minimalbestand
     * @param status     "ok", "warn" oder "kritisch"
     * @param zebra      ob diese Zeile den alternativen Hintergrund bekommt
     */
    private HorizontalLayout buildLagerZeile(String artikel, String kategorie,
                                             String bestand, String minimal,
                                             String status, boolean zebra) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("background", zebra ? "rgba(245,242,255,0.3)" : "white")
                .set("padding", "1rem 2rem")
                .set("gap", "0")
                .set("transition", "background 0.15s");

        boolean kritisch = "kritisch".equals(status);

        Span artikelZelle = new Span(artikel);
        artikelZelle.getStyle()
                .set("width", BREITE_ARTIKEL)
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("color", kritisch ? "#ba1a1a" : "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span kategorieZelle = new Span(kategorie);
        kategorieZelle.getStyle()
                .set("width", BREITE_KATEGORIE)
                .set("font-size", "0.875rem")
                .set("color", "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span bestandZelle = new Span(bestand);
        bestandZelle.getStyle()
                .set("width", BREITE_BESTAND)
                .set("font-size", "0.875rem")
                .set("font-weight", kritisch ? "800" : "400")
                .set("color", kritisch ? "#ba1a1a" : "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span minimalZelle = new Span(minimal);
        minimalZelle.getStyle()
                .set("width", BREITE_MINIMAL)
                .set("font-size", "0.875rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Status-Ampel als farbiger Punkt
        Div statusPunkt = new Div();
        String farbe = switch (status) {
            case "ok"       -> "#22c55e";
            case "warn"     -> "#fb923c";
            case "kritisch" -> "#dc2626";
            default         -> "#82746d";
        };
        statusPunkt.getStyle()
                .set("width", "0.75rem")
                .set("height", "0.75rem")
                .set("border-radius", "9999px")
                .set("background", farbe)
                .set("box-shadow", "0 0 6px " + farbe);

        Div statusZelle = new Div(statusPunkt);
        statusZelle.getStyle()
                .set("width", BREITE_STATUS)
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        // Mehr-Optionen Button (beim Hover sichtbar)
        Button aktionBtn = new Button();
        aktionBtn.getElement().appendChild(createIcon("more_vert").getElement());
        aktionBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0.25rem")
                .set("border-radius", "9999px")
                .set("color", "#82746d")
                .set("min-width", "unset")
                .set("opacity", "0")
                .set("transition", "opacity 0.15s");
        aktionBtn.addClassName("zeilen-aktion");

        Div aktionZelle = new Div(aktionBtn);
        aktionZelle.getStyle()
                .set("width", BREITE_AKTION)
                .set("display", "flex")
                .set("justify-content", "flex-end");

        /*
         * Hover: Hintergrund + Aktions-Button einblenden.
         */
        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.background = '#f0eeff';" +
                        "  const btn = this.querySelector('.zeilen-aktion');" +
                        "  if (btn) btn.style.opacity = '1';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.background = '" + (zebra ? "rgba(245,242,255,0.3)" : "white") + "';" +
                        "  const btn = this.querySelector('.zeilen-aktion');" +
                        "  if (btn) btn.style.opacity = '0';" +
                        "});"
        );

        zeile.add(artikelZelle, kategorieZelle, bestandZelle,
                minimalZelle, statusZelle, aktionZelle);
        return zeile;
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt einen Material Symbols Icon-Span.
     *
     * @param iconName Name des Icons
     */
    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }
}