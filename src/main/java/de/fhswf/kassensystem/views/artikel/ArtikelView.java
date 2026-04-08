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
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.views.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ArtikelView zeigt alle Artikel des Cafés in einer Tabelle.
 *
 * Aufbau:
 * - Header: Titel + Suchfeld + "Neuer Artikel" Button
 * - Tabelle: ID, Name, Kategorie, Preis, MwSt, Bestand, Minimalgrenze, Status, Aktionen
 * - Statistik-Karten: Gesamtartikel, Aktiv, Niedriger Bestand, Kategorien
 *
 * Daten kommen aus ArtikelService.
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

    private final ArtikelService artikelService;

    /*
     * tabelle: Instanzfeld damit buildTabelle() und ladeArtikel()
     * auf dasselbe Layout zugreifen können.
     */
    private final VerticalLayout tabelle = new VerticalLayout();

    /*
     * statistikKarten: Instanzfeld damit die Karten nach Aktionen
     * neu geladen werden können.
     */
    private final HorizontalLayout statistikKartenLayout = new HorizontalLayout();

    public ArtikelView(ArtikelService artikelService) {
        this.artikelService = artikelService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box")
                .set("overflow-y", "auto");

        tabelle.setWidthFull();
        tabelle.setPadding(false);
        tabelle.setSpacing(false);
        tabelle.getStyle().set("gap", "0.25rem");

        statistikKartenLayout.setWidthFull();
        statistikKartenLayout.setSpacing(false);
        statistikKartenLayout.getStyle()
                .set("gap", "1.5rem")
                .set("margin-top", "1.5rem");

        add(
                buildHeader(),
                buildTabellenBereich(),
                statistikKartenLayout
        );

        ladeArtikel(null);
        ladeStatistikKarten();
    }

    // ═══════════════════════════════════════════════════════════
    // DATEN LADEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Lädt Artikel aus dem ArtikelService und befüllt die Tabelle.
     * Bei suchBegriff != null wird gefiltert, sonst alle Artikel geladen.
     *
     * @param suchBegriff Suchbegriff oder null für alle Artikel
     */
    private void ladeArtikel(String suchBegriff) {
        tabelle.removeAll();
        tabelle.add(buildTabellenHeader());

        List<Artikel> artikel;
        if (suchBegriff != null && !suchBegriff.isBlank()) {
            artikel = artikelService.findByName(suchBegriff);
        } else {
            artikel = artikelService.findAllArtikel();
        }
        // Nach ID aufsteigend sortieren
        artikel.sort(java.util.Comparator.comparing(Artikel::getId));

        for (Artikel a : artikel) {
            boolean warnBestand = a.getBestand() < a.getMinimalbestand();
            String bestandText = a.getBestand() == Integer.MAX_VALUE
                    ? "∞"
                    : a.getBestand() + " Stk.";
            String preisText = String.format("%,.2f €", a.getPreis());
            String mwstText  = a.getMehrwertsteuer().getSatz()
                    .stripTrailingZeros().toPlainString() + "%";
            String idText    = "#ART-" + String.format("%03d", a.getId());

            tabelle.add(buildArtikelZeile(
                    idText,
                    a.getName(),
                    a.getKategorie().getName(),
                    preisText,
                    mwstText,
                    bestandText,
                    String.valueOf(a.getMinimalbestand()),
                    a.isAktiv(),
                    warnBestand,
                    a
            ));
        }
    }

    /**
     * Lädt die Statistik-Karten mit echten Zahlen aus dem Service.
     */
    private void ladeStatistikKarten() {
        statistikKartenLayout.removeAll();

        List<Artikel> alle   = artikelService.findAllArtikel();
        long gesamtArtikel   = alle.size();
        long aktiv           = alle.stream().filter(Artikel::isAktiv).count();
        long niedrigBestand  = alle.stream()
                .filter(a -> a.getBestand() < a.getMinimalbestand()).count();
        long kategorien      = alle.stream()
                .map(a -> a.getKategorie().getId())
                .collect(Collectors.toSet()).size();

        statistikKartenLayout.add(
                buildStatistikKarte("GESAMTARTIKEL",     String.valueOf(gesamtArtikel), "inventory_2",  false),
                buildStatistikKarte("AKTIV",             String.valueOf(aktiv),          "check_circle", false),
                buildStatistikKarte("NIEDRIGER BESTAND", String.valueOf(niedrigBestand), "warning",      true),
                buildStatistikKarte("KATEGORIEN",        String.valueOf(kategorien),     "category",     false)
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

        /*
         * Suche bei jeder Eingabe – kein Button nötig.
         * ValueChangeListener feuert bei jedem Tastendruck.
         */
        suchfeld.addValueChangeListener(e -> ladeArtikel(e.getValue()));

        aktionen.add(suchfeld, buildNeuerArtikelButton());
        return aktionen;
    }

    /**
     * "Neuer Artikel" Button mit Gradient und Plus-Icon.
     * Öffnet NeuerArtikelDialog und lädt nach dem Schließen die Tabelle neu.
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

        btn.addClickListener(event -> {
            NeuerArtikelDialog dialog = new NeuerArtikelDialog(artikelService);
            /*
             * Nach dem Schließen des Dialogs Tabelle und Statistik neu laden,
             * damit der neue Artikel sofort erscheint.
             */
            dialog.addOpenedChangeListener(e -> {
                if (!e.isOpened()) {
                    ladeArtikel(null);
                    ladeStatistikKarten();
                }
            });
            dialog.open();
        });

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

        bereich.add(tabelle);
        return bereich;
    }

    /**
     * Header-Zeile mit allen Spaltenbezeichnungen.
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
     * @param id          Artikel-ID (z.B. "#ART-001")
     * @param name        Artikelname
     * @param kategorie   Kategoriename
     * @param preis       formatierter Preis
     * @param mwst        MwSt-Satz
     * @param bestand     Bestandstext
     * @param minBestand  Minimalbestand
     * @param aktiv       ob der Artikel aktiv ist
     * @param warnBestand ob der Bestand unter der Minimalgrenze liegt
     * @param artikel     das Artikel-Entity für Aktionsbuttons
     */
    private HorizontalLayout buildArtikelZeile(String id, String name,
                                               String kategorie, String preis,
                                               String mwst, String bestand,
                                               String minBestand,
                                               boolean aktiv, boolean warnBestand,
                                               Artikel artikel) {
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

        Div aktionenZelle = buildAktionenZelle(aktiv, artikel);

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
     * Aktionsbuttons-Zelle (Bearbeiten + Sichtbarkeit umschalten).
     * Sichtbarkeit-Button ruft artikelService.deleteArtikel() auf (deaktiviert).
     *
     * @param aktiv   aktueller Aktivstatus
     * @param artikel das Artikel-Entity
     */
    private Div buildAktionenZelle(boolean aktiv, Artikel artikel) {
        Div zelle = new Div();
        zelle.addClassName("aktionen-gruppe");
        zelle.getStyle()
                .set("width", BREITE_AKTIONEN)
                .set("display", "flex")
                .set("justify-content", "flex-start")
                .set("gap", "0.5rem")
                .set("opacity", "0")
                .set("transition", "opacity 0.15s");

        // Bearbeiten-Button öffnet NeuerArtikelDialog im Bearbeitungsmodus
        Button editBtn = buildAktionsButton("edit", "#553722", "#ffdcc6");
        editBtn.addClickListener(e -> {
            NeuerArtikelDialog dialog = new NeuerArtikelDialog(artikelService, artikel);
            dialog.addOpenedChangeListener(ev -> {
                if (!ev.isOpened()) {
                    ladeArtikel(null);
                    ladeStatistikKarten();
                }
            });
            dialog.open();
        });

        // Sichtbarkeit umschalten (aktiv/inaktiv)
        Button sichtbarBtn = buildAktionsButton(
                aktiv ? "visibility_off" : "visibility",
                aktiv ? "#ba1a1a" : "#553722",
                aktiv ? "#ffdad6" : "#ffdcc6"
        );
        sichtbarBtn.addClickListener(e -> {
            /*
             * deleteArtikel() setzt aktiv=false (Soft-Delete).
             * Für Reaktivierung müsste ein updateArtikel() verwendet werden.
             */
            if (aktiv) {
                artikelService.deleteArtikel(artikel.getId());
            } else {
                artikel.setAktiv(true);
                artikelService.updateArtikel(artikel);
            }
            ladeArtikel(null);
            ladeStatistikKarten();
        });

        zelle.add(editBtn, sichtbarBtn);
        return zelle;
    }

    /**
     * Einzelner Icon-only Aktionsbutton.
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
     * Einzelne Statistik-Karte mit Label, Zahlenwert und Icon.
     *
     * @param label   Bezeichnung
     * @param wert    Wert als String
     * @param icon    Material Symbol Icon-Name
     * @param warnung ob diese Karte rot hervorgehoben wird
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