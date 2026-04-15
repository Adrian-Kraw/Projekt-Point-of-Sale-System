package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.service.LagerService;
import de.fhswf.kassensystem.views.MainLayout;
import de.fhswf.kassensystem.views.components.AbstractTabellenView;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lager-Übersicht mit Bestandstabelle, Statistik-Karten und Nachbestellhinweisen.
 * FIX: Ampel-Symbole stehen jetzt unter der Kartenüberschrift (nicht rechts neben dem Wert).
 */
@Route(value = "lager", layout = MainLayout.class)
public class LagerView extends AbstractTabellenView {

    private final LagerService   lagerService;
    private final ArtikelService artikelService;

    private final HorizontalLayout statistikKartenLayout = new HorizontalLayout();
    private final VerticalLayout   nachbestellBlock      = new VerticalLayout();
    private final VerticalLayout   lieferungBlock        = new VerticalLayout();
    private final VerticalLayout   tabellenZeilen        = new VerticalLayout();

    public LagerView(LagerService lagerService, ArtikelService artikelService) {
        super(Rolle.KASSIERER);
        this.lagerService   = lagerService;
        this.artikelService = artikelService;

        statistikKartenLayout.setWidthFull();
        statistikKartenLayout.setSpacing(false);
        statistikKartenLayout.getStyle().set("gap", "1.5rem").set("margin-bottom", "2rem");
        statistikKartenLayout.getElement().setAttribute("tour-id", "statistik-karten");

        nachbestellBlock.setWidthFull();
        nachbestellBlock.setPadding(false);
        nachbestellBlock.setSpacing(false);

        lieferungBlock.setWidthFull();
        lieferungBlock.setPadding(false);
        lieferungBlock.setSpacing(false);

        tabellenZeilen.setWidthFull();
        tabellenZeilen.setPadding(false);
        tabellenZeilen.setSpacing(false);
        tabellenZeilen.getStyle().set("gap", "0");

        add(buildHeader(), statistikKartenLayout, new Div(nachbestellBlock), new Div(lieferungBlock), buildBestandsTabelle());
        ladeAlles();
    }

    // ═══════════════════════════════════════════════════════════
    // DATEN LADEN
    // ═══════════════════════════════════════════════════════════

    private void ladeAlles() {
        ladeStatistikKarten();
        ladeNachbestellHinweise();
        ladeLieferungshinweise();
        ladeDaten();
    }

    private void ladeStatistikKarten() {
        statistikKartenLayout.removeAll();
        int warnAnzahl   = lagerService.getMinimalbestandWarnliste().size();
        int gesamtAnzahl = artikelService.findAllArtikel().size();
        statistikKartenLayout.add(buildKritischKarte(warnAnzahl), buildGesamtKarte(gesamtAnzahl));
        if (istManager()) statistikKartenLayout.add(buildAktionenKarte());
    }

    private void ladeNachbestellHinweise() {
        nachbestellBlock.removeAll();
        List<Artikel> warnliste = lagerService.getMinimalbestandWarnliste();
        if (warnliste.isEmpty()) return;

        HorizontalLayout warnHeader = new HorizontalLayout();
        warnHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        warnHeader.setSpacing(false);
        warnHeader.getStyle().set("background", "#ffdad6").set("padding", "1rem 1.5rem").set("gap", "0.75rem");
        Span wi = createIcon("notification_important");
        wi.getStyle().set("color", "#ba1a1a");
        Span wt = new Span("Nachbestellhinweise");
        wt.getStyle().set("font-size", "0.875rem").set("font-weight", "700").set("color", "#ba1a1a")
                .set("text-transform", "uppercase").set("letter-spacing", "0.05em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        warnHeader.add(wi, wt);

        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.setSpacing(false);
        grid.getStyle().set("padding", "1.5rem").set("gap", "1rem").set("flex-wrap", "wrap");
        for (Artikel a : warnliste) {
            grid.add(NachbestellKarteFactory.create(a, istManager(), this::oeffneWareneingangFuerArtikel));
        }

        nachbestellBlock.getStyle()
                .set("background", "#fff5f2").set("border-radius", "1.25rem")
                .set("overflow", "hidden").set("margin-bottom", "2rem");
        nachbestellBlock.add(warnHeader, grid);
    }

    private void ladeLieferungshinweise() {
        lieferungBlock.removeAll();

        List<Wareneingang> offeneLieferungen = lagerService.getAusstehendeLieferungen();

        if (offeneLieferungen.isEmpty()) return;

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.getStyle().set("background", "#d4edda").set("padding", "1rem 1.5rem").set("gap", "0.75rem");

        Span icon = createIcon("local_shipping");
        icon.getStyle().set("color", "#155724");
        Span titel = new Span("Lieferungsbescheid");
        titel.getStyle().set("font-size", "0.875rem").set("font-weight", "700").set("color", "#155724")
                .set("text-transform", "uppercase").set("letter-spacing", "0.05em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        header.add(icon, titel);

        // Karten-Grid
        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.setSpacing(false);
        grid.getStyle().set("padding", "1.5rem").set("gap", "1rem").set("flex-wrap", "wrap");

        for (Wareneingang w : offeneLieferungen) {
            String bestelltAm = w.getBestelltAm()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String bestelltVon = w.getBestelltVon() != null
                    ? w.getBestelltVon().getBenutzername() : "Unbekannt";

            // Lieferdatum: nächster Tag 06:00
            String liefertAm = w.getBestelltAm()
                    .toLocalDate()
                    .plusDays(1)
                    .atTime(6, 0)
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'"));

            grid.add(buildLieferungsKarte(w.getId(), w.getArtikel().getName(),
                    w.getMenge(), bestelltAm, bestelltVon, liefertAm));
        }

        lieferungBlock.getStyle()
                .set("background", "#f0fff4").set("border-radius", "1.25rem")
                .set("overflow", "hidden").set("margin-bottom", "2rem");
        lieferungBlock.add(header, grid);
    }

    // TODO: Parameter anpassen sobald echtes Model vorhanden –
    //       z.B. buildLieferungsKarte(OffeneLieferung lieferung)
    private HorizontalLayout buildLieferungsKarte(Long lieferungId, String artikelName, int menge,
                                                  String bestelltAm, String bestelltVon, String liefertAm) {
        HorizontalLayout karte = new HorizontalLayout();
        karte.setAlignItems(FlexComponent.Alignment.CENTER);
        karte.setSpacing(false);
        karte.getStyle()
                .set("background", "rgba(255,255,255,0.7)").set("border-radius", "0.75rem")
                .set("padding", "1rem 1.25rem").set("gap", "1rem")
                .set("flex", "1").set("min-width", "260px");

        // Info links
        Span name = new Span(artikelName);
        name.getStyle().set("font-weight", "700").set("font-size", "0.875rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span mengeBadge = new Span("+" + menge + " Stk.");
        mengeBadge.getStyle()
                .set("background", "#155724").set("color", "white").set("border-radius", "9999px")
                .set("padding", "0.15rem 0.6rem").set("font-size", "0.75rem").set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span meta = new Span("Bestellt am " + bestelltAm + " von @" + bestelltVon
                + "  ·  Lieferung erwartet: " + liefertAm);
        meta.getStyle().set("font-size", "0.75rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout badgeRow = new HorizontalLayout();
        badgeRow.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeRow.setSpacing(false);
        badgeRow.getStyle().set("gap", "0.5rem");
        badgeRow.add(mengeBadge, meta);

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("flex", "1").set("gap", "0.3rem");
        info.add(name, badgeRow);

        // Buttons rechts
        HorizontalLayout btnRow = new HorizontalLayout();
        btnRow.setAlignItems(FlexComponent.Alignment.CENTER);
        btnRow.setSpacing(false);
        btnRow.getStyle().set("gap", "0.5rem").set("flex-shrink", "0");

        Button bestaetigenBtn = new Button("✓  Lieferung bestätigen");
        bestaetigenBtn.getStyle()
                .set("background", "#155724").set("color", "white").set("border", "none")
                .set("border-radius", "0.75rem").set("padding", "0.6rem 1.1rem")
                .set("font-weight", "700").set("font-size", "0.8rem").set("cursor", "pointer")
                .set("white-space", "nowrap").set("font-family", "'Plus Jakarta Sans', sans-serif");

        bestaetigenBtn.addClickListener(e -> {
            lagerService.lieferungBestaetigen(lieferungId);
            ladeAlles();
        });

        Button ablehnBtn = new Button("✕");
        ablehnBtn.getStyle()
                .set("background", "none").set("color", "#ba1a1a")
                .set("border", "1.5px solid #ba1a1a").set("border-radius", "0.75rem")
                .set("padding", "0.6rem 0.85rem").set("font-weight", "700")
                .set("font-size", "0.8rem").set("cursor", "pointer")
                .set("white-space", "nowrap").set("font-family", "'Plus Jakarta Sans', sans-serif");

        ablehnBtn.addClickListener(e -> {
            lagerService.lieferungStornieren(lieferungId);
            ladeAlles();
        });

        btnRow.add(bestaetigenBtn, ablehnBtn);

        karte.add(info, btnRow);
        return karte;
    }

    @Override
    public void ladeDaten() {
        ladeDaten(null);
    }

    private void ladeDaten(String suchBegriff) {
        tabellenZeilen.removeAll();
        tabellenZeilen.add(buildTabellenHeader());
        List<Artikel> artikel = (suchBegriff != null && !suchBegriff.isBlank())
                ? artikelService.findByName(suchBegriff)
                : artikelService.findAllArtikel();
        artikel = artikel.stream()
                .sorted(java.util.Comparator
                        .comparing((Artikel a) -> a.getKategorie().getName())
                        .thenComparing(Artikel::getName))
                .collect(java.util.stream.Collectors.toList());
        boolean zebra = false;
        for (Artikel a : artikel) { tabellenZeilen.add(LagerZeileFactory.create(a, zebra)); zebra = !zebra; }
    }

    private void oeffneWareneingangFuerArtikel(Artikel artikel) {
        new WareneingangDialog(artikelService.findAllArtikel(), artikel, lagerService, this::ladeAlles).open();
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    @Override
    protected HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(false);
        header.getStyle().set("margin-bottom", "2rem");

        Div iconBox = new Div();
        iconBox.getStyle().set("background", "#e2e0fc").set("border-radius", "1rem").set("padding", "0.75rem")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        Span icon = createIcon("inventory_2");
        icon.getStyle().set("color", "#553722").set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Lagerverwaltung");
        titel.getStyle().set("margin", "0").set("font-size", "1.75rem").set("font-weight", "800")
                .set("color", "#1a1a2e").set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "1rem");
        titelGruppe.add(iconBox, titel);
        header.add(titelGruppe);
        return header;
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIK-KARTEN
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildKritischKarte(int anzahl) {
        VerticalLayout karte = buildStatistikKarte("Artikel unter Minimalbestand", String.valueOf(anzahl),
                "warning", "#ba1a1a", "#ffdad6", "Nachbestellen empfohlen");
        karte.getElement().setAttribute("tour-id", "artikel-minimum");
        return karte;
    }

    private VerticalLayout buildGesamtKarte(int anzahl) {
        VerticalLayout karte = buildStatistikKarte("Gesamtartikel", String.valueOf(anzahl),
                "inventory", "#553722", "#ffdcc6", "Aktive Artikel im System");
        karte.getElement().setAttribute("tour-id", "gesamtartikel");
        return karte;
    }

    /**
     * FIX: Icon-Box steht jetzt DIREKT unter dem Label (Überschrift),
     * nicht mehr rechts neben dem Zahlenwert.
     */
    private VerticalLayout buildStatistikKarte(String label, String wert, String iconName,
                                               String farbe, String iconBg, String hinweis) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle().set("flex", "1").set("background", "white").set("border-radius", "1.25rem")
                .set("padding", "2rem").set("gap", "0.5rem");

        Span lbl = new Span(label.toUpperCase());
        lbl.getStyle().set("font-size", "0.65rem").set("font-weight", "800").set("letter-spacing", "0.1em")
                .set("color", "#82746d").set("font-family", "'Plus Jakarta Sans', sans-serif");

        // FIX: Icon-Box direkt unter dem Label
        Div iconBox = new Div();
        iconBox.getStyle().set("width", "3rem").set("height", "3rem").set("border-radius", "1rem")
                .set("background", iconBg).set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("margin-bottom", "0.25rem");
        Span ic = createIcon(iconName);
        ic.getStyle().set("font-size", "1.5rem").set("color", farbe);
        iconBox.add(ic);

        Span w = new Span(wert);
        w.getStyle().set("font-size", "3rem").set("font-weight", "900").set("color", farbe)
                .set("letter-spacing", "-0.025em").set("line-height", "1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph p = new Paragraph(hinweis);
        p.getStyle().set("margin", "0").set("font-size", "0.8rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        karte.add(lbl, iconBox, w, p);
        return karte;
    }

    private VerticalLayout buildAktionenKarte() {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle().set("flex", "1").set("background", "linear-gradient(135deg, #553722, #6f4e37)")
                .set("border-radius", "1.25rem").set("padding", "2rem").set("gap", "0.75rem");
        karte.getElement().setAttribute("tour-id", "lager-aktionen-karte");

        H3 titel = new H3("Lager-Aktionen");
        titel.getStyle().set("margin", "0").set("font-size", "1.1rem").set("font-weight", "700")
                .set("color", "white").set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph text = new Paragraph("Verwalten Sie Ihre Bestände effizient und buchen Sie Eingänge.");
        text.getStyle().set("margin", "0").set("font-size", "0.8rem").set("color", "rgba(255,220,198,0.8)")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span plusIcon = createIcon("add_circle");
        plusIcon.getStyle().set("font-size", "1.1rem");
        Span btnText = new Span("Bestandseingang buchen");
        btnText.getStyle().set("font-weight", "700").set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button btn = new Button();
        btn.setWidthFull();
        btn.getElement().appendChild(plusIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getElement().setAttribute("tour-id", "bestandseingang-btn");
        btn.getStyle()
                .set("background", "#ffdcc6").set("color", "#553722").set("border", "none")
                .set("border-radius", "9999px").set("padding", "1rem 1.5rem").set("cursor", "pointer")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                .set("gap", "0.5rem").set("margin-top", "0.5rem");
        btn.addClickListener(e ->
                new WareneingangDialog(artikelService.findAllArtikel(), lagerService, this::ladeAlles).open());

        karte.add(titel, text, btn);
        return karte;
    }

    /** Tour-Aktionen für den TourManager. */
    public void tourAktion(String action) {
        switch (action) {
            case "open-wareneingang-dialog" ->
                    new WareneingangDialog(artikelService.findAllArtikel(), lagerService, this::ladeAlles).open();
            default -> {}
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BESTANDSTABELLE
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildBestandsTabelle() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.getStyle().set("background", "white").set("border-radius", "1.25rem").set("overflow", "hidden");
        container.getElement().setAttribute("tour-id", "bestand-tabelle");
        container.add(buildTabellenKopf(), tabellenZeilen);
        return container;
    }

    private HorizontalLayout buildTabellenKopf() {
        H3 titel = new H3("Bestandsübersicht");
        titel.getStyle().set("margin", "0").set("font-size", "1.1rem").set("font-weight", "700")
                .set("color", "#553722").set("font-family", "'Plus Jakarta Sans', sans-serif");

        TextField suchfeld = new TextField();
        suchfeld.setPlaceholder("Artikel suchen...");
        suchfeld.setPrefixComponent(createIcon("search"));
        suchfeld.getStyle().set("width", "16rem");
        suchfeld.addValueChangeListener(e -> ladeDaten(e.getValue()));

        HorizontalLayout kopf = new HorizontalLayout();
        kopf.setWidthFull();
        kopf.setAlignItems(FlexComponent.Alignment.CENTER);
        kopf.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopf.setPadding(false);
        kopf.getStyle().set("padding", "1.5rem 2rem").set("border-bottom", "1px solid #f5f2ff");
        kopf.add(titel, suchfeld);
        kopf.getElement().setAttribute("tour-id", "bestand-tabelle-kopf");
        return kopf;
    }

    private HorizontalLayout buildTabellenHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("background", "#f5f2ff").set("padding", "0.75rem 2rem").set("gap", "0");
        header.add(
                headerZelle("Artikel",       LagerZeileFactory.BREITE_ARTIKEL),
                headerZelle("Kategorie",     LagerZeileFactory.BREITE_KATEGORIE),
                headerZelle("Bestand",       LagerZeileFactory.BREITE_BESTAND),
                headerZelle("Minimalgrenze", LagerZeileFactory.BREITE_MINIMAL),
                buildStatusHeaderZelle(),
                headerZelle("",              LagerZeileFactory.BREITE_AKTION)
        );
        return header;
    }
    private Span buildStatusHeaderZelle() {
        Span zelle = headerZelle("Status", LagerZeileFactory.BREITE_STATUS);
        zelle.getElement().setAttribute("tour-id", "status-spalte");
        return zelle;
    }

}