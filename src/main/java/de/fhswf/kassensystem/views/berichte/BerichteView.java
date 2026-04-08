package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.dto.ArtikelStatistikDTO;
import de.fhswf.kassensystem.model.dto.TagesabschlussDTO;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.service.BerichteService;
import de.fhswf.kassensystem.views.MainLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * BerichteView – vollständig mit BerichteService verbunden.
 * Datum-Picker steuert alle drei Tabs.
 */
@Route(value = "berichte", layout = MainLayout.class)
public class BerichteView extends VerticalLayout {

    private final BerichteService berichteService;
    private final de.fhswf.kassensystem.service.PdfExportService pdfExportService;

    private final Div       tabInhalt  = new Div();
    private       LocalDate aktivDatum = LocalDate.now();
    private       String    aktiverTab = "tagesabschluss";

    private Span tagesTab;
    private Span umsatzTab;
    private Span artikelTab;

    public BerichteView(BerichteService berichteService,
                        de.fhswf.kassensystem.service.PdfExportService pdfExportService) {
        this.berichteService = berichteService;
        this.pdfExportService = pdfExportService;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box");

        tabInhalt.setWidthFull();

        add(buildHeader(), buildTabNavigation(), buildDatumZeile(), tabInhalt);
        ladeTabInhalt();
    }

    // ═══════════════════════════════════════════════════════════
    // DATEN LADEN
    // ═══════════════════════════════════════════════════════════

    private void ladeTabInhalt() {
        tabInhalt.removeAll();
        switch (aktiverTab) {
            case "tagesabschluss" -> tabInhalt.add(buildTagesabschlussInhalt());
            case "umsatz"         -> tabInhalt.add(buildUmsatzuebersichtInhalt());
            case "artikel"        -> tabInhalt.add(buildArtikelstatistikInhalt());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

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
                .set("background", "#e2e0fc").set("border-radius", "1rem")
                .set("padding", "0.75rem").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");
        Span icon = createIcon("bar_chart");
        icon.getStyle().set("color", "#553722").set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Berichte & Auswertungen");
        titel.getStyle()
                .set("margin", "0").set("font-size", "1.75rem").set("font-weight", "800")
                .set("color", "#1a1a2e").set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        titelGruppe.add(iconBox, titel);

        Button exportBtn = new Button();
        Span dlIcon = createIcon("download");
        Span dlText = new Span("Als PDF exportieren");
        dlText.getStyle().set("font-weight","600").set("font-size","0.8rem")
                .set("font-family","'Plus Jakarta Sans', sans-serif")
                .set("letter-spacing","0.05em").set("text-transform","uppercase");
        exportBtn.getElement().appendChild(dlIcon.getElement());
        exportBtn.getElement().appendChild(dlText.getElement());
        exportBtn.getStyle()
                .set("background","transparent").set("border","2px solid rgba(85,55,34,0.2)")
                .set("border-radius","9999px").set("padding","0.625rem 1.5rem")
                .set("color","#553722").set("cursor","pointer")
                .set("display","flex").set("align-items","center").set("gap","0.5rem");

        exportBtn.addClickListener(e -> exportiereAlsPdf());

        header.add(titelGruppe, exportBtn);
        return header;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB-NAVIGATION
    // ═══════════════════════════════════════════════════════════

    private HorizontalLayout buildTabNavigation() {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.setSpacing(false);
        tabs.getStyle()
                .set("gap", "3rem")
                .set("border-bottom", "1px solid rgba(130,116,109,0.15)")
                .set("margin-bottom", "2rem");

        tagesTab   = buildTab("Tagesabschluss",  true);
        umsatzTab  = buildTab("Umsatzübersicht", false);
        artikelTab = buildTab("Artikelstatistik",false);

        tagesTab.addClickListener(e -> {
            aktiverTab = "tagesabschluss";
            zeigeTab(tagesTab, new Span[]{umsatzTab, artikelTab});
            ladeTabInhalt();
        });
        umsatzTab.addClickListener(e -> {
            aktiverTab = "umsatz";
            zeigeTab(umsatzTab, new Span[]{tagesTab, artikelTab});
            ladeTabInhalt();
        });
        artikelTab.addClickListener(e -> {
            aktiverTab = "artikel";
            zeigeTab(artikelTab, new Span[]{tagesTab, umsatzTab});
            ladeTabInhalt();
        });

        tabs.add(tagesTab, umsatzTab, artikelTab);
        return tabs;
    }

    private Span buildTab(String label, boolean aktiv) {
        Span tab = new Span(label);
        tab.getStyle()
                .set("font-size","0.8rem").set("font-weight","600")
                .set("text-transform","uppercase").set("letter-spacing","0.05em")
                .set("padding-bottom","1rem").set("cursor","pointer")
                .set("font-family","'Plus Jakarta Sans', sans-serif")
                .set("color", aktiv ? "#553722" : "rgba(85,55,34,0.4)")
                .set("border-bottom", aktiv ? "3px solid #6f4e37" : "3px solid transparent")
                .set("transition","all 0.2s");
        return tab;
    }

    private void zeigeTab(Span aktiv, Span[] inaktive) {
        aktiv.getStyle().set("color","#553722").set("border-bottom","3px solid #6f4e37");
        for (Span s : inaktive)
            s.getStyle().set("color","rgba(85,55,34,0.4)").set("border-bottom","3px solid transparent");
    }

    // ═══════════════════════════════════════════════════════════
    // DATUM-ZEILE
    // ═══════════════════════════════════════════════════════════

    private HorizontalLayout buildDatumZeile() {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setPadding(false);
        zeile.getStyle().set("margin-bottom", "2rem");

        DatePicker datePicker = new DatePicker(aktivDatum);
        datePicker.setLocale(java.util.Locale.GERMAN);
        datePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                aktivDatum = e.getValue();
                ladeTabInhalt();
            }
        });

        Span echtzeit = new Span("Daten werden in Echtzeit aktualisiert");
        echtzeit.getStyle()
                .set("font-size","0.8rem").set("font-style","italic")
                .set("color","#82746d").set("font-family","'Plus Jakarta Sans', sans-serif");

        zeile.add(datePicker, echtzeit);
        return zeile;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 1: TAGESABSCHLUSS
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildTagesabschlussInhalt() {
        TagesabschlussDTO dto = berichteService.getTagesabschluss(aktivDatum);

        BigDecimal gesamtumsatz = safe(dto.getGesamtumsatz());
        BigDecimal umsatzBar    = safe(dto.getUmsatzBar());
        BigDecimal umsatzKarte  = safe(dto.getUmsatzKarte());
        int        trans        = dto.getAnzahlTransaktionen();
        BigDecimal bonWert      = trans > 0
                ? gesamtumsatz.divide(BigDecimal.valueOf(trans), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        VerticalLayout inhalt = new VerticalLayout();
        inhalt.setWidthFull();
        inhalt.setPadding(false);
        inhalt.setSpacing(false);
        inhalt.getStyle().set("gap", "2rem");

        // Metric-Karten
        HorizontalLayout karten = new HorizontalLayout();
        karten.setWidthFull();
        karten.setSpacing(false);
        karten.getStyle().set("gap", "1.5rem");
        karten.add(
                buildMetricKarte("GESAMTUMSATZ",  fp(gesamtumsatz), "payments",     "Tagesumsatz",           false),
                buildMetricKarte("TRANSAKTIONEN", String.valueOf(trans), "receipt_long", "Abgeschlossene Verkäufe", false),
                buildMetricKarte("Ø BON-WERT",    fp(bonWert),      "coffee",       "Zielwert: 22,00€",      false)
        );

        // Unterer Bereich
        HorizontalLayout bereich = new HorizontalLayout();
        bereich.setWidthFull();
        bereich.setAlignItems(FlexComponent.Alignment.START);
        bereich.setSpacing(false);
        bereich.getStyle().set("gap", "3rem").set("flex-wrap", "wrap");
        bereich.add(buildZahlungsarten(umsatzBar, umsatzKarte), buildTopSeller(dto));

        inhalt.add(karten, bereich);
        return inhalt;
    }

    private VerticalLayout buildMetricKarte(String label, String wert,
                                            String icon, String subtext, boolean positiv) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex","1").set("background","white").set("border-radius","1.25rem")
                .set("padding","2rem").set("gap","0.5rem")
                .set("position","relative").set("overflow","hidden");

        Span bgIcon = createIcon(icon);
        bgIcon.getStyle()
                .set("position","absolute").set("top","0").set("right","0")
                .set("font-size","6rem").set("color","#553722").set("opacity","0.05")
                .set("pointer-events","none").set("line-height","1");

        Span lbl = new Span(label);
        lbl.getStyle()
                .set("font-size","0.65rem").set("font-weight","800").set("text-transform","uppercase")
                .set("letter-spacing","0.1em").set("color","rgba(85,55,34,0.6)")
                .set("font-family","'Plus Jakarta Sans', sans-serif").set("position","relative");

        Span w = new Span(wert);
        w.getStyle()
                .set("font-size","3rem").set("font-weight","900").set("color","#553722")
                .set("letter-spacing","-0.025em").set("line-height","1")
                .set("font-family","'Plus Jakarta Sans', sans-serif")
                .set("margin-top","0.5rem").set("position","relative");

        Span sub = new Span(subtext);
        sub.getStyle()
                .set("font-size","0.8rem").set("font-weight","700")
                .set("color", positiv ? "#16a34a" : "#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif");

        karte.add(bgIcon, lbl, w, sub);
        return karte;
    }

    private VerticalLayout buildZahlungsarten(BigDecimal umsatzBar, BigDecimal umsatzKarte) {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
        spalte.getStyle()
                .set("flex","0 0 780px").set("width","780px")
                .set("max-width","780px").set("gap","1.5rem");

        HorizontalLayout titelZeile = new HorizontalLayout();
        titelZeile.setWidthFull();
        titelZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        titelZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titelZeile.setPadding(false);

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap","0.75rem");

        Div akzent = new Div();
        akzent.getStyle()
                .set("width","0.25rem").set("height","1.5rem").set("background","#553722")
                .set("border-radius","9999px");

        H4 titel = new H4("Zahlungsarten");
        titel.getStyle()
                .set("margin","0").set("font-size","1.1rem").set("font-weight","700")
                .set("color","#553722").set("font-family","'Plus Jakarta Sans', sans-serif");
        titelGruppe.add(akzent, titel);

        String[][] daten = {
                {"Bar",   umsatzBar.setScale(2, RoundingMode.HALF_UP).toPlainString(),   "#ffdcc6"},
                {"Karte", umsatzKarte.setScale(2, RoundingMode.HALF_UP).toPlainString(), "#553722"}
        };

        Div diagrammContainer = new Div();
        diagrammContainer.setWidthFull();
        diagrammContainer.getStyle()
                .set("background","#f5f2ff").set("border-radius","1rem").set("padding","1.5rem");
        diagrammContainer.add(buildDiagramm(daten, true));

        HorizontalLayout toggle = new HorizontalLayout();
        toggle.setSpacing(false);
        toggle.getStyle()
                .set("background","#efecff").set("border-radius","9999px")
                .set("padding","0.25rem").set("gap","0.25rem");

        Button torteBtn  = buildToggleButton("Torte",  false);
        Button balkenBtn = buildToggleButton("Balken", true);
        torteBtn.addClickListener(e -> {
            diagrammContainer.removeAll();
            diagrammContainer.add(buildDiagramm(daten, false));
            torteBtn.getStyle().set("background","#553722").set("color","white");
            balkenBtn.getStyle().set("background","transparent").set("color","#553722");
        });
        balkenBtn.addClickListener(e -> {
            diagrammContainer.removeAll();
            diagrammContainer.add(buildDiagramm(daten, true));
            balkenBtn.getStyle().set("background","#553722").set("color","white");
            torteBtn.getStyle().set("background","transparent").set("color","#553722");
        });
        toggle.add(torteBtn, balkenBtn);
        titelZeile.add(titelGruppe, toggle);

        HorizontalLayout kartenZeile = new HorizontalLayout();
        kartenZeile.setWidthFull();
        kartenZeile.setSpacing(false);
        kartenZeile.getStyle().set("gap","1rem");
        kartenZeile.add(
                buildZahlungsKarte("Bar",   fp(umsatzBar),   "wallet"),
                buildZahlungsKarte("Karte", fp(umsatzKarte), "credit_card")
        );

        spalte.add(titelZeile, kartenZeile, diagrammContainer);
        return spalte;
    }

    private VerticalLayout buildTopSeller(TagesabschlussDTO dto) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex","0 0 320px").set("max-width","320px")
                .set("background","white").set("border-radius","1.25rem")
                .set("border","2px solid #553722").set("padding","2rem").set("gap","1.5rem");

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
                .set("margin","0").set("font-size","1.1rem").set("font-weight","900")
                .set("color","#553722").set("font-family","'Plus Jakarta Sans', sans-serif");
        Paragraph sub = new Paragraph("Basierend auf der Anzahl der verkauften Artikel");
        sub.getStyle().set("margin","0").set("font-size","0.7rem").set("color","#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif");
        titelBlock.add(titel, sub);

        Div sternBox = new Div();
        sternBox.getStyle()
                .set("width","3rem").set("height","3rem").set("border-radius","9999px")
                .set("background","#f5f2ff").set("display","flex")
                .set("align-items","center").set("justify-content","center");
        sternBox.add(createIcon("star"));
        kopf.add(titelBlock, sternBox);

        VerticalLayout liste = new VerticalLayout();
        liste.setWidthFull();
        liste.setPadding(false);
        liste.setSpacing(false);
        liste.getStyle().set("gap","0.75rem");

        // Top-Seller aus Verkaufspos. berechnen
        Map<Artikel, Integer> mengen = new LinkedHashMap<>();
        List<Verkauf> verkaeufe = dto.getVerkaeufe() != null ? dto.getVerkaeufe() : List.of();
        for (Verkauf v : verkaeufe) {
            if (v.getPositionen() == null) continue;
            for (Verkaufsposition pos : v.getPositionen())
                mengen.merge(pos.getArtikel(), pos.getMenge(), Integer::sum);
        }

        mengen.entrySet().stream()
                .sorted(Map.Entry.<Artikel, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> liste.add(buildTopSellerEintrag(
                        e.getKey().getName(),
                        e.getKey().getKategorie().getName(),
                        e.getValue() + "x")));

        if (mengen.isEmpty()) {
            Span leer = new Span("Keine Verkäufe an diesem Tag.");
            leer.getStyle().set("font-size","0.8rem").set("color","#82746d")
                    .set("font-family","'Plus Jakarta Sans', sans-serif");
            liste.add(leer);
        }

        karte.add(kopf, liste);
        return karte;
    }

    private HorizontalLayout buildTopSellerEintrag(String name, String kategorie, String anzahl) {
        HorizontalLayout eintrag = new HorizontalLayout();
        eintrag.setWidthFull();
        eintrag.setAlignItems(FlexComponent.Alignment.CENTER);
        eintrag.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        eintrag.setPadding(false);
        eintrag.getStyle().set("padding","0.25rem 0");

        HorizontalLayout links = new HorizontalLayout();
        links.setAlignItems(FlexComponent.Alignment.CENTER);
        links.setSpacing(false);
        links.getStyle().set("gap","1rem");

        Div avatar = new Div();
        avatar.getStyle()
                .set("width","2.5rem").set("height","2.5rem")
                .set("border-radius","9999px").set("background","#efecff").set("flex-shrink","0");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        Span n = new Span(name);
        n.getStyle().set("font-weight","700").set("color","#553722")
                .set("font-size","0.875rem").set("font-family","'Plus Jakarta Sans', sans-serif");
        Span k = new Span(kategorie);
        k.getStyle().set("font-size","0.7rem").set("color","#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif");
        info.add(n, k);
        links.add(avatar, info);

        Span a = new Span(anzahl);
        a.getStyle().set("font-weight","900").set("color","#553722")
                .set("font-size","0.875rem").set("font-family","'Plus Jakarta Sans', sans-serif");

        eintrag.add(links, a);
        return eintrag;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 2: UMSATZÜBERSICHT
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildUmsatzuebersichtInhalt() {
        VerticalLayout inhalt = new VerticalLayout();
        inhalt.setWidthFull();
        inhalt.setPadding(false);
        inhalt.setSpacing(false);
        inhalt.getStyle().set("gap","2rem");

        HorizontalLayout kopfZeile = new HorizontalLayout();
        kopfZeile.setWidthFull();
        kopfZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        kopfZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopfZeile.setPadding(false);

        H3 umsatzTitel = new H3("Umsatzübersicht");
        umsatzTitel.getStyle()
                .set("margin","0").set("font-size","1.25rem").set("font-weight","800")
                .set("color","#553722").set("font-family","'Plus Jakarta Sans', sans-serif");

        HorizontalLayout zeitToggle = new HorizontalLayout();
        zeitToggle.setSpacing(false);
        zeitToggle.getStyle()
                .set("background","#efecff").set("border-radius","9999px")
                .set("padding","0.25rem").set("gap","0.25rem");

        Button tagBtn   = buildToggleButton("Tag",   true);
        Button wocheBtn = buildToggleButton("Woche", false);

        Div umsatzContainer = new Div();
        umsatzContainer.setWidthFull();
        umsatzContainer.add(buildTagesansicht());

        tagBtn.addClickListener(e -> {
            umsatzContainer.removeAll();
            umsatzContainer.add(buildTagesansicht());
            tagBtn.getStyle().set("background","#553722").set("color","white");
            wocheBtn.getStyle().set("background","transparent").set("color","#553722");
        });
        wocheBtn.addClickListener(e -> {
            umsatzContainer.removeAll();
            umsatzContainer.add(buildWochenansicht());
            wocheBtn.getStyle().set("background","#553722").set("color","white");
            tagBtn.getStyle().set("background","transparent").set("color","#553722");
        });

        zeitToggle.add(tagBtn, wocheBtn);
        kopfZeile.add(umsatzTitel, zeitToggle);
        inhalt.add(kopfZeile, umsatzContainer);
        return inhalt;
    }

    private VerticalLayout buildTagesansicht() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap","1.5rem");

        List<Verkauf> verkaeufe = berichteService.findByTimestampBetween(
                aktivDatum.atStartOfDay(), aktivDatum.atTime(23, 59, 59));

        // Stunden 8–18
        BigDecimal[] bar   = new BigDecimal[11];
        BigDecimal[] karte = new BigDecimal[11];
        BigDecimal   maxH  = BigDecimal.ONE;
        for (int i = 0; i < 11; i++) { bar[i] = BigDecimal.ZERO; karte[i] = BigDecimal.ZERO; }

        for (Verkauf v : verkaeufe) {
            int h = v.getTimestamp().getHour();
            if (h >= 8 && h <= 18) {
                int idx = h - 8;
                BigDecimal s = safe(v.getGesamtsumme());
                if (v.getZahlungsart() == Zahlungsart.BAR)   bar[idx]   = bar[idx].add(s);
                else                                          karte[idx] = karte[idx].add(s);
                BigDecimal tot = bar[idx].add(karte[idx]);
                if (tot.compareTo(maxH) > 0) maxH = tot;
            }
        }

        VerticalLayout stundenKarte = new VerticalLayout();
        stundenKarte.setWidthFull();
        stundenKarte.setPadding(false);
        stundenKarte.setSpacing(false);
        stundenKarte.getStyle()
                .set("background","white").set("border-radius","1.25rem")
                .set("padding","2rem").set("gap","1.25rem");

        H3 stundenTitel = new H3("Umsatz nach Stunde – " +
                aktivDatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        stundenTitel.getStyle()
                .set("margin","0").set("font-size","1.1rem").set("font-weight","700")
                .set("color","#553722").set("font-family","'Plus Jakarta Sans', sans-serif");

        HorizontalLayout stundenDiag = new HorizontalLayout();
        stundenDiag.setWidthFull();
        stundenDiag.setAlignItems(FlexComponent.Alignment.END);
        stundenDiag.setSpacing(false);
        stundenDiag.getStyle()
                .set("height","14rem").set("gap","0.5rem").set("padding","0 0.5rem");

        String[] labels = {"8:00-8:59","9:00-9:59","10:00-10:59","11:00-11:59","12:00-12:59","13:00-13:59","14:00-14:59","15:00-15:59","16:00-16:59","17:00-17:59","18:00-18:59"};
        for (int i = 0; i < 11; i++) {
            int bp = pct(bar[i], maxH);
            int kp = pct(karte[i], maxH);
            stundenDiag.add(buildBalken(labels[i], Math.max(bp,2)+"%", Math.max(kp,2)+"%"));
        }
        stundenKarte.add(stundenTitel, stundenDiag, buildLegende());
        layout.add(stundenKarte);

        // Produktliste
        VerticalLayout produktKarte = new VerticalLayout();
        produktKarte.setWidthFull();
        produktKarte.setPadding(false);
        produktKarte.setSpacing(false);
        produktKarte.getStyle()
                .set("background","white").set("border-radius","1.25rem")
                .set("padding","2rem").set("gap","1.25rem");

        H3 pTitel = new H3("Verkaufte Produkte – " +
                aktivDatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        pTitel.getStyle()
                .set("margin","0").set("font-size","1.1rem").set("font-weight","700")
                .set("color","#553722").set("font-family","'Plus Jakarta Sans', sans-serif");

        VerticalLayout produktListe = new VerticalLayout();
        produktListe.setWidthFull();
        produktListe.setPadding(false);
        produktListe.setSpacing(false);
        produktListe.getStyle().set("gap","0.75rem");

        Map<Artikel, int[]> stats = new LinkedHashMap<>();
        for (Verkauf v : verkaeufe) {
            if (v.getPositionen() == null) continue;
            for (Verkaufsposition pos : v.getPositionen()) {
                stats.computeIfAbsent(pos.getArtikel(), k -> new int[]{0, 0});
                stats.get(pos.getArtikel())[0] += pos.getMenge();
                stats.get(pos.getArtikel())[1] +=
                        safe(pos.getEinzelpreis()).multiply(BigDecimal.valueOf(pos.getMenge()))
                                .multiply(BigDecimal.valueOf(100)).intValue();
            }
        }

        stats.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .forEach(e -> {
                    BigDecimal u = BigDecimal.valueOf(e.getValue()[1])
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    produktListe.add(buildProduktZeile(
                            e.getKey().getName(),
                            e.getKey().getKategorie().getName(),
                            e.getValue()[0] + "x", fp(u)));
                });

        if (stats.isEmpty()) {
            produktListe.add(leerSpan("Keine Verkäufe an diesem Tag."));
        }

        produktKarte.add(pTitel, produktListe);
        layout.add(produktKarte);
        return layout;
    }

    private VerticalLayout buildWochenansicht() {
        LocalDate wochenStart = aktivDatum.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap","1.5rem");

        VerticalLayout karte = new VerticalLayout();
        karte.setWidthFull();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("background","white").set("border-radius","1.25rem")
                .set("padding","2rem").set("gap","1.25rem");

        H3 wTitel = new H3("Umsatz nach Zahlungsart – KW " +
                aktivDatum.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + " (Mo–Sa)");
        wTitel.getStyle()
                .set("margin","0").set("font-size","1.1rem").set("font-weight","700")
                .set("color","#553722").set("font-family","'Plus Jakarta Sans', sans-serif");

        String[] tagLabels = {"MO","DI","MI","DO","FR","SA"};
        BigDecimal[] barW    = new BigDecimal[6];
        BigDecimal[] karteW  = new BigDecimal[6];
        BigDecimal   maxTag  = BigDecimal.ONE;
        BigDecimal   wUmsatz = BigDecimal.ZERO;
        int          wTrans  = 0;

        for (int i = 0; i < 6; i++) {
            barW[i] = BigDecimal.ZERO;
            karteW[i] = BigDecimal.ZERO;
            LocalDate tag = wochenStart.plusDays(i);
            List<Verkauf> tv = berichteService.findByTimestampBetween(
                    tag.atStartOfDay(), tag.atTime(23,59,59));
            wTrans += tv.size();
            for (Verkauf v : tv) {
                BigDecimal s = safe(v.getGesamtsumme());
                wUmsatz = wUmsatz.add(s);
                if (v.getZahlungsart() == Zahlungsart.BAR) barW[i] = barW[i].add(s);
                else karteW[i] = karteW[i].add(s);
            }
            BigDecimal tot = barW[i].add(karteW[i]);
            if (tot.compareTo(maxTag) > 0) maxTag = tot;
        }

        HorizontalLayout wDiag = new HorizontalLayout();
        wDiag.setWidthFull();
        wDiag.setAlignItems(FlexComponent.Alignment.END);
        wDiag.setSpacing(false);
        wDiag.getStyle().set("height","16rem").set("gap","1rem").set("padding","0 1rem");

        BigDecimal starkWert = BigDecimal.ZERO;
        String     starkTag  = "-";
        for (int i = 0; i < 6; i++) {
            int bp = Math.max(pct(barW[i], maxTag), 2);
            int kp = Math.max(pct(karteW[i], maxTag), 2);
            wDiag.add(buildBalken(tagLabels[i], bp+"%", kp+"%"));
            BigDecimal tagesSum = barW[i].add(karteW[i]);
            if (tagesSum.compareTo(starkWert) > 0) { starkWert = tagesSum; starkTag = tagLabels[i]; }
        }

        HorizontalLayout summary = new HorizontalLayout();
        summary.setWidthFull();
        summary.setSpacing(false);
        summary.getStyle().set("gap","1rem");
        summary.add(
                buildSummaryKarte("Wochenumsatz",  fp(wUmsatz),            "payments"),
                buildSummaryKarte("Transaktionen", String.valueOf(wTrans),  "receipt_long"),
                buildSummaryKarte("Stärkster Tag", starkTag,                "star")
        );

        karte.add(wTitel, wDiag, buildLegende(), summary);
        layout.add(karte);
        return layout;
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 3: ARTIKELSTATISTIK
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildArtikelstatistikInhalt() {
        VerticalLayout inhalt = new VerticalLayout();
        inhalt.setWidthFull();
        inhalt.setPadding(false);
        inhalt.setSpacing(false);
        inhalt.getStyle().set("gap","2rem");

        VerticalLayout karte = new VerticalLayout();
        karte.setWidthFull();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("background","white").set("border-radius","1.25rem")
                .set("padding","2rem").set("gap","1.5rem");

        H3 titel = new H3("Artikelstatistik – Verkaufsranking (letzte 30 Tage)");
        titel.getStyle()
                .set("margin","0").set("font-size","1.1rem").set("font-weight","700")
                .set("color","#553722").set("font-family","'Plus Jakarta Sans', sans-serif");

        VerticalLayout liste = new VerticalLayout();
        liste.setWidthFull();
        liste.setPadding(false);
        liste.setSpacing(false);
        liste.getStyle().set("gap","1rem");

        List<ArtikelStatistikDTO> statistik = berichteService.getArtikelStatistik(30);
        int maxAnzahl = statistik.isEmpty() ? 1 : statistik.get(0).getAnzahlVerkauft();

        for (ArtikelStatistikDTO dto : statistik) {
            liste.add(buildStatistikZeile(
                    dto.getArtikel().getName(),
                    dto.getArtikel().getKategorie().getName(),
                    dto.getAnzahlVerkauft() + "x",
                    dto.getAnzahlVerkauft(), maxAnzahl));
        }

        if (statistik.isEmpty()) liste.add(leerSpan("Keine Verkaufsdaten der letzten 30 Tage."));

        karte.add(titel, liste);
        inhalt.add(karte);
        return inhalt;
    }

    private VerticalLayout buildStatistikZeile(String name, String kat,
                                               String anzahl, int anzahlInt, int maxAnzahl) {
        VerticalLayout zeile = new VerticalLayout();
        zeile.setWidthFull();
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle().set("gap","0.4rem");

        HorizontalLayout kopf = new HorizontalLayout();
        kopf.setWidthFull();
        kopf.setAlignItems(FlexComponent.Alignment.CENTER);
        kopf.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopf.setPadding(false);

        HorizontalLayout links = new HorizontalLayout();
        links.setAlignItems(FlexComponent.Alignment.CENTER);
        links.setSpacing(false);
        links.getStyle().set("gap","0.75rem");

        Div av = new Div();
        av.getStyle()
                .set("width","2.25rem").set("height","2.25rem")
                .set("border-radius","9999px").set("background","#efecff").set("flex-shrink","0");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        Span n = new Span(name);
        n.getStyle().set("font-weight","700").set("font-size","0.875rem").set("color","#553722")
                .set("font-family","'Plus Jakarta Sans', sans-serif");
        Span k = new Span(kat);
        k.getStyle().set("font-size","0.7rem").set("color","#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif");
        info.add(n, k);
        links.add(av, info);

        Span a = new Span(anzahl);
        a.getStyle().set("font-weight","900").set("font-size","0.875rem").set("color","#553722")
                .set("font-family","'Plus Jakarta Sans', sans-serif");

        kopf.add(links, a);

        int pct = maxAnzahl == 0 ? 0 : (int)((anzahlInt / (double) maxAnzahl) * 100);
        Div bg = new Div();
        bg.getStyle()
                .set("width","100%").set("height","0.3rem").set("background","#efecff")
                .set("border-radius","9999px");
        Div fill = new Div();
        fill.getStyle()
                .set("width", pct + "%").set("height","100%").set("background","#553722")
                .set("border-radius","9999px");
        bg.add(fill);

        zeile.add(kopf, bg);
        return zeile;
    }

    // ═══════════════════════════════════════════════════════════
    // GEMEINSAME UI-HELFER
    // ═══════════════════════════════════════════════════════════

    private Button buildToggleButton(String label, boolean aktiv) {
        Button btn = new Button(label);
        btn.getStyle()
                .set("background", aktiv ? "#553722" : "transparent")
                .set("color", aktiv ? "white" : "#553722")
                .set("border","none").set("border-radius","9999px")
                .set("padding","0.25rem 0.75rem").set("font-size","0.65rem")
                .set("font-weight","700").set("cursor","pointer")
                .set("font-family","'Plus Jakarta Sans', sans-serif").set("transition","all 0.2s");
        return btn;
    }

    private Div buildDiagramm(String[][] daten, boolean balken) {
        Div container = new Div();
        container.getStyle()
                .set("width","100%").set("display","flex").set("gap","1.5rem")
                .set("align-items","center");

        Div legende = new Div();
        legende.getStyle()
                .set("display","flex").set("flex-direction","column").set("gap","0.75rem")
                .set("flex-shrink","0").set("min-width","120px");

        double summe = 0;
        for (String[] x : daten) summe += Double.parseDouble(x[1]);
        if (summe == 0) summe = 1;

        for (String[] d : daten) {
            double wert = Double.parseDouble(d[1]);
            int p = (int) Math.round(wert / summe * 100);
            Div eintrag = new Div();
            eintrag.getStyle().set("display","flex").set("flex-direction","column").set("gap","0.15rem");
            Div lz = new Div();
            lz.getStyle().set("display","flex").set("align-items","center").set("gap","0.5rem");
            Div punkt = new Div();
            punkt.getStyle()
                    .set("width","10px").set("height","10px").set("border-radius","50%")
                    .set("background",d[2]).set("flex-shrink","0");
            Span nm = new Span(d[0]);
            nm.getStyle().set("font-size","0.8rem").set("font-weight","700").set("color","#553722")
                    .set("font-family","'Plus Jakarta Sans', sans-serif");
            lz.add(punkt, nm);
            Span inf = new Span(String.format("%.0f€ · %d%%", wert, p));
            inf.getStyle().set("font-size","0.7rem").set("color","#82746d")
                    .set("font-family","'Plus Jakarta Sans', sans-serif").set("padding-left","1.25rem");
            eintrag.add(lz, inf);
            legende.add(eintrag);
        }

        Div cw = new Div();
        cw.getStyle().set("flex","1").set("display","flex").set("justify-content","center");
        String cid = "chart-" + System.nanoTime();
        cw.getElement().setProperty("innerHTML",
                "<canvas id='" + cid + "' width='200' height='200' style='display:block;'></canvas>");
        String js = balken ? buildBalkenJs(cid, daten) : buildTorteJs(cid, daten);
        cw.getElement().executeJs("setTimeout(function() { " + js + " }, 100);");

        container.add(legende, cw);
        return container;
    }

    private String buildBalkenJs(String cid, String[][] daten) {
        StringBuilder js = new StringBuilder();
        js.append("var c=document.getElementById('").append(cid).append("');if(!c)return;");
        js.append("var ctx=c.getContext('2d');var w=c.width,h=c.height;ctx.clearRect(0,0,w,h);");
        js.append("var labels=["); for(String[]d:daten)js.append("'").append(d[0]).append("',"); js.append("];");
        js.append("var werte=[");  for(String[]d:daten)js.append(d[1]).append(",");              js.append("];");
        js.append("var farben=["); for(String[]d:daten)js.append("'").append(d[2]).append("',"); js.append("];");
        js.append("var maxW=Math.max.apply(null,werte)||1;var n=werte.length;");
        js.append("var padT=20,padB=25,padL=10,padR=10;");
        js.append("var chartH=h-padT-padB;var chartW=w-padL-padR;var slot=chartW/n;var bw=slot*0.5;");
        js.append("for(var i=0;i<n;i++){");
        js.append("var bh=(werte[i]/maxW)*chartH;var x=padL+slot*i+(slot-bw)/2;var y=padT+chartH-bh;");
        js.append("ctx.fillStyle=farben[i];var r=6;ctx.beginPath();");
        js.append("ctx.moveTo(x+r,y);ctx.lineTo(x+bw-r,y);ctx.quadraticCurveTo(x+bw,y,x+bw,y+r);");
        js.append("ctx.lineTo(x+bw,y+bh);ctx.lineTo(x,y+bh);ctx.lineTo(x,y+r);");
        js.append("ctx.quadraticCurveTo(x,y,x+r,y);ctx.closePath();ctx.fill();");
        js.append("ctx.fillStyle='#553722';ctx.font='bold 10px sans-serif';ctx.textAlign='center';");
        js.append("ctx.fillText(Math.round(werte[i])+'€',x+bw/2,y-5);");
        js.append("ctx.fillStyle='#82746d';ctx.font='9px sans-serif';");
        js.append("ctx.fillText(labels[i].toUpperCase(),x+bw/2,h-8);}");
        return js.toString();
    }

    private String buildTorteJs(String cid, String[][] daten) {
        StringBuilder js = new StringBuilder();
        js.append("var c=document.getElementById('").append(cid).append("');if(!c)return;");
        js.append("var ctx=c.getContext('2d');var w=c.width,h=c.height;ctx.clearRect(0,0,w,h);");
        js.append("var werte=[");  for(String[]d:daten)js.append(d[1]).append(","); js.append("];");
        js.append("var farben=["); for(String[]d:daten)js.append("'").append(d[2]).append("',"); js.append("];");
        js.append("var s=werte.reduce(function(a,b){return a+b;},0)||1;");
        js.append("var cx=w/2,cy=h/2,r=Math.min(w,h)/2-10,start=-Math.PI/2;");
        js.append("for(var i=0;i<werte.length;i++){");
        js.append("var sl=(werte[i]/s)*2*Math.PI;ctx.fillStyle=farben[i];ctx.beginPath();");
        js.append("ctx.moveTo(cx,cy);ctx.arc(cx,cy,r,start,start+sl);ctx.closePath();ctx.fill();");
        js.append("ctx.strokeStyle='white';ctx.lineWidth=2;ctx.stroke();start+=sl;}");
        return js.toString();
    }

    private HorizontalLayout buildZahlungsKarte(String label, String betrag, String icon) {
        HorizontalLayout karte = new HorizontalLayout();
        karte.setAlignItems(FlexComponent.Alignment.CENTER);
        karte.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex","1").set("background","#efecff")
                .set("border-radius","1rem").set("padding","1.5rem");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        Span l = new Span(label);
        l.getStyle().set("font-size","0.65rem").set("font-weight","800")
                .set("text-transform","uppercase").set("letter-spacing","0.05em")
                .set("color","rgba(85,55,34,0.6)").set("font-family","'Plus Jakarta Sans', sans-serif");
        Span b = new Span(betrag);
        b.getStyle().set("font-size","1.5rem").set("font-weight","900").set("color","#553722")
                .set("letter-spacing","-0.025em").set("font-family","'Plus Jakarta Sans', sans-serif");
        info.add(l, b);

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("width","3rem").set("height","3rem").set("border-radius","9999px")
                .set("background","white").set("display","flex").set("align-items","center")
                .set("justify-content","center").set("box-shadow","0 2px 8px rgba(0,0,0,0.08)");
        Span ic = createIcon(icon);
        ic.getStyle().set("color","#553722").set("font-variation-settings","'FILL' 1");
        iconBox.add(ic);

        karte.add(info, iconBox);
        return karte;
    }

    private HorizontalLayout buildProduktZeile(String name, String kat, String menge, String umsatz) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setPadding(false);
        zeile.getStyle().set("padding","0.5rem 0").set("border-bottom","1px solid #f5f2ff");

        HorizontalLayout links = new HorizontalLayout();
        links.setAlignItems(FlexComponent.Alignment.CENTER);
        links.setSpacing(false);
        links.getStyle().set("gap","0.75rem");

        Div av = new Div();
        av.getStyle().set("width","2rem").set("height","2rem")
                .set("border-radius","9999px").set("background","#efecff").set("flex-shrink","0");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        Span n = new Span(name);
        n.getStyle().set("font-weight","700").set("font-size","0.875rem").set("color","#553722")
                .set("font-family","'Plus Jakarta Sans', sans-serif");
        Span k = new Span(kat);
        k.getStyle().set("font-size","0.7rem").set("color","#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif");
        info.add(n, k);
        links.add(av, info);

        HorizontalLayout rechts = new HorizontalLayout();
        rechts.setAlignItems(FlexComponent.Alignment.CENTER);
        rechts.setSpacing(false);
        rechts.getStyle().set("gap","2rem");

        Span m = new Span(menge);
        m.getStyle().set("font-size","0.8rem").set("font-weight","700").set("color","#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif").set("min-width","3rem").set("text-align","right");
        Span u = new Span(umsatz);
        u.getStyle().set("font-size","0.875rem").set("font-weight","900").set("color","#553722")
                .set("font-family","'Plus Jakarta Sans', sans-serif").set("min-width","5rem").set("text-align","right");
        rechts.add(m, u);

        zeile.add(links, rechts);
        return zeile;
    }

    private VerticalLayout buildSummaryKarte(String label, String wert, String icon) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex","1").set("background","#f5f2ff")
                .set("border-radius","1rem").set("padding","1rem 1.25rem").set("gap","0.25rem");
        Span l = new Span(label);
        l.getStyle().set("font-size","0.6rem").set("font-weight","800")
                .set("text-transform","uppercase").set("letter-spacing","0.1em")
                .set("color","#82746d").set("font-family","'Plus Jakarta Sans', sans-serif");
        HorizontalLayout wz = new HorizontalLayout();
        wz.setAlignItems(FlexComponent.Alignment.CENTER);
        wz.setSpacing(false);
        wz.getStyle().set("gap","0.5rem");
        Span ic = createIcon(icon);
        ic.getStyle().set("font-size","1rem").set("color","#553722");
        Span w = new Span(wert);
        w.getStyle().set("font-size","1.1rem").set("font-weight","900").set("color","#553722")
                .set("letter-spacing","-0.025em").set("font-family","'Plus Jakarta Sans', sans-serif");
        wz.add(ic, w);
        karte.add(l, wz);
        return karte;
    }

    private HorizontalLayout buildLegende() {
        HorizontalLayout legende = new HorizontalLayout();
        legende.setSpacing(false);
        legende.getStyle().set("gap","2rem").set("justify-content","center");
        for (String[] e : new String[][]{{"#ffdcc6","Barzahlung"},{"#553722","Kartenzahlung"}}) {
            HorizontalLayout item = new HorizontalLayout();
            item.setAlignItems(FlexComponent.Alignment.CENTER);
            item.setSpacing(false);
            item.getStyle().set("gap","0.5rem");
            Div p = new Div();
            p.getStyle().set("width","0.75rem").set("height","0.75rem")
                    .set("border-radius","9999px").set("background",e[0]).set("flex-shrink","0");
            Span t = new Span(e[1]);
            t.getStyle().set("font-size","0.75rem").set("font-weight","600").set("color","#553722")
                    .set("font-family","'Plus Jakarta Sans', sans-serif");
            item.add(p, t);
            legende.add(item);
        }
        return legende;
    }

    private VerticalLayout buildBalken(String label, String barH, String karteH) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.getStyle()
                .set("flex","1").set("gap","0.35rem").set("height","100%").set("justify-content","flex-end");

        HorizontalLayout paar = new HorizontalLayout();
        paar.setAlignItems(FlexComponent.Alignment.END);
        paar.setSpacing(false);
        paar.getStyle()
                .set("gap","0.15rem").set("width","100%").set("height","calc(100% - 1.25rem)");

        Div bar = new Div();
        bar.getStyle().set("flex","1").set("height",barH)
                .set("background","#ffdcc6").set("border-radius","0.3rem 0.3rem 0 0");
        Div karte = new Div();
        karte.getStyle().set("flex","1").set("height",karteH)
                .set("background","#553722").set("border-radius","0.3rem 0.3rem 0 0");
        paar.add(bar, karte);

        Span lbl = new Span(label);
        lbl.getStyle().set("font-size","0.5rem").set("font-weight","700").set("color","#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif")
                .set("text-align","center")
                .set("white-space","nowrap");
        wrapper.add(paar, lbl);
        return wrapper;
    }

    // ═══════════════════════════════════════════════════════════
    // KLEINE HILFSMETHODEN
    // ═══════════════════════════════════════════════════════════

    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }

    /** Null-sicherer BigDecimal */
    private BigDecimal safe(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    /** Formatiert BigDecimal als deutschen Preis (z.B. "47,50€") */
    private String fp(BigDecimal v) {
        if (v == null) return "0,00€";
        return String.format("%,.2f€", v).replace(",","X").replace(".",",").replace("X",".");
    }

    /** Berechnet Prozent (0-100) eines Werts relativ zu max */
    private int pct(BigDecimal val, BigDecimal max) {
        if (max.compareTo(BigDecimal.ZERO) == 0) return 0;
        return val.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
    }

    private Span leerSpan(String text) {
        Span s = new Span(text);
        s.getStyle().set("font-size","0.8rem").set("color","#82746d")
                .set("font-family","'Plus Jakarta Sans', sans-serif");
        return s;
    }
    // ═══════════════════════════════════════════════════════════
    // PDF EXPORT
    // ═══════════════════════════════════════════════════════════

    /**
     * Generiert einen PDF-Bericht und startet den Download im Browser.
     */
    private void exportiereAlsPdf() {
        try {
            de.fhswf.kassensystem.model.dto.TagesabschlussDTO tagesabschluss =
                    berichteService.getTagesabschluss(aktivDatum);
            java.util.List<de.fhswf.kassensystem.model.dto.ArtikelStatistikDTO> statistik =
                    berichteService.getArtikelStatistik(30);

            byte[] pdfBytes = pdfExportService.exportiereTagebericht(tagesabschluss, statistik);

            String dateiname = "Tagesbericht_" +
                    aktivDatum.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    ".pdf";

            // Download via Base64 + JavaScript (kein deprecated API, Vaadin 25)
            String base64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                    "const bytes = atob($0);" +
                            "const arr = new Uint8Array(bytes.length);" +
                            "for(let i=0;i<bytes.length;i++) arr[i]=bytes.charCodeAt(i);" +
                            "const blob = new Blob([arr],{type:'application/pdf'});" +
                            "const url = URL.createObjectURL(blob);" +
                            "const a = document.createElement('a');" +
                            "a.href = url;" +
                            "a.download = $1;" +
                            "document.body.appendChild(a);" +
                            "a.click();" +
                            "document.body.removeChild(a);" +
                            "URL.revokeObjectURL(url);",
                    base64, dateiname
            );

            com.vaadin.flow.component.notification.Notification.show(
                    "PDF wird heruntergeladen...", 3000,
                    com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);

        } catch (Exception ex) {
            com.vaadin.flow.component.notification.Notification.show(
                    "Fehler beim PDF-Export: " + ex.getMessage(), 4000,
                    com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        }
    }


}