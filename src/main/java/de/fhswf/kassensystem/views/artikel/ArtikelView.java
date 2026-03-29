package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

/**
 * ArtikelView zeigt alle Artikel des Cafés in einer Tabelle.
 *
 * Aufbau:
 * - Header: Titel + Suchfeld + "Neuer Artikel" Button
 * - Tabelle: ID, Name, Kategorie, Preis, MwSt, Bestand, Minimalgrenze, Status, Aktionen
 * - Statistik-Karten: Gesamtartikel, Aktiv, Niedriger Bestand, Kategorien
 *
 * Im Prototyp mit Dummy-Daten – später mit ArtikelService verbunden.
 */
@Route(value = "artikel", layout = MainLayout.class)
public class ArtikelView extends VerticalLayout {

    /*
     * Spaltenbreiten zentral definiert – Änderung hier wirkt
     * automatisch auf Header UND alle Datenzeilen.
     * Summe muss 100% ergeben.
     */
    private static final String BREITE_ID        = "7%";
    private static final String BREITE_NAME      = "18%";
    private static final String BREITE_KATEGORIE = "14%";
    private static final String BREITE_PREIS     = "9%";
    private static final String BREITE_MWST      = "6%";
    private static final String BREITE_BESTAND   = "12%";
    private static final String BREITE_MINIMAL   = "11%";
    private static final String BREITE_STATUS    = "10%";
    private static final String BREITE_AKTIONEN  = "13%";

    public ArtikelView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box")
                .set("overflow-y", "auto");

        add(
                buildHeader(),
                buildTabellenBereich(),
                buildStatistikKarten()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    /**
     * Header mit Titel links und Suchfeld + Button rechts.
     */
    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(false);
        header.getStyle().set("margin-bottom", "2rem");

        header.add(buildTitel(), buildHeaderAktionen());
        return header;
    }

    /**
     * Titel-Gruppe: Icon-Box + Überschrift "Artikelverwaltung".
     */
    private HorizontalLayout buildTitel() {
        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "1rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "#e2e0fc")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span icon = createIcon("label");
        icon.getStyle()
                .set("color", "#553722")
                .set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Artikelverwaltung");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.75rem")
                .set("font-weight", "800")
                .set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);
        return titelGruppe;
    }

    /**
     * Rechter Header-Bereich: Suchfeld + "Neuer Artikel" Button.
     */
    private HorizontalLayout buildHeaderAktionen() {
        HorizontalLayout aktionen = new HorizontalLayout();
        aktionen.setAlignItems(FlexComponent.Alignment.CENTER);
        aktionen.setSpacing(false);
        aktionen.getStyle().set("gap", "1rem");

        TextField suchfeld = new TextField();
        suchfeld.setPlaceholder("Artikel suchen...");
        suchfeld.setPrefixComponent(createIcon("search"));
        suchfeld.addClassName("artikel-suchfeld");
        suchfeld.getStyle().set("width", "18rem");

        aktionen.add(suchfeld, buildNeuerArtikelButton());
        return aktionen;
    }

    /**
     * "Neuer Artikel" Button mit Gradient und Plus-Icon.
     * TODO: Öffnet später einen Dialog zum Erstellen eines neuen Artikels.
     */
    private Button buildNeuerArtikelButton() {
        Span plusIcon = createIcon("add");
        plusIcon.getStyle().set("font-size", "1.1rem");

        Span btnText = new Span("Neuer Artikel");
        btnText.getStyle()
                .set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button btn = new Button();
        btn.getElement().appendChild(plusIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("background", "linear-gradient(135deg, #553722, #6f4e37)")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 1.5rem")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("gap", "0.5rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("white-space", "nowrap");

        btn.addClickListener(event -> new NeuerArtikelDialog().open());

        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // TABELLEN-BEREICH
    // ═══════════════════════════════════════════════════════════

    /**
     * Äußerer Container für Tabelle und Pagination.
     */
    private VerticalLayout buildTabellenBereich() {
        VerticalLayout bereich = new VerticalLayout();
        bereich.setWidthFull();
        bereich.setPadding(false);
        bereich.setSpacing(false);
        bereich.getStyle()
                .set("background", "#f5f2ff")
                .set("border-radius", "1.25rem")
                .set("padding", "1.5rem")
                .set("gap", "0");

        bereich.add(buildTabelle(), buildPagination());
        return bereich;
    }

    /**
     * Tabelle mit Header und allen Datenzeilen.
     */
    private VerticalLayout buildTabelle() {
        VerticalLayout tabelle = new VerticalLayout();
        tabelle.setWidthFull();
        tabelle.setPadding(false);
        tabelle.setSpacing(false);
        tabelle.getStyle().set("gap", "0.25rem");

        tabelle.add(buildTabellenHeader());

        /*
         * Dummy-Daten – später durch ArtikelService.findAll() ersetzen.
         * Parameter: id, name, kategorie, preis, mwst, bestand,
         *            minBestand, aktiv, warnBestand
         */
        tabelle.add(buildArtikelZeile("#ART-001", "Espresso",        "Heißgetränke", "2,50 €", "7%",  "∞",       "20", true,  false));
        tabelle.add(buildArtikelZeile("#ART-002", "Cappuccino",      "Heißgetränke", "3,80 €", "7%",  "∞",       "20", true,  false));
        tabelle.add(buildArtikelZeile("#ART-003", "Croissant",       "Gebäck",       "2,20 €", "7%",  "12 Stk.", "20", true,  false));
        tabelle.add(buildArtikelZeile("#ART-004", "Muffin",          "Gebäck",       "2,90 €", "7%",  "2 Stk.",  "20", true,  true));
        tabelle.add(buildArtikelZeile("#ART-005", "Wasser",          "Kaltgetränke", "1,90 €", "19%", "45 Stk.", "20", false, false));
        tabelle.add(buildArtikelZeile("#ART-006", "Latte Macchiato", "Heißgetränke", "4,20 €", "7%",  "∞",       "20", true,  false));

        return tabelle;
    }

    /**
     * Header-Zeile mit allen Spaltenbezeichnungen.
     * Breiten kommen aus den BREITE_*-Konstanten.
     */
    private HorizontalLayout buildTabellenHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle()
                .set("padding", "0 1.5rem 0.5rem 1.5rem")
                .set("gap", "0");

        header.add(
                buildHeaderZelle("ID",            BREITE_ID),
                buildHeaderZelle("Name",          BREITE_NAME),
                buildHeaderZelle("Kategorie",     BREITE_KATEGORIE),
                buildHeaderZelle("Preis",         BREITE_PREIS),
                buildHeaderZelle("MwSt",          BREITE_MWST),
                buildHeaderZelle("Bestand",       BREITE_BESTAND),
                buildHeaderZelle("Minimalgrenze", BREITE_MINIMAL),
                buildHeaderZelle("Status",        BREITE_STATUS),
                buildHeaderZelle("Aktionen",      BREITE_AKTIONEN)
        );
        return header;
    }

    /**
     * Einzelne Spaltenüberschrift.
     *
     * @param text   Spaltenbezeichnung
     * @param breite CSS-Breite aus den BREITE_*-Konstanten
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
     * Eine Artikel-Datenzeile in der Tabelle.
     *
     * Hover-Effekt: Hintergrund aufhellen + Aktionsbuttons einblenden.
     * Warnbestand: Bestand-Zelle wird rot mit Warnsymbol markiert.
     *
     * @param id          Artikel-ID (z.B. "#ART-001")
     * @param name        Artikelname
     * @param kategorie   Kategorie
     * @param preis       formatierter Preis (z.B. "2,50 €")
     * @param mwst        MwSt-Satz (z.B. "7%")
     * @param bestand     aktueller Bestand (z.B. "12 Stk." oder "∞")
     * @param minBestand  Minimalgrenze (z.B. "20")
     * @param aktiv       ob der Artikel aktiv ist
     * @param warnBestand ob der Bestand unter der Minimalgrenze liegt
     */
    private HorizontalLayout buildArtikelZeile(String id, String name,
                                               String kategorie, String preis,
                                               String mwst, String bestand,
                                               String minBestand,
                                               boolean aktiv, boolean warnBestand) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("background", "white")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 1.5rem")
                .set("gap", "0")
                .set("transition", "background 0.15s")
                .set("cursor", "default");

        Span idZelle = new Span(id);
        idZelle.getStyle()
                .set("width", BREITE_ID)
                .set("font-size", "0.7rem")
                .set("font-family", "monospace")
                .set("color", "#82746d")
                .set("opacity", "0.7");

        Span nameZelle = new Span(name);
        nameZelle.getStyle()
                .set("width", BREITE_NAME)
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span kategorieZelle = new Span(kategorie);
        kategorieZelle.getStyle()
                .set("width", BREITE_KATEGORIE)
                .set("font-size", "0.875rem")
                .set("color", "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span preisZelle = new Span(preis);
        preisZelle.getStyle()
                .set("width", BREITE_PREIS)
                .set("font-weight", "600")
                .set("font-size", "0.875rem")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span mwstZelle = new Span(mwst);
        mwstZelle.getStyle()
                .set("width", BREITE_MWST)
                .set("font-size", "0.875rem")
                .set("color", "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        /*
         * Bestand-Zelle: bei Unterschreitung der Minimalgrenze
         * wird ein Warnsymbol vorangestellt und der Text rot markiert.
         */
        HorizontalLayout bestandZelle = new HorizontalLayout();
        bestandZelle.setAlignItems(FlexComponent.Alignment.CENTER);
        bestandZelle.setPadding(false);
        bestandZelle.setSpacing(false);
        bestandZelle.getStyle()
                .set("width", BREITE_BESTAND)
                .set("gap", "0.4rem");

        if (warnBestand) {
            Span warnIcon = createIcon("warning");
            warnIcon.getStyle()
                    .set("font-size", "1rem")
                    .set("color", "#ba1a1a");
            bestandZelle.add(warnIcon);
        }

        Span bestandText = new Span(bestand);
        bestandText.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", warnBestand ? "700" : "400")
                .set("color", warnBestand ? "#ba1a1a" : "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        bestandZelle.add(bestandText);

        Span minBestandZelle = new Span(minBestand);
        minBestandZelle.getStyle()
                .set("width", BREITE_MINIMAL)
                .set("font-size", "0.875rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Div statusZelle = new Div(buildStatusBadge(aktiv));
        statusZelle.getStyle().set("width", BREITE_STATUS);

        Div aktionenZelle = buildAktionenZelle(aktiv);

        /*
         * Hover per JavaScript: Hintergrund + Aktionen-Sichtbarkeit.
         * Inline-Events weil Vaadin kein direktes :hover für Java hat.
         */
        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.background = '#f0eeff';" +
                        "  const btns = this.querySelector('.aktionen-gruppe');" +
                        "  if (btns) btns.style.opacity = '1';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.background = 'white';" +
                        "  const btns = this.querySelector('.aktionen-gruppe');" +
                        "  if (btns) btns.style.opacity = '0';" +
                        "});"
        );

        zeile.add(idZelle, nameZelle, kategorieZelle, preisZelle,
                mwstZelle, bestandZelle, minBestandZelle,
                statusZelle, aktionenZelle);

        return zeile;
    }

    /**
     * Status-Badge: olivgrün für Aktiv, grau für Inaktiv.
     *
     * @param aktiv ob der Artikel aktiv ist
     */
    private Span buildStatusBadge(boolean aktiv) {
        Span badge = new Span(aktiv ? "Aktiv" : "Inaktiv");
        badge.getStyle()
                .set("padding", "0.2rem 0.75rem")
                .set("border-radius", "9999px")
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("letter-spacing", "0.025em")
                .set("background", aktiv ? "#e1e1c9" : "rgba(212,195,186,0.3)")
                .set("color", aktiv ? "#474836" : "#82746d");
        return badge;
    }

    /**
     * Aktionsbuttons-Zelle (Bearbeiten + Sichtbarkeit).
     * Standardmäßig unsichtbar – erscheinen beim Hover der Zeile.
     *
     * @param aktiv beeinflusst das Sichtbarkeits-Icon (ein/ausblenden)
     */
    private Div buildAktionenZelle(boolean aktiv) {
        Div zelle = new Div();
        zelle.addClassName("aktionen-gruppe");
        zelle.getStyle()
                .set("width", BREITE_AKTIONEN)
                .set("display", "flex")
                .set("justify-content", "flex-start")
                .set("gap", "0.5rem")
                .set("opacity", "0")
                .set("transition", "opacity 0.15s");

        zelle.add(
                buildAktionsButton("edit", "#553722", "#ffdcc6"),
                buildAktionsButton(
                        aktiv ? "visibility_off" : "visibility",
                        aktiv ? "#ba1a1a" : "#553722",
                        aktiv ? "#ffdad6" : "#ffdcc6"
                )
        );
        return zelle;
    }

    /**
     * Einzelner Icon-only Aktionsbutton.
     *
     * @param iconName   Material Symbol Icon-Name
     * @param iconFarbe  Farbe des Icons im Normalzustand
     * @param hoverFarbe Hintergrundfarbe beim Hover
     */
    private Button buildAktionsButton(String iconName,
                                      String iconFarbe,
                                      String hoverFarbe) {
        Span icon = createIcon(iconName);
        icon.getStyle()
                .set("font-size", "1.1rem")
                .set("color", iconFarbe);

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0.4rem")
                .set("border-radius", "0.5rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("min-width", "unset")
                .set("transition", "background 0.15s");

        btn.getElement().executeJs(
                "this.addEventListener('mouseenter', () => this.style.background = '" + hoverFarbe + "');" +
                        "this.addEventListener('mouseleave', () => this.style.background = 'none');"
        );

        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIK-KARTEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Vier Statistik-Karten als Zusammenfassung unterhalb der Tabelle:
     * Gesamtartikel, Aktiv, Niedriger Bestand, Kategorien.
     */
    private HorizontalLayout buildStatistikKarten() {
        HorizontalLayout reihe = new HorizontalLayout();
        reihe.setWidthFull();
        reihe.setSpacing(false);
        reihe.getStyle()
                .set("gap", "1.5rem")
                .set("margin-top", "1.5rem");

        reihe.add(
                buildStatistikKarte("GESAMTARTIKEL",     "24", "inventory_2",  false),
                buildStatistikKarte("AKTIV",             "18", "check_circle", false),
                buildStatistikKarte("NIEDRIGER BESTAND", "2",  "warning",      true),
                buildStatistikKarte("KATEGORIEN",        "4",  "category",     false)
        );
        return reihe;
    }

    /**
     * Einzelne Statistik-Karte mit Label, großem Zahlenwert und Icon.
     *
     * @param label   Bezeichnung (z.B. "GESAMTARTIKEL")
     * @param wert    anzuzeigender Wert (z.B. "24")
     * @param icon    Material Symbol Icon-Name
     * @param warnung ob diese Karte in Rot hervorgehoben wird
     */
    private VerticalLayout buildStatistikKarte(String label, String wert,
                                               String icon, boolean warnung) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1")
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "1.5rem")
                .set("gap", "0.5rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.65rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", warnung ? "#ba1a1a" : "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout wertZeile = new HorizontalLayout();
        wertZeile.setPadding(false);
        wertZeile.setSpacing(false);
        wertZeile.getStyle()
                .set("justify-content", "space-between")
                .set("align-items", "flex-end")
                .set("width", "100%");

        Span wertSpan = new Span(wert);
        wertSpan.getStyle()
                .set("font-size", "2.5rem")
                .set("font-weight", "900")
                .set("color", warnung ? "#ba1a1a" : "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("line-height", "1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("width", "2.5rem")
                .set("height", "2.5rem")
                .set("border-radius", "9999px")
                .set("background", warnung ? "#ffdad6" : "#efecff")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span iconSpan = createIcon(icon);
        iconSpan.getStyle()
                .set("font-size", "1.1rem")
                .set("color", warnung ? "#ba1a1a" : "#553722");
        iconBox.add(iconSpan);

        wertZeile.add(wertSpan, iconBox);
        karte.add(labelSpan, wertZeile);
        return karte;
    }

    // ═══════════════════════════════════════════════════════════
    // PAGINATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Pagination mit Info-Text links und Seitenbuttons rechts.
     * Im Prototyp ohne Funktionalität.
     */
    private HorizontalLayout buildPagination() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.setWidthFull();
        pagination.setAlignItems(FlexComponent.Alignment.CENTER);
        pagination.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        pagination.setPadding(false);
        pagination.getStyle().set("margin-top", "1.5rem");

        Span info = new Span("Zeige 1–6 von 24 Artikeln");
        info.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout seitenButtons = new HorizontalLayout();
        seitenButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        seitenButtons.setSpacing(false);
        seitenButtons.getStyle().set("gap", "0.25rem");

        seitenButtons.add(
                buildPaginationButton("chevron_left",  true),
                buildSeiteButton("1", true),
                buildSeiteButton("2", false),
                buildSeiteButton("3", false),
                buildPaginationButton("chevron_right", false)
        );

        pagination.add(info, seitenButtons);
        return pagination;
    }

    /**
     * Navigations-Pfeil-Button für die Pagination.
     *
     * @param iconName Material Symbol Icon ("chevron_left" oder "chevron_right")
     * @param deaktiv  ob der Button ausgegraut dargestellt wird
     */
    private Button buildPaginationButton(String iconName, boolean deaktiv) {
        Button btn = new Button();
        Span icon = createIcon(iconName);
        icon.getStyle().set("font-size", "1.25rem");
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", deaktiv ? "default" : "pointer")
                .set("padding", "0.5rem")
                .set("border-radius", "0.75rem")
                .set("opacity", deaktiv ? "0.3" : "1")
                .set("color", "#553722")
                .set("min-width", "unset");
        return btn;
    }

    /**
     * Seiten-Nummer-Button für die Pagination.
     *
     * @param nummer anzuzeigende Seitenzahl
     * @param aktiv  ob diese Seite gerade aktiv ist
     */
    private Button buildSeiteButton(String nummer, boolean aktiv) {
        Button btn = new Button(nummer);
        btn.getStyle()
                .set("width", "2.5rem")
                .set("height", "2.5rem")
                .set("border-radius", "0.75rem")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-weight", aktiv ? "700" : "500")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("font-size", "0.875rem")
                .set("background", aktiv ? "#553722" : "none")
                .set("color", aktiv ? "white" : "#50453e");
        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt einen Material Symbols Icon-Span.
     *
     * @param iconName Name des Icons (z.B. "edit", "warning", "label")
     */
    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }
}