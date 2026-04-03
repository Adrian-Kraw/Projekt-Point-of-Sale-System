package de.fhswf.kassensystem.views.berichte;

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
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

/**
 * BerichteView zeigt Tagesauswertungen und Umsatzberichte.
 *
 * Aufbau:
 * - Header: Titel + PDF-Export Button
 * - Tab-Navigation: Tagesabschluss / Umsatzübersicht / Artikelstatistik
 * - Datum-Zeile
 * - Dynamischer Tab-Inhalt:
 *   - Tagesabschluss: Metric-Karten, Zahlungsarten, Top Seller
 *   - Umsatzübersicht: Wochenbalkendiagramm
 *   - Artikelstatistik: Verkaufsranking mit Fortschrittsbalken
 *
 * Im Prototyp mit Dummy-Daten – später mit VerkaufService verbunden.
 */
@Route(value = "berichte", layout = MainLayout.class)
public class BerichteView extends VerticalLayout {

    /**
     * tabInhalt: Container dessen Inhalt je nach aktivem Tab wechselt.
     * Als Instanzfeld damit buildTabNavigation() darauf zugreifen kann.
     */
    private final Div tabInhalt = new Div();

    public BerichteView() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box");

        tabInhalt.setWidthFull();
        tabInhalt.add(buildTagesabschlussInhalt());

        add(
                buildHeader(),
                buildTabNavigation(),
                buildDatumZeile(),
                tabInhalt
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    /**
     * Header mit Titel links und PDF-Export Button rechts.
     */
    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(false);
        header.getStyle().set("margin-bottom", "2rem");

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

        Span icon = createIcon("bar_chart");
        icon.getStyle()
                .set("color", "#553722")
                .set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Berichte & Auswertungen");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.75rem")
                .set("font-weight", "800")
                .set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);

        /*
         * PDF-Export Button – im Prototyp ohne Funktion.
         * TODO: PDF-Generierung mit iText oder JasperReports einbinden.
         */
        Button exportBtn = new Button();
        Span downloadIcon = createIcon("download");
        Span exportText = new Span("Als PDF exportieren");
        exportText.getStyle()
                .set("font-weight", "600")
                .set("font-size", "0.8rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("letter-spacing", "0.05em")
                .set("text-transform", "uppercase");

        exportBtn.getElement().appendChild(downloadIcon.getElement());
        exportBtn.getElement().appendChild(exportText.getElement());
        exportBtn.getStyle()
                .set("background", "transparent")
                .set("border", "2px solid rgba(85,55,34,0.2)")
                .set("border-radius", "9999px")
                .set("padding", "0.625rem 1.5rem")
                .set("color", "#553722")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.5rem");

        header.add(titelGruppe, exportBtn);
        return header;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB-NAVIGATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Tab-Navigation mit drei Tabs.
     * Beim Klick wird tabInhalt ausgetauscht.
     */
    private HorizontalLayout buildTabNavigation() {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.setSpacing(false);
        tabs.getStyle()
                .set("gap", "3rem")
                .set("border-bottom", "1px solid rgba(130,116,109,0.15)")
                .set("margin-bottom", "2rem");

        Span tagesTab   = buildTab("Tagesabschluss",  true);
        Span umsatzTab  = buildTab("Umsatzübersicht", false);
        Span artikelTab = buildTab("Artikelstatistik",false);

        tagesTab.addClickListener(e -> {
            zeigeTab(tagesTab, new Span[]{umsatzTab, artikelTab});
            tabInhalt.removeAll();
            tabInhalt.add(buildTagesabschlussInhalt());
        });

        umsatzTab.addClickListener(e -> {
            zeigeTab(umsatzTab, new Span[]{tagesTab, artikelTab});
            tabInhalt.removeAll();
            tabInhalt.add(buildUmsatzuebersichtInhalt());
        });

        artikelTab.addClickListener(e -> {
            zeigeTab(artikelTab, new Span[]{tagesTab, umsatzTab});
            tabInhalt.removeAll();
            tabInhalt.add(buildArtikelstatistikInhalt());
        });

        tabs.add(tagesTab, umsatzTab, artikelTab);
        return tabs;
    }

    /**
     * Einzelner Tab-Span.
     *
     * @param label Anzeigename
     * @param aktiv ob initial aktiv
     */
    private Span buildTab(String label, boolean aktiv) {
        Span tab = new Span(label);
        tab.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em")
                .set("padding-bottom", "1rem")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("color", aktiv ? "#553722" : "rgba(85,55,34,0.4)")
                .set("border-bottom", aktiv ? "3px solid #6f4e37" : "3px solid transparent")
                .set("transition", "all 0.2s");
        return tab;
    }

    /**
     * Setzt den aktiven Tab visuell und alle anderen inaktiv.
     *
     * @param aktiv   der neu aktive Tab
     * @param inaktive alle anderen Tabs
     */
    private void zeigeTab(Span aktiv, Span[] inaktive) {
        aktiv.getStyle()
                .set("color", "#553722")
                .set("border-bottom", "3px solid #6f4e37");
        for (Span s : inaktive) {
            s.getStyle()
                    .set("color", "rgba(85,55,34,0.4)")
                    .set("border-bottom", "3px solid transparent");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DATUM-ZEILE
    // ═══════════════════════════════════════════════════════════

    /**
     * Datum-Auswahl links und Echtzeit-Hinweis rechts.
     */
    private HorizontalLayout buildDatumZeile() {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setPadding(false);
        zeile.getStyle().set("margin-bottom", "2rem");

        HorizontalLayout datumPicker = new HorizontalLayout();
        datumPicker.setAlignItems(FlexComponent.Alignment.CENTER);
        datumPicker.setSpacing(false);
        datumPicker.getStyle()
                .set("background", "#f5f2ff")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 1.25rem")
                .set("gap", "0.75rem")
                .set("cursor", "pointer");

        Span kalenderIcon = createIcon("calendar_today");
        kalenderIcon.getStyle().set("color", "rgba(85,55,34,0.6)");

        Span datum = new Span("27.10.2025");
        datum.getStyle()
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span arrowIcon = createIcon("expand_more");
        arrowIcon.getStyle()
                .set("color", "rgba(85,55,34,0.6)")
                .set("font-size", "1rem");

        datumPicker.add(kalenderIcon, datum, arrowIcon);

        Span echtzeit = new Span("Daten werden in Echtzeit aktualisiert");
        echtzeit.getStyle()
                .set("font-size", "0.8rem")
                .set("font-style", "italic")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        zeile.add(datumPicker, echtzeit);
        return zeile;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 1: TAGESABSCHLUSS
    // ═══════════════════════════════════════════════════════════

    /**
     * Inhalt des Tagesabschluss-Tabs.
     * Metric-Karten + Zahlungsarten + Top Seller.
     */
    private VerticalLayout buildTagesabschlussInhalt() {
        VerticalLayout inhalt = new VerticalLayout();
        inhalt.setWidthFull();
        inhalt.setPadding(false);
        inhalt.setSpacing(false);
        inhalt.getStyle().set("gap", "2rem");

        inhalt.add(buildMetricKarten(), buildUntererBereich());
        return inhalt;
    }

    /**
     * Drei Metric-Karten: Gesamtumsatz, Transaktionen, Ø Bon-Wert.
     */
    private HorizontalLayout buildMetricKarten() {
        HorizontalLayout reihe = new HorizontalLayout();
        reihe.setWidthFull();
        reihe.setSpacing(false);
        reihe.getStyle().set("gap", "1.5rem");

        reihe.add(
                buildMetricKarte("GESAMTUMSATZ",  "847,50€", "payments",     "+12.4% vs. gestern", true),
                buildMetricKarte("TRANSAKTIONEN", "34",       "receipt_long", "Durchschn. 4.2 / Std.", false),
                buildMetricKarte("Ø BON-WERT",    "24,93€",  "coffee",       "Zielwert: 22,00€", false)
        );
        return reihe;
    }

    /**
     * Einzelne Metric-Karte mit großem Zahlenwert und dekorativem Hintergrund-Icon.
     *
     * @param label    Bezeichnung
     * @param wert     anzuzeigender Wert
     * @param icon     Material Symbol für das Hintergrund-Icon
     * @param subtext  Zusatzinfo unter dem Wert
     * @param positiv  ob der Subtext grün dargestellt wird
     */
    private VerticalLayout buildMetricKarte(String label, String wert,
                                            String icon, String subtext,
                                            boolean positiv) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1")
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "0.5rem")
                .set("position", "relative")
                .set("overflow", "hidden");

        Span bgIcon = createIcon(icon);
        bgIcon.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("right", "0")
                .set("font-size", "6rem")
                .set("color", "#553722")
                .set("opacity", "0.05")
                .set("pointer-events", "none")
                .set("line-height", "1");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.65rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", "rgba(85,55,34,0.6)")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("position", "relative");

        Span wertSpan = new Span(wert);
        wertSpan.getStyle()
                .set("font-size", "3rem")
                .set("font-weight", "900")
                .set("color", "#553722")
                .set("letter-spacing", "-0.025em")
                .set("line-height", "1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("margin-top", "0.5rem")
                .set("position", "relative");

        HorizontalLayout subtextZeile = new HorizontalLayout();
        subtextZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        subtextZeile.setSpacing(false);
        subtextZeile.getStyle()
                .set("gap", "0.4rem")
                .set("position", "relative");

        if (positiv) {
            Span trendIcon = createIcon("trending_up");
            trendIcon.getStyle()
                    .set("font-size", "1rem")
                    .set("color", "#16a34a");
            subtextZeile.add(trendIcon);
        }

        Span subtextSpan = new Span(subtext);
        subtextSpan.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "700")
                .set("color", positiv ? "#16a34a" : "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        subtextZeile.add(subtextSpan);
        karte.add(bgIcon, labelSpan, wertSpan, subtextZeile);
        return karte;
    }

    /**
     * Zwei-Spalten-Layout: Zahlungsarten links, Top Seller rechts.
     */
    private HorizontalLayout buildUntererBereich() {
        HorizontalLayout bereich = new HorizontalLayout();
        bereich.setWidthFull();
        bereich.setAlignItems(FlexComponent.Alignment.START);
        bereich.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bereich.setSpacing(false);
        bereich.getStyle()
                .set("gap", "3rem")
                .set("flex-wrap", "wrap");

        bereich.add(buildZahlungsarten(), buildTopSeller());
        return bereich;
    }

    /**
     * Zahlungsarten mit Toggle Torte/Balken.
     */
    private VerticalLayout buildZahlungsarten() {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
//        spalte.getStyle()
//                .set("flex", "1")
//                .set("max-width", "480px")
//                .set("gap", "1.5rem");
        spalte.getStyle()
                .set("flex", "0 0 780px")
                .set("width", "780px")
                .set("max-width", "780px")
                .set("gap", "1.5rem");

        HorizontalLayout titelZeile = new HorizontalLayout();
        titelZeile.setWidthFull();
        titelZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        titelZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titelZeile.setPadding(false);

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "0.75rem");

        Div akzent = new Div();
        akzent.getStyle()
                .set("width", "0.25rem")
                .set("height", "1.5rem")
                .set("background", "#553722")
                .set("border-radius", "9999px");

        H4 titel = new H4("Zahlungsarten");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(akzent, titel);

        /*
         * Diagramm-Daten: Label, Wert, Farbe
         * Wird an buildDiagramm() übergeben – dort wird je nach Modus
         * ein Balken- oder Tortendiagramm als reines SVG gerendert.
         */
        String[][] daten = {
                {"Bar",   "423.00", "#ffdcc6"},
                {"Karte", "424.50", "#553722"}
        };

        Div diagrammContainer = new Div();
        diagrammContainer.setWidthFull();
        diagrammContainer.getStyle()
                .set("background", "#f5f2ff")
                .set("border-radius", "1rem")
                .set("padding", "1.5rem");
        diagrammContainer.add(buildDiagramm(daten, true));

        HorizontalLayout toggle = new HorizontalLayout();
        toggle.setSpacing(false);
        toggle.getStyle()
                .set("background", "#efecff")
                .set("border-radius", "9999px")
                .set("padding", "0.25rem")
                .set("gap", "0.25rem");

        Button torteBtn  = buildToggleButton("Torte",  false);
        Button balkenBtn = buildToggleButton("Balken", true);

        torteBtn.addClickListener(e -> {
            diagrammContainer.removeAll();
            diagrammContainer.add(buildDiagramm(daten, false));
            torteBtn.getStyle().set("background", "#553722").set("color", "white");
            balkenBtn.getStyle().set("background", "transparent").set("color", "#553722");
        });

        balkenBtn.addClickListener(e -> {
            diagrammContainer.removeAll();
            diagrammContainer.add(buildDiagramm(daten, true));
            balkenBtn.getStyle().set("background", "#553722").set("color", "white");
            torteBtn.getStyle().set("background", "transparent").set("color", "#553722");
        });

        toggle.add(torteBtn, balkenBtn);
        titelZeile.add(titelGruppe, toggle);

        HorizontalLayout kartenZeile = new HorizontalLayout();
        kartenZeile.setWidthFull();
        kartenZeile.setSpacing(false);
        kartenZeile.getStyle().set("gap", "1rem");
        kartenZeile.add(
                buildZahlungsKarte("Bar",   "423,00€", "wallet"),
                buildZahlungsKarte("Karte", "424,50€", "credit_card")
        );

        spalte.add(titelZeile, kartenZeile, diagrammContainer);
        return spalte;
    }

    /**
     * Toggle-Button für Torte/Balken.
     *
     * @param label Beschriftung
     * @param aktiv ob initial aktiv
     */
    private Button buildToggleButton(String label, boolean aktiv) {
        Button btn = new Button(label);
        btn.getStyle()
                .set("background", aktiv ? "#553722" : "transparent")
                .set("color", aktiv ? "white" : "#553722")
                .set("border", "none")
                .set("border-radius", "9999px")
                .set("padding", "0.25rem 0.75rem")
                .set("font-size", "0.65rem")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("transition", "all 0.2s");
        return btn;
    }

    /**
     * Unified Diagramm-Renderer via HTML Canvas + JavaScript.
     *
     * Zeichnet Balken- oder Tortendiagramm auf einem Canvas-Element.
     * Canvas garantiert korrekte Proportionen unabhängig vom Vaadin-Layout,
     * da width/height in Pixeln fix gesetzt werden.
     *
     * Daten-Format: String[][] mit je {label, wert, farbe}
     *
     * @param daten  Diagramm-Daten als String[][]
     * @param balken true = Balkendiagramm, false = Tortendiagramm
     */
    private Div buildDiagramm(String[][] daten, boolean balken) {
        Div container = new Div();
        container.getStyle()
                .set("width", "100%")
                .set("display", "flex")
                .set("gap", "1.5rem")
                .set("align-items", "center");

        // Legende links
        Div legende = new Div();
        legende.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0.75rem")
                .set("flex-shrink", "0")
                .set("min-width", "120px");

        double summe = 0;
        for (String[] x : daten) summe += Double.parseDouble(x[1]);

        for (String[] d : daten) {
            double wert = Double.parseDouble(d[1]);
            int pct = (int) Math.round(wert / summe * 100);

            Div eintrag = new Div();
            eintrag.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("gap", "0.15rem");

            Div labelZeile = new Div();
            labelZeile.getStyle()
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("gap", "0.5rem");

            Div punkt = new Div();
            punkt.getStyle()
                    .set("width", "10px")
                    .set("height", "10px")
                    .set("border-radius", "50%")
                    .set("background", d[2])
                    .set("flex-shrink", "0");

            Span name = new Span(d[0]);
            name.getStyle()
                    .set("font-size", "0.8rem")
                    .set("font-weight", "700")
                    .set("color", "#553722")
                    .set("font-family", "'Plus Jakarta Sans', sans-serif");

            labelZeile.add(punkt, name);

            Span info = new Span(String.format("%.0f€ · %d%%", wert, pct));
            info.getStyle()
                    .set("font-size", "0.7rem")
                    .set("color", "#82746d")
                    .set("font-family", "'Plus Jakarta Sans', sans-serif")
                    .set("padding-left", "1.25rem");

            eintrag.add(labelZeile, info);
            legende.add(eintrag);
        }

        // Canvas für das Diagramm
        Div canvasWrapper = new Div();
        canvasWrapper.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("justify-content", "center");

        String canvasId = "chart-" + System.nanoTime();
        canvasWrapper.getElement().setProperty("innerHTML",
                "<canvas id='" + canvasId + "' width='200' height='200' " +
                        "style='display:block;'></canvas>");

        String js = balken
                ? buildBalkenJs(canvasId, daten)
                : buildTorteJs(canvasId, daten);

        canvasWrapper.getElement().executeJs(
                "setTimeout(function() { " + js + " }, 100);"
        );

        container.add(legende, canvasWrapper);
        return container;
    }

    /**
     * Erzeugt JavaScript-Code zum Zeichnen eines Balkendiagramms auf einem Canvas.
     * Balkenhöhe proportional zum Maximalwert. Wert-Labels über den Balken.
     *
     * @param canvasId DOM-ID des Canvas-Elements
     * @param daten    {label, wert, farbe} pro Balken
     */
    private String buildBalkenJs(String canvasId, String[][] daten) {
        StringBuilder js = new StringBuilder();
        js.append("var c = document.getElementById('").append(canvasId).append("');");
        js.append("if (!c) return;");
        js.append("var ctx = c.getContext('2d');");
        js.append("var w = c.width, h = c.height;");
        js.append("ctx.clearRect(0,0,w,h);");

        // Daten als JS-Arrays
        js.append("var labels = [");
        for (String[] d : daten) js.append("'").append(d[0]).append("',");
        js.append("];");

        js.append("var werte = [");
        for (String[] d : daten) js.append(d[1]).append(",");
        js.append("];");

        js.append("var farben = [");
        for (String[] d : daten) js.append("'").append(d[2]).append("',");
        js.append("];");

        js.append("var maxW = Math.max.apply(null, werte);");
        js.append("var n = werte.length;");
        js.append("var padT=20, padB=25, padL=10, padR=10;");
        js.append("var chartH = h - padT - padB;");
        js.append("var chartW = w - padL - padR;");
        js.append("var slot = chartW / n;");
        js.append("var bw = slot * 0.5;");

        js.append("for (var i=0; i<n; i++) {");
        js.append("  var bh = (werte[i]/maxW) * chartH;");
        js.append("  var x = padL + slot*i + (slot-bw)/2;");
        js.append("  var y = padT + chartH - bh;");
        // Balken mit abgerundeten oberen Ecken
        js.append("  ctx.fillStyle = farben[i];");
        js.append("  var r=6;");
        js.append("  ctx.beginPath();");
        js.append("  ctx.moveTo(x+r, y);");
        js.append("  ctx.lineTo(x+bw-r, y);");
        js.append("  ctx.quadraticCurveTo(x+bw, y, x+bw, y+r);");
        js.append("  ctx.lineTo(x+bw, y+bh);");
        js.append("  ctx.lineTo(x, y+bh);");
        js.append("  ctx.lineTo(x, y+r);");
        js.append("  ctx.quadraticCurveTo(x, y, x+r, y);");
        js.append("  ctx.closePath();");
        js.append("  ctx.fill();");
        // Wert über dem Balken
        js.append("  ctx.fillStyle = '#553722';");
        js.append("  ctx.font = 'bold 10px Plus Jakarta Sans, sans-serif';");
        js.append("  ctx.textAlign = 'center';");
        js.append("  ctx.fillText(Math.round(werte[i]) + '€', x+bw/2, y-5);");
        // Label unter dem Balken
        js.append("  ctx.fillStyle = '#82746d';");
        js.append("  ctx.font = '9px Plus Jakarta Sans, sans-serif';");
        js.append("  ctx.fillText(labels[i].toUpperCase(), x+bw/2, h-8);");
        js.append("}");

        return js.toString();
    }

    /**
     * Erzeugt JavaScript-Code zum Zeichnen eines Tortendiagramms auf einem Canvas.
     * Perfekter Kreis garantiert da Canvas width === height (200x200px).
     *
     * @param canvasId DOM-ID des Canvas-Elements
     * @param daten    {label, wert, farbe} pro Segment
     */
    private String buildTorteJs(String canvasId, String[][] daten) {
        StringBuilder js = new StringBuilder();
        js.append("var c = document.getElementById('").append(canvasId).append("');");
        js.append("if (!c) return;");
        js.append("var ctx = c.getContext('2d');");
        js.append("var w = c.width, h = c.height;");
        js.append("ctx.clearRect(0,0,w,h);");

        js.append("var werte = [");
        for (String[] d : daten) js.append(d[1]).append(",");
        js.append("];");

        js.append("var farben = [");
        for (String[] d : daten) js.append("'").append(d[2]).append("',");
        js.append("];");

        js.append("var summe = werte.reduce(function(a,b){return a+b;},0);");
        js.append("var cx = w/2, cy = h/2;");
        /*
         * Radius = halbe Canvas-Höhe minus Rand.
         * Da Canvas 200x200 ist, bleibt der Kreis immer ein Kreis.
         */
        js.append("var r = Math.min(w,h)/2 - 10;");
        js.append("var start = -Math.PI/2;");

        js.append("for (var i=0; i<werte.length; i++) {");
        js.append("  var slice = (werte[i]/summe) * 2 * Math.PI;");
        js.append("  ctx.fillStyle = farben[i];");
        js.append("  ctx.beginPath();");
        js.append("  ctx.moveTo(cx, cy);");
        js.append("  ctx.arc(cx, cy, r, start, start+slice);");
        js.append("  ctx.closePath();");
        js.append("  ctx.fill();");
        // Trennlinie zwischen Segmenten
        js.append("  ctx.strokeStyle = 'white';");
        js.append("  ctx.lineWidth = 2;");
        js.append("  ctx.stroke();");
        js.append("  start += slice;");
        js.append("}");

        return js.toString();
    }

    /**
     * Einzelne Zahlungsart-Karte (Bar oder Karte).
     *
     * @param label  Zahlungsart-Bezeichnung
     * @param betrag formatierter Betrag
     * @param icon   Material Symbol Icon
     */
    private HorizontalLayout buildZahlungsKarte(String label, String betrag,
                                                String icon) {
        HorizontalLayout karte = new HorizontalLayout();
        karte.setAlignItems(FlexComponent.Alignment.CENTER);
        karte.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1")
                .set("background", "#efecff")
                .set("border-radius", "1rem")
                .set("padding", "1.5rem");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.65rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em")
                .set("color", "rgba(85,55,34,0.6)")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span betragSpan = new Span(betrag);
        betragSpan.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "900")
                .set("color", "#553722")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(labelSpan, betragSpan);

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("width", "3rem")
                .set("height", "3rem")
                .set("border-radius", "9999px")
                .set("background", "white")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)");

        Span iconSpan = createIcon(icon);
        iconSpan.getStyle()
                .set("color", "#553722")
                .set("font-variation-settings", "'FILL' 1");
        iconBox.add(iconSpan);

        karte.add(info, iconBox);
        return karte;
    }

    /**
     * Top Seller Karte (rechte Spalte im Tagesabschluss).
     */
    private VerticalLayout buildTopSeller() {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "0 0 320px")
                .set("max-width", "320px")
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("border", "2px solid #553722")
                .set("padding", "2rem")
                .set("gap", "1.5rem");

        HorizontalLayout kopf = new HorizontalLayout();
        kopf.setWidthFull();
        kopf.setAlignItems(FlexComponent.Alignment.START);
        kopf.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopf.setPadding(false);

        VerticalLayout titelBlock = new VerticalLayout();
        titelBlock.setPadding(false);
        titelBlock.setSpacing(false);

        H3 titel = new H3("Top Seller Heute");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "900")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph untertitel = new Paragraph(
                "Basierend auf der Anzahl der verkauften Artikel");
        untertitel.getStyle()
                .set("margin", "0")
                .set("font-size", "0.7rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelBlock.add(titel, untertitel);

        Div sternBox = new Div();
        sternBox.getStyle()
                .set("width", "3rem")
                .set("height", "3rem")
                .set("border-radius", "9999px")
                .set("background", "#f5f2ff")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");
        Span sternIcon = createIcon("star");
        sternIcon.getStyle().set("color", "#553722");
        sternBox.add(sternIcon);

        kopf.add(titelBlock, sternBox);

        VerticalLayout liste = new VerticalLayout();
        liste.setWidthFull();
        liste.setPadding(false);
        liste.setSpacing(false);
        liste.getStyle().set("gap", "0.75rem");

        liste.add(
                buildTopSellerEintrag("Hafer-Cappuccino", "Espresso Bar", "48x", "+8%",   true),
                buildTopSellerEintrag("Espresso Double",  "Espresso Bar", "32x", "+/- 0", null),
                buildTopSellerEintrag("Buttercroissant",  "Bakery",       "24x", "-2%",   false)
        );

        Button statistikBtn = new Button("Vollständige Artikelstatistik");
        statistikBtn.setWidthFull();
        statistikBtn.getStyle()
                .set("background", "#553722")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "1rem")
                .set("padding", "1rem")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("font-size", "0.9rem");

        karte.add(kopf, liste, statistikBtn);
        return karte;
    }

    /**
     * Einzelner Top-Seller Eintrag.
     *
     * @param name      Artikelname
     * @param kategorie Kategorie
     * @param anzahl    Verkaufsanzahl
     * @param trend     Trendwert
     * @param positiv   true=grün, false=rot, null=grau
     */
    private HorizontalLayout buildTopSellerEintrag(String name, String kategorie,
                                                   String anzahl, String trend,
                                                   Boolean positiv) {
        HorizontalLayout eintrag = new HorizontalLayout();
        eintrag.setWidthFull();
        eintrag.setAlignItems(FlexComponent.Alignment.CENTER);
        eintrag.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        eintrag.setPadding(false);
        eintrag.getStyle().set("padding", "0.25rem 0");

        HorizontalLayout linkeSeite = new HorizontalLayout();
        linkeSeite.setAlignItems(FlexComponent.Alignment.CENTER);
        linkeSeite.setSpacing(false);
        linkeSeite.getStyle().set("gap", "1rem");

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "2.5rem")
                .set("height", "2.5rem")
                .set("border-radius", "9999px")
                .set("background", "#efecff")
                .set("flex-shrink", "0");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span nameSpan = new Span(name);
        nameSpan.getStyle()
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span katSpan = new Span(kategorie);
        katSpan.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(nameSpan, katSpan);
        linkeSeite.add(avatar, info);

        VerticalLayout rechteSeite = new VerticalLayout();
        rechteSeite.setPadding(false);
        rechteSeite.setSpacing(false);
        rechteSeite.setAlignItems(FlexComponent.Alignment.END);

        Span anzahlSpan = new Span(anzahl);
        anzahlSpan.getStyle()
                .set("font-weight", "900")
                .set("color", "#553722")
                .set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        String trendFarbe = positiv == null ? "#82746d"
                : positiv ? "#16a34a" : "#dc2626";

        Span trendSpan = new Span(trend);
        trendSpan.getStyle()
                .set("font-size", "0.65rem")
                .set("font-weight", "700")
                .set("color", trendFarbe)
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        rechteSeite.add(anzahlSpan, trendSpan);
        eintrag.add(linkeSeite, rechteSeite);
        return eintrag;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 2: UMSATZÜBERSICHT
    // ═══════════════════════════════════════════════════════════

    /**
     * Umsatzübersicht mit Tag/Woche Toggle.
     *
     * Tag-Ansicht:
     *   - Balkendiagramm Bar/Karte pro Stunde (8–18 Uhr)
     *   - Produktverkäufe des Tages als Ranking
     *
     * Wochen-Ansicht:
     *   - Balkendiagramm Bar/Karte pro Tag Mo–Sa (kein Sonntag)
     */
    private VerticalLayout buildUmsatzuebersichtInhalt() {
        VerticalLayout inhalt = new VerticalLayout();
        inhalt.setWidthFull();
        inhalt.setPadding(false);
        inhalt.setSpacing(false);
        inhalt.getStyle().set("gap", "2rem");

        /*
         * Toggle: Tag | Woche
         * Steuert welcher Inhalt im umsatzInhaltContainer gezeigt wird.
         */
        HorizontalLayout kopfZeile = new HorizontalLayout();
        kopfZeile.setWidthFull();
        kopfZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        kopfZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopfZeile.setPadding(false);

        H3 umsatzTitel = new H3("Umsatzübersicht");
        umsatzTitel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.25rem")
                .set("font-weight", "800")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout zeitToggle = new HorizontalLayout();
        zeitToggle.setSpacing(false);
        zeitToggle.getStyle()
                .set("background", "#efecff")
                .set("border-radius", "9999px")
                .set("padding", "0.25rem")
                .set("gap", "0.25rem");

        Button tagBtn   = buildToggleButton("Tag",   true);
        Button wocheBtn = buildToggleButton("Woche", false);

        Div umsatzInhaltContainer = new Div();
        umsatzInhaltContainer.setWidthFull();
        umsatzInhaltContainer.add(buildTagesansicht());

        tagBtn.addClickListener(e -> {
            umsatzInhaltContainer.removeAll();
            umsatzInhaltContainer.add(buildTagesansicht());
            tagBtn.getStyle().set("background", "#553722").set("color", "white");
            wocheBtn.getStyle().set("background", "transparent").set("color", "#553722");
        });

        wocheBtn.addClickListener(e -> {
            umsatzInhaltContainer.removeAll();
            umsatzInhaltContainer.add(buildWochenansicht());
            wocheBtn.getStyle().set("background", "#553722").set("color", "white");
            tagBtn.getStyle().set("background", "transparent").set("color", "#553722");
        });

        zeitToggle.add(tagBtn, wocheBtn);
        kopfZeile.add(umsatzTitel, zeitToggle);

        inhalt.add(kopfZeile, umsatzInhaltContainer);
        return inhalt;
    }

    /**
     * Tagesansicht: Stunden-Balkendiagramm + Produktverkäufe.
     *
     * Zeigt Barzahlung und Kartenzahlung pro Stunde (8–18 Uhr)
     * sowie die meistverkauften Produkte des Tages.
     */
    private VerticalLayout buildTagesansicht() {
        VerticalLayout tagesLayout = new VerticalLayout();
        tagesLayout.setWidthFull();
        tagesLayout.setPadding(false);
        tagesLayout.setSpacing(false);
        tagesLayout.getStyle().set("gap", "1.5rem");

        // Stunden-Diagramm
        VerticalLayout stundenKarte = new VerticalLayout();
        stundenKarte.setWidthFull();
        stundenKarte.setPadding(false);
        stundenKarte.setSpacing(false);
        stundenKarte.getStyle()
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "1.25rem");

        H3 stundenTitel = new H3("Umsatz nach Stunde – 27.10.2025");
        stundenTitel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        /*
         * Dummy-Daten: {Stunde, Bar-Höhe%, Karte-Höhe%}
         * Öffnungszeiten 8–18 Uhr.
         */
        String[][] stundenDaten = {
                {"8",  "20", "15"},
                {"9",  "45", "30"},
                {"10", "60", "55"},
                {"11", "75", "65"},
                {"12", "90", "80"},
                {"13", "85", "90"},
                {"14", "70", "60"},
                {"15", "55", "50"},
                {"16", "65", "70"},
                {"17", "50", "45"},
                {"18", "30", "25"}
        };

        HorizontalLayout stundenDiagramm = new HorizontalLayout();
        stundenDiagramm.setWidthFull();
        stundenDiagramm.setAlignItems(FlexComponent.Alignment.END);
        stundenDiagramm.setSpacing(false);
        stundenDiagramm.getStyle()
                .set("height", "14rem")
                .set("gap", "0.5rem")
                .set("padding", "0 0.5rem");

        for (String[] stunde : stundenDaten) {
            stundenDiagramm.add(buildWochentagBalken(stunde[0] + "h",
                    stunde[1] + "%", stunde[2] + "%"));
        }

        stundenKarte.add(stundenTitel, stundenDiagramm, buildLegende());
        tagesLayout.add(stundenKarte);

        // Produktverkäufe des Tages
        VerticalLayout produktKarte = new VerticalLayout();
        produktKarte.setWidthFull();
        produktKarte.setPadding(false);
        produktKarte.setSpacing(false);
        produktKarte.getStyle()
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "1.25rem");

        H3 produktTitel = new H3("Verkaufte Produkte – 27.10.2025");
        produktTitel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        VerticalLayout produktListe = new VerticalLayout();
        produktListe.setWidthFull();
        produktListe.setPadding(false);
        produktListe.setSpacing(false);
        produktListe.getStyle().set("gap", "0.75rem");

        /*
         * Dummy-Daten Tagesverkäufe: Name, Kategorie, Menge, Umsatz
         */
        String[][] tagProdukte = {
                {"Hafer-Cappuccino",  "Espresso Bar", "48x", "182,40€"},
                {"Espresso Double",   "Espresso Bar", "32x",  "96,00€"},
                {"Buttercroissant",   "Bakery",       "24x",  "52,80€"},
                {"Latte Macchiato",   "Espresso Bar", "21x",  "88,20€"},
                {"Wasser Still",      "Getränke",     "18x",  "34,20€"},
                {"Muffin Schoko",     "Bakery",       "12x",  "34,80€"}
        };

        for (String[] p : tagProdukte) {
            produktListe.add(buildProduktZeile(p[0], p[1], p[2], p[3]));
        }

        produktKarte.add(produktTitel, produktListe);
        tagesLayout.add(produktKarte);
        return tagesLayout;
    }

    /**
     * Wochenansicht: Balkendiagramm Bar/Karte pro Tag Mo–Sa.
     * Sonntag wird nicht angezeigt (Café geschlossen).
     */
    private VerticalLayout buildWochenansicht() {
        VerticalLayout wochenLayout = new VerticalLayout();
        wochenLayout.setWidthFull();
        wochenLayout.setPadding(false);
        wochenLayout.setSpacing(false);
        wochenLayout.getStyle().set("gap", "1.5rem");

        VerticalLayout wochenKarte = new VerticalLayout();
        wochenKarte.setWidthFull();
        wochenKarte.setPadding(false);
        wochenKarte.setSpacing(false);
        wochenKarte.getStyle()
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "1.25rem");

        H3 wochenTitel = new H3("Umsatz nach Zahlungsart – KW 43 (Mo–Sa)");
        wochenTitel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        /*
         * Dummy-Daten pro Wochentag Mo–Sa (kein Sonntag).
         * {Tag, Bar-Höhe%, Karte-Höhe%}
         */
        String[][] wochendaten = {
                {"MO", "40", "50"},
                {"DI", "55", "35"},
                {"MI", "45", "60"},
                {"DO", "70", "40"},
                {"FR", "65", "75"},
                {"SA", "90", "80"}
        };

        HorizontalLayout wochenDiagramm = new HorizontalLayout();
        wochenDiagramm.setWidthFull();
        wochenDiagramm.setAlignItems(FlexComponent.Alignment.END);
        wochenDiagramm.setSpacing(false);
        wochenDiagramm.getStyle()
                .set("height", "16rem")
                .set("gap", "1rem")
                .set("padding", "0 1rem");

        for (String[] tag : wochendaten) {
            wochenDiagramm.add(buildWochentagBalken(tag[0], tag[1] + "%", tag[2] + "%"));
        }

        // Wochen-Zusammenfassung
        HorizontalLayout summaryZeile = new HorizontalLayout();
        summaryZeile.setWidthFull();
        summaryZeile.setSpacing(false);
        summaryZeile.getStyle().set("gap", "1rem");

        summaryZeile.add(
                buildSummaryKarte("Wochenumsatz",   "3.247,50€", "payments"),
                buildSummaryKarte("Transaktionen",  "187",        "receipt_long"),
                buildSummaryKarte("Stärkster Tag",  "Samstag",    "star")
        );

        wochenKarte.add(wochenTitel, wochenDiagramm, buildLegende(), summaryZeile);
        wochenLayout.add(wochenKarte);
        return wochenLayout;
    }

    /**
     * Einzelne Produkt-Zeile für die Tagesansicht.
     *
     * @param name      Produktname
     * @param kategorie Kategorie
     * @param menge     verkaufte Menge (z.B. "48x")
     * @param umsatz    Umsatz (z.B. "182,40€")
     */
    private HorizontalLayout buildProduktZeile(String name, String kategorie,
                                               String menge, String umsatz) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setPadding(false);
        zeile.getStyle()
                .set("padding", "0.5rem 0")
                .set("border-bottom", "1px solid #f5f2ff");

        HorizontalLayout linkeSeite = new HorizontalLayout();
        linkeSeite.setAlignItems(FlexComponent.Alignment.CENTER);
        linkeSeite.setSpacing(false);
        linkeSeite.getStyle().set("gap", "0.75rem");

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "2rem")
                .set("height", "2rem")
                .set("border-radius", "9999px")
                .set("background", "#efecff")
                .set("flex-shrink", "0");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span nameSpan = new Span(name);
        nameSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span katSpan = new Span(kategorie);
        katSpan.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(nameSpan, katSpan);
        linkeSeite.add(avatar, info);

        HorizontalLayout rechteSeite = new HorizontalLayout();
        rechteSeite.setAlignItems(FlexComponent.Alignment.CENTER);
        rechteSeite.setSpacing(false);
        rechteSeite.getStyle().set("gap", "2rem");

        Span mengeSpan = new Span(menge);
        mengeSpan.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "700")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("min-width", "3rem")
                .set("text-align", "right");

        Span umsatzSpan = new Span(umsatz);
        umsatzSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", "900")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("min-width", "5rem")
                .set("text-align", "right");

        rechteSeite.add(mengeSpan, umsatzSpan);
        zeile.add(linkeSeite, rechteSeite);
        return zeile;
    }

    /**
     * Kleine Summary-Karte für die Wochenansicht.
     *
     * @param label Bezeichnung
     * @param wert  anzuzeigender Wert
     * @param icon  Material Symbol Icon
     */
    private VerticalLayout buildSummaryKarte(String label, String wert, String icon) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1")
                .set("background", "#f5f2ff")
                .set("border-radius", "1rem")
                .set("padding", "1rem 1.25rem")
                .set("gap", "0.25rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.6rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout wertZeile = new HorizontalLayout();
        wertZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        wertZeile.setSpacing(false);
        wertZeile.getStyle().set("gap", "0.5rem");

        Span iconSpan = createIcon(icon);
        iconSpan.getStyle()
                .set("font-size", "1rem")
                .set("color", "#553722");

        Span wertSpan = new Span(wert);
        wertSpan.getStyle()
                .set("font-size", "1.1rem")
                .set("font-weight", "900")
                .set("color", "#553722")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        wertZeile.add(iconSpan, wertSpan);
        karte.add(labelSpan, wertZeile);
        return karte;
    }

    /**
     * Legende für Bar/Karte Balkendiagramme (wiederverwendbar).
     */
    private HorizontalLayout buildLegende() {
        HorizontalLayout legende = new HorizontalLayout();
        legende.setSpacing(false);
        legende.getStyle()
                .set("gap", "2rem")
                .set("justify-content", "center");

        for (String[] e : new String[][]{{"#ffdcc6", "Barzahlung"}, {"#553722", "Kartenzahlung"}}) {
            HorizontalLayout item = new HorizontalLayout();
            item.setAlignItems(FlexComponent.Alignment.CENTER);
            item.setSpacing(false);
            item.getStyle().set("gap", "0.5rem");

            Div punkt = new Div();
            punkt.getStyle()
                    .set("width", "0.75rem").set("height", "0.75rem")
                    .set("border-radius", "9999px").set("background", e[0])
                    .set("flex-shrink", "0");

            Span text = new Span(e[1]);
            text.getStyle()
                    .set("font-size", "0.75rem").set("font-weight", "600")
                    .set("color", "#553722")
                    .set("font-family", "'Plus Jakarta Sans', sans-serif");

            item.add(punkt, text);
            legende.add(item);
        }
        return legende;
    }

    /**
     * Einzelner Balken-Slot mit zwei gruppierten Balken (Bar + Karte).
     * Wird für Stunden- und Wochendiagramm verwendet.
     *
     * @param label      Beschriftung (z.B. "MO" oder "10h")
     * @param barHoehe   CSS-Höhe des Bar-Balkens (z.B. "60%")
     * @param karteHoehe CSS-Höhe des Karte-Balkens
     */
    private VerticalLayout buildWochentagBalken(String label,
                                                String barHoehe,
                                                String karteHoehe) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.getStyle()
                .set("flex", "1")
                .set("gap", "0.35rem")
                .set("height", "100%")
                .set("justify-content", "flex-end");

        HorizontalLayout balkenPaar = new HorizontalLayout();
        balkenPaar.setAlignItems(FlexComponent.Alignment.END);
        balkenPaar.setSpacing(false);
        balkenPaar.getStyle()
                .set("gap", "0.15rem")
                .set("width", "100%")
                .set("height", "calc(100% - 1.25rem)");

        Div barBalken = new Div();
        barBalken.getStyle()
                .set("flex", "1")
                .set("height", barHoehe)
                .set("background", "#ffdcc6")
                .set("border-radius", "0.3rem 0.3rem 0 0");

        Div karteBalken = new Div();
        karteBalken.getStyle()
                .set("flex", "1")
                .set("height", karteHoehe)
                .set("background", "#553722")
                .set("border-radius", "0.3rem 0.3rem 0 0");

        balkenPaar.add(barBalken, karteBalken);

        Span tagLabel = new Span(label);
        tagLabel.getStyle()
                .set("font-size", "0.6rem")
                .set("font-weight", "700")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        wrapper.add(balkenPaar, tagLabel);
        return wrapper;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 3: ARTIKELSTATISTIK
    // ═══════════════════════════════════════════════════════════

    /**
     * Artikelstatistik: Verkaufsranking mit Fortschrittsbalken.
     */
    private VerticalLayout buildArtikelstatistikInhalt() {
        VerticalLayout inhalt = new VerticalLayout();
        inhalt.setWidthFull();
        inhalt.setPadding(false);
        inhalt.setSpacing(false);
        inhalt.getStyle().set("gap", "2rem");

        VerticalLayout karte = new VerticalLayout();
        karte.setWidthFull();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("padding", "2rem")
                .set("gap", "1.5rem");

        H3 titel = new H3("Artikelstatistik – Verkaufsranking");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        VerticalLayout liste = new VerticalLayout();
        liste.setWidthFull();
        liste.setPadding(false);
        liste.setSpacing(false);
        liste.getStyle().set("gap", "1rem");

        /*
         * Dummy-Daten: Name, Kategorie, Anzahl als String,
         * Anzahl als int für Fortschrittsbalken, Trend, positiv
         */
        liste.add(buildArtikelStatistikZeile("Hafer-Cappuccino", "Espresso Bar", "48x", 48, "+8%",   true));
        liste.add(buildArtikelStatistikZeile("Espresso Double",  "Espresso Bar", "32x", 32, "+/- 0", null));
        liste.add(buildArtikelStatistikZeile("Buttercroissant",  "Bakery",       "24x", 24, "-2%",   false));
        liste.add(buildArtikelStatistikZeile("Latte Macchiato",  "Espresso Bar", "21x", 21, "+5%",   true));
        liste.add(buildArtikelStatistikZeile("Wasser Still",     "Getränke",     "18x", 18, "+/- 0", null));
        liste.add(buildArtikelStatistikZeile("Muffin Schoko",    "Bakery",       "12x", 12, "-5%",   false));

        karte.add(titel, liste);
        inhalt.add(karte);
        return inhalt;
    }

    /**
     * Einzelne Zeile in der Artikelstatistik mit Fortschrittsbalken.
     *
     * @param name       Artikelname
     * @param kategorie  Kategorie
     * @param anzahl     Verkaufsanzahl als String
     * @param anzahlInt  Verkaufsanzahl als int für den Fortschrittsbalken
     * @param trend      Trendwert
     * @param positiv    true=grün, false=rot, null=grau
     */
    private VerticalLayout buildArtikelStatistikZeile(String name, String kategorie,
                                                      String anzahl, int anzahlInt,
                                                      String trend, Boolean positiv) {
        VerticalLayout zeile = new VerticalLayout();
        zeile.setWidthFull();
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle().set("gap", "0.4rem");

        HorizontalLayout kopf = new HorizontalLayout();
        kopf.setWidthFull();
        kopf.setAlignItems(FlexComponent.Alignment.CENTER);
        kopf.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopf.setPadding(false);

        HorizontalLayout linkeSeite = new HorizontalLayout();
        linkeSeite.setAlignItems(FlexComponent.Alignment.CENTER);
        linkeSeite.setSpacing(false);
        linkeSeite.getStyle().set("gap", "0.75rem");

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "2.25rem")
                .set("height", "2.25rem")
                .set("border-radius", "9999px")
                .set("background", "#efecff")
                .set("flex-shrink", "0");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span nameSpan = new Span(name);
        nameSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span katSpan = new Span(kategorie);
        katSpan.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(nameSpan, katSpan);
        linkeSeite.add(avatar, info);

        HorizontalLayout rechteSeite = new HorizontalLayout();
        rechteSeite.setAlignItems(FlexComponent.Alignment.CENTER);
        rechteSeite.setSpacing(false);
        rechteSeite.getStyle().set("gap", "1rem");

        Span anzahlSpan = new Span(anzahl);
        anzahlSpan.getStyle()
                .set("font-weight", "900")
                .set("font-size", "0.875rem")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        String trendFarbe = positiv == null ? "#82746d"
                : positiv ? "#16a34a" : "#dc2626";

        Span trendSpan = new Span(trend);
        trendSpan.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("color", trendFarbe)
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("min-width", "3rem")
                .set("text-align", "right");

        rechteSeite.add(anzahlSpan, trendSpan);
        kopf.add(linkeSeite, rechteSeite);

        /*
         * Fortschrittsbalken relativ zum Spitzenreiter (48).
         * Breite = (anzahlInt / 48) * 100%
         */
        int prozent = (int) ((anzahlInt / 48.0) * 100);

        Div balkenHintergrund = new Div();
        balkenHintergrund.getStyle()
                .set("width", "100%")
                .set("height", "0.3rem")
                .set("background", "#efecff")
                .set("border-radius", "9999px");

        Div balkenFuell = new Div();
        balkenFuell.getStyle()
                .set("width", prozent + "%")
                .set("height", "100%")
                .set("background", "#553722")
                .set("border-radius", "9999px");

        balkenHintergrund.add(balkenFuell);
        zeile.add(kopf, balkenHintergrund);
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