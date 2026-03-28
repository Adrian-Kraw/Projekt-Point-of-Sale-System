package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

/**
 * VerkaufView ist die Kassier-Ansicht (Point of Sale).
 *
 * Aufbau: Zwei-Spalten-Layout
 * - Linke Spalte (flex:55): Artikelauswahl mit Kategorie-Filter und Artikel-Grid
 * - Rechte Spalte (flex:45): Warenkorb mit Positionen, MwSt, Gesamt, Bezahlen
 *
 * Im Prototyp sind alle Daten statisch (Dummy-Daten).
 */
@Route(value = "kassieren", layout = MainLayout.class)
public class VerkaufView extends HorizontalLayout {

    public VerkaufView() {
        /*
         * setSizeFull() sorgt dafür dass die View den gesamten
         * Content-Bereich des MainLayout ausfüllt.
         * overflow: hidden verhindert dass Inhalte über den Rand ragen.
         */
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("overflow", "hidden")
                  .set("height", "100vh");

        add(
                buildArtikelSpalte(),
                buildWarenkorbSpalte()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // LINKE SPALTE – ARTIKELAUSWAHL
    // ═══════════════════════════════════════════════════════════

    /**
     * Linke Spalte nimmt 55% des verfügbaren Platzes ein (flex: 0 0 55%).
     * overflow-y: auto erlaubt Scrollen wenn viele Artikel vorhanden sind.
     */
    private VerticalLayout buildArtikelSpalte() {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
        spalte.getStyle()
                .set("flex", "0 0 60%")
                .set("max-width", "55%")
                .set("background", "#fcf8ff")
                .set("overflow-y", "auto")
                .set("height", "100%")
                .set("padding", "2rem")
                .set("box-sizing", "border-box");

        Div grid = buildArtikelGrid();

        spalte.add(
                buildSuchfeld(),
                buildKategorieFilter(),
                grid
        );

        /*
         * ResizeObserver beobachtet die Breite der linken Spalte.
         * Sobald eine Artikelkarte (ca. 200px) zu wenig Platz hat
         * für 3 Spalten (also < 600px) wird auf 2 Spalten gewechselt.
         *
         * Die 600px entsprechen grob: 3 Karten × ~180px + Gap.
         * Das ist genau der Punkt wo die rechte Spalte ~10% der Karte überdeckt.
         */
        spalte.getElement().executeJs(
                "const observer = new ResizeObserver(entries => {" +
                        "  for (const entry of entries) {" +
                        "    const w = entry.contentRect.width;" +
                        "    const grid = this.querySelector('.artikel-grid');" +
                        "    if (!grid) return;" +
                        "    if (w < 600) {" +
                        "      grid.style.gridTemplateColumns = 'repeat(2, 1fr)';" +
                        "    } else {" +
                        "      grid.style.gridTemplateColumns = 'repeat(3, 1fr)';" +
                        "    }" +
                        "  }" +
                        "});" +
                        "observer.observe(this);"
        );

        return spalte;
    }

    /**
     * Suchfeld mit abgerundeten Ecken.
     * Das Styling der inneren Vaadin-Komponente erfolgt über styles.css
     * via ::part(input-field) Selektor.
     */
    private HorizontalLayout buildSuchfeld() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("margin-bottom", "1.5rem");

        TextField search = new TextField();
        search.setWidthFull();
        search.setPlaceholder("Artikel suchen...");
        search.setPrefixComponent(createIcon("search"));

        row.add(search);
        return row;
    }

    /**
     * Kategorie-Filter-Chips – "Alle" ist aktiv markiert.
     */
    private HorizontalLayout buildKategorieFilter() {
        HorizontalLayout filter = new HorizontalLayout();
        filter.setSpacing(false);
        filter.getStyle()
                .set("gap", "0.75rem")
                .set("margin-bottom", "2rem")
                .set("flex-wrap", "wrap");

        filter.add(
                buildKategorieChip("Alle", true),
                buildKategorieChip("Getränke", false),
                buildKategorieChip("Snacks", false),
                buildKategorieChip("Sonstiges", false)
        );
        return filter;
    }

    /**
     * @param label  Anzeigename der Kategorie
     * @param aktiv  ob dieser Chip gerade ausgewählt ist
     */
    private Button buildKategorieChip(String label, boolean aktiv) {
        Button chip = new Button(label);

        if (aktiv) {
            chip.addClassName("kategorie-chip-aktiv");
        } else {
            chip.addClassName("kategorie-chip");
        }

        chip.getStyle()
                .set("border-radius", "9999px")
                .set("padding", "0.625rem 1.5rem")
                .set("font-size", "0.875rem")
                .set("font-weight", aktiv ? "700" : "500")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("background", aktiv ? "#553722" : "#e8e5ff")
                .set("color", aktiv ? "white" : "#50453e")
                .set("box-shadow", aktiv ? "0 4px 15px rgba(85,55,34,0.2)" : "none");

        return chip;
    }

    /**
     * Artikel-Grid: 3 Spalten mit Dummy-Artikeln.
     */
    private Div buildArtikelGrid() {
        Div grid = new Div();
        grid.addClassName("artikel-grid");
        grid.getStyle()
                .set("width", "100%")
                 // Startwert: 3 Spalten. Der ResizeObserver überschreibt das dynamisch.
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "1.25rem");

        grid.add(buildArtikelKarte("local_cafe",          "Espresso",        "2,50€", 42,  false));
        grid.add(buildArtikelKarte("emoji_food_beverage", "Cappuccino",      "3,20€", 28,  false));
        grid.add(buildArtikelKarte("coffee",              "Latte Macchiato", "3,80€", 15,  false));
        grid.add(buildArtikelKarte("water_drop",          "Wasser",          "1,50€", 124, false));
        grid.add(buildArtikelKarte("bakery_dining",       "Croissant",       "2,00€", 8,   false));
        grid.add(buildArtikelKarte("cake",                "Muffin",          "2,50€", 0,   true));

        return grid;
    }

    /**
     * Einzelne Artikel-Karte.
     *
     * @param iconName    Material Symbols Icon-Name
     * @param name        Artikelname
     * @param preis       Preis als formatierter String
     * @param bestand     aktueller Lagerbestand
     * @param ausverkauft ob der Artikel nicht verfügbar ist
     */
    private Div buildArtikelKarte(String iconName, String name,
                                  String preis, int bestand,
                                  boolean ausverkauft) {
        Div karte = new Div();
        karte.getStyle()
                .set("background", ausverkauft ? "rgba(239,236,255,0.5)" : "white")
                .set("border-radius", "1.25rem")
                .set("padding", "1.25rem")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("cursor", ausverkauft ? "not-allowed" : "pointer")
                .set("transition", "all 0.2s")
                .set("opacity", ausverkauft ? "0.6" : "1")
                .set("filter", ausverkauft ? "grayscale(1)" : "none")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.04)");

        // Icon-Bereich
        Div iconWrapper = new Div();
        iconWrapper.getStyle()
                .set("width", "100%")
                .set("height", "7rem")
                .set("background", "#efecff")
                /*
                 * border-radius: 0.75rem – leicht abgerundet, nicht pill-form.
                 * Passend zum Stitch-Design für den Bild-/Icon-Bereich.
                 */
                .set("border-radius", "0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "1rem")
                .set("position", "relative");

        Span icon = createIcon(iconName);
        icon.getStyle()
                .set("font-size", "3rem")
                .set("color", "#553722");

        iconWrapper.add(icon);

        // Ausverkauft-Badge
        if (ausverkauft) {
            Div badge = new Div();
            Span badgeText = new Span("Ausverkauft");
            badgeText.getStyle()
                    .set("font-size", "0.625rem")
                    .set("font-weight", "700")
                    .set("text-transform", "uppercase")
                    .set("letter-spacing", "0.05em")
                    .set("color", "#1a1a2e");
            badge.add(badgeText);
            badge.getStyle()
                    .set("position", "absolute")
                    .set("top", "0.5rem")
                    .set("left", "0.5rem")
                    .set("background", "white")
                    .set("border-radius", "9999px")
                    .set("padding", "0.25rem 0.75rem");
            iconWrapper.add(badge);
        }

        H3 artikelName = new H3(name);
        artikelName.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("font-size", "1rem")
                .set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout preisBestand = new HorizontalLayout();
        preisBestand.setWidthFull();
        preisBestand.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        preisBestand.setAlignItems(FlexComponent.Alignment.CENTER);
        preisBestand.setPadding(false);
        preisBestand.setSpacing(false);
        preisBestand.getStyle().set("margin-top", "auto");

        Span preisSpan = new Span(preis);
        preisSpan.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "800")
                .set("color", ausverkauft ? "#82746d" : "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span bestandSpan = new Span("Bestand: " + bestand);
        bestandSpan.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "500")
                .set("color", "#82746d")
                .set("background", "#efecff")
                .set("padding", "0.2rem 0.6rem")
                .set("border-radius", "0.5rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        preisBestand.add(preisSpan, bestandSpan);
        karte.add(iconWrapper, artikelName, preisBestand);
        return karte;
    }

    // ═══════════════════════════════════════════════════════════
    // RECHTE SPALTE – WARENKORB
    // ═══════════════════════════════════════════════════════════

    /**
     * Rechte Spalte nimmt 45% des verfügbaren Platzes ein.
     * Sie ist als VerticalLayout aufgebaut mit:
     * - Header (fixed)
     * - Positionen (scrollbar, flex:1)
     * - Zusammenfassung (fixed am unteren Rand)
     */
    private VerticalLayout buildWarenkorbSpalte() {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
        spalte.getStyle()
                .set("flex", "1")
                .set("background", "#f5f2ff")
                .set("min-height", "100vh")
                .set("height", "100%")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("overflow", "hidden");

        spalte.add(
                buildWarenkorbHeader(),
                buildWarenkorbPositionen(),
                buildBestellZusammenfassung()
        );
        return spalte;
    }

    /**
     * Warenkorb-Header mit Titel und Löschen-Button.
     */
    private HorizontalLayout buildWarenkorbHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(false);
        header.getStyle().set("padding", "2rem 2rem 1rem 2rem");

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "0.75rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "rgba(85,55,34,0.1)")
                .set("border-radius", "0.75rem")
                .set("padding", "0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span receiptIcon = createIcon("receipt");
        receiptIcon.getStyle().set("color", "#553722");
        iconBox.add(receiptIcon);

        H2 titel = new H2("Warenkorb");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);

        Button loeschenBtn = new Button();
        loeschenBtn.getElement().appendChild(createIcon("delete_sweep").getElement());
        loeschenBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("color", "#82746d")
                .set("padding", "0.5rem")
                .set("border-radius", "9999px")
                .set("min-width", "unset");

        header.add(titelGruppe, loeschenBtn);
        return header;
    }

    /**
     * Warenkorb-Positionen mit Zebra-Muster und Mengenkontrolle.
     * flex:1 sorgt dafür dass dieser Bereich den verfügbaren Platz ausfüllt.
     */
    private VerticalLayout buildWarenkorbPositionen() {
        VerticalLayout liste = new VerticalLayout();
        liste.setPadding(false);
        liste.setSpacing(false);
        liste.getStyle()
                .set("padding", "0 1.5rem")
                .set("gap", "0.25rem")
                .set("flex", "1")
                .set("overflow-y", "auto");

        liste.add(
                buildWarenkorbPosition("Cappuccino",  "3,20€", 2, "6,40€", false),
                buildWarenkorbPosition("Croissant",   "2,00€", 1, "2,00€", true),
                buildWarenkorbPosition("Espresso",    "2,50€", 1, "2,50€", false)
        );
        return liste;
    }

    /**
     * Einzelne Warenkorb-Position mit Mengenkontrolle.
     *
     * @param name        Artikelname
     * @param einzelPreis Preis pro Stück
     * @param menge       aktuelle Menge
     * @param gesamt      berechneter Gesamtpreis dieser Position
     * @param zebra       ob diese Zeile den alternativen Hintergrund bekommt
     */
    private HorizontalLayout buildWarenkorbPosition(String name, String einzelPreis,
                                                    int menge, String gesamt,
                                                    boolean zebra) {
        HorizontalLayout position = new HorizontalLayout();
        position.setWidthFull();
        position.setAlignItems(FlexComponent.Alignment.CENTER);
        position.setSpacing(false);
        position.getStyle()
                .set("background", zebra ? "#f5f2ff" : "white")
                .set("border-radius", "1rem")
                .set("padding", "1rem")
                .set("gap", "0.75rem");

        // Name + Einzelpreis
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("flex", "1");

        H4 artikelName = new H4(name);
        artikelName.getStyle()
                .set("margin", "0")
                .set("font-size", "0.9rem")
                .set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph einzelPreisLabel = new Paragraph("Einzel: " + einzelPreis);
        einzelPreisLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "0.7rem")
                .set("color", "#d4c3ba")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(artikelName, einzelPreisLabel);

        // Mengenkontrolle
        HorizontalLayout mengeKontrolle = new HorizontalLayout();
        mengeKontrolle.setAlignItems(FlexComponent.Alignment.CENTER);
        mengeKontrolle.setSpacing(false);
        mengeKontrolle.getStyle()
                .set("background", "#efecff")
                .set("border-radius", "9999px")
                .set("padding", "0.25rem")
                .set("gap", "0.25rem");

        Button minusBtn = buildMengeButton("remove");
        Span mengeSpan = new Span(String.valueOf(menge));
        mengeSpan.getStyle()
                .set("width", "2rem")
                .set("text-align", "center")
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        Button plusBtn = buildMengeButton("add");

        mengeKontrolle.add(minusBtn, mengeSpan, plusBtn);

        // Gesamtpreis
        Span gesamtSpan = new Span(gesamt);
        gesamtSpan.getStyle()
                .set("width", "4rem")
                .set("text-align", "right")
                .set("font-weight", "700")
                .set("font-size", "0.9rem")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Löschen
        Button deleteBtn = new Button();
        deleteBtn.getElement().appendChild(createIcon("delete").getElement());
        deleteBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("color", "#d4c3ba")
                .set("padding", "0.25rem")
                .set("min-width", "unset");

        position.add(info, mengeKontrolle, gesamtSpan, deleteBtn);
        return position;
    }

    /**
     * Runder +/- Button für die Mengenkontrolle.
     *
     * @param iconName "add" oder "remove"
     */
    private Button buildMengeButton(String iconName) {
        Button btn = new Button();
        Span icon = createIcon(iconName);
        icon.getStyle()
                .set("font-size", "1rem")
                .set("line-height", "1")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("width", "2rem")
                .set("height", "2rem")
                .set("min-width", "2rem")
                .set("border-radius", "9999px")
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "#553722")
                .set("padding", "0");
        return btn;
    }

    /**
     * Unterer Bereich der rechten Spalte:
     * Rabattfeld, MwSt-Aufschlüsselung, Gesamtsumme, Aktionsbuttons.
     *
     * border-radius oben (3rem 3rem 0 0) gibt dem Bereich die
     * charakteristische abgerundete Oberkante aus dem Stitch-Design.
     */
    private VerticalLayout buildBestellZusammenfassung() {
        VerticalLayout zusammenfassung = new VerticalLayout();
        zusammenfassung.setWidthFull();
        zusammenfassung.setPadding(false);
        zusammenfassung.setSpacing(false);
        zusammenfassung.getStyle()
                .set("background", "#e8e5ff")
                .set("border-radius", "3rem 3rem 0 0")
                .set("padding", "1.5rem")
                .set("gap", "0")
                .set("flex-shrink", "0");

        zusammenfassung.add(
                buildRabattZeile(),
                buildPreisZeilen(),
                buildAktionsButtons()
        );
        return zusammenfassung;
    }

    /**
     * Rabatt-Eingabefeld mit Anwenden-Button.
     */
    private HorizontalLayout buildRabattZeile() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(false);
        row.getStyle()
                .set("gap", "0.75rem")
                .set("margin-bottom", "1.25rem");

        TextField rabattFeld = new TextField();
        rabattFeld.setPlaceholder("Rabatt in %");
        rabattFeld.addClassName("rabatt-feld");
        rabattFeld.getStyle().set("flex", "1");

        Button anwendenBtn = new Button("Anwenden");
        anwendenBtn.getStyle()
                .set("background", "#e2e0fc")
                .set("color", "#553722")
                .set("font-weight", "700")
                .set("border", "none")
                .set("border-radius", "0.75rem")
                .set("padding", "0.75rem 1.25rem")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("white-space", "nowrap")
                .set("flex-shrink", "0");

        row.add(rabattFeld, anwendenBtn);
        return row;
    }

    /**
     * Preiszeilen: Zwischensumme, MwSt 7%, MwSt 19%, Gesamtsumme.
     */
    private VerticalLayout buildPreisZeilen() {
        VerticalLayout zeilen = new VerticalLayout();
        zeilen.setPadding(false);
        zeilen.setSpacing(false);
        zeilen.getStyle()
                .set("gap", "0.4rem")
                .set("margin-bottom", "1.25rem");

        zeilen.add(
                buildPreisZeile("Zwischensumme", "10,90€"),
                buildPreisZeile("MwSt 7%",       "0,40€"),
                buildPreisZeile("MwSt 19%",      "0,98€")
        );

        Div trennlinie = new Div();
        trennlinie.getStyle()
                .set("border-top", "1px solid rgba(85,55,34,0.1)")
                .set("margin", "0.75rem 0");
        zeilen.add(trennlinie);

        // Gesamtzeile
        HorizontalLayout gesamtZeile = new HorizontalLayout();
        gesamtZeile.setWidthFull();
        gesamtZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        gesamtZeile.setAlignItems(FlexComponent.Alignment.BASELINE);
        gesamtZeile.setPadding(false);

        Span gesamtLabel = new Span("Gesamt");
        gesamtLabel.getStyle()
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span gesamtBetrag = new Span("10,90€");
        gesamtBetrag.getStyle()
                .set("font-size", "2.25rem")
                .set("font-weight", "900")
                .set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        gesamtZeile.add(gesamtLabel, gesamtBetrag);
        zeilen.add(gesamtZeile);
        return zeilen;
    }

    /**
     * Einzelne Preiszeile (Label links, Betrag rechts).
     */
    private HorizontalLayout buildPreisZeile(String label, String betrag) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span betragSpan = new Span(betrag);
        betragSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        zeile.add(labelSpan, betragSpan);
        return zeile;
    }

    /**
     * Abbrechen und Bezahlen Buttons.
     */
    private HorizontalLayout buildAktionsButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setSpacing(false);
        buttons.getStyle().set("gap", "0.75rem");

        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("flex", "1")
                .set("padding", "1rem")
                .set("border", "2px solid #d4c3ba")
                .set("background", "transparent")
                .set("border-radius", "1rem")
                .set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("font-size", "0.95rem");

        Button bezahlenBtn = new Button();

        Span zahlungsIcon = createIcon("payments");
        zahlungsIcon.getStyle()
                .set("font-variation-settings", "'FILL' 1")
                .set("font-size", "1.2rem")
                .set("line-height", "1")
                .set("vertical-align", "middle");

        Span bezahlenText = new Span("Bezahlen");
        bezahlenText.getStyle()
                .set("font-weight", "900")
                .set("font-size", "1rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("line-height", "1");       // ← neu

        bezahlenBtn.getElement().appendChild(zahlungsIcon.getElement());
        bezahlenBtn.getElement().appendChild(bezahlenText.getElement());
        bezahlenBtn.getStyle()
                .set("flex", "2")
                .set("padding", "1rem 1.5rem")
                .set("background", "linear-gradient(to right, #553722, #6f4e37)")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "1rem")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("flex-direction", "row")   // ← Icon und Text nebeneinander
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("gap", "0.5rem")
                .set("box-shadow", "0 8px 25px rgba(85,55,34,0.3)");

        buttons.add(abbrechenBtn, bezahlenBtn);
        return buttons;
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt einen Material Symbols Icon-Span.
     *
     * @param iconName Name des Icons (z.B. "search", "delete", "add")
     */
    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }
}