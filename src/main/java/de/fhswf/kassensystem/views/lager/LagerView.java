package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import de.fhswf.kassensystem.broadcast.Broadcaster;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.service.LagerService;
import de.fhswf.kassensystem.views.MainLayout;
import de.fhswf.kassensystem.views.components.AbstractTabellenView;
import de.fhswf.kassensystem.views.components.FehlerUI;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Lagerverwaltungs-View mit Bestandstabelle, Statistikkarten,
 * Nachbestellhinweisen und Lieferungsbestätigungen.
 *
 * <p>Aufbau:
 * <ul>
 *   <li>Statistikkarten: Artikel unter Minimalbestand, Gesamtartikel,
 *       Lager-Aktionen-Karte (nur Manager)</li>
 *   <li>Nachbestellhinweise: alle Artikel unter Minimalbestand</li>
 *   <li>Lieferungsbescheid: alle ausstehenden Wareneingänge</li>
 *   <li>Bestandstabelle: alle Artikel mit Ampelstatus</li>
 * </ul>
 *
 * <p>Zugriff: Rollen {@code KASSIERER} und {@code MANAGER}.
 *
 * @author Adrian Krawietz
 */
@Route(value = "lager", layout = MainLayout.class)
public class LagerView extends AbstractTabellenView {

    private final LagerService   lagerService;
    private final ArtikelService artikelService;

    private final HorizontalLayout statistikKartenLayout = new HorizontalLayout();
    private final VerticalLayout   nachbestellBlock      = new VerticalLayout();
    private final VerticalLayout   lieferungBlock        = new VerticalLayout();
    private final VerticalLayout   tabellenZeilen        = new VerticalLayout();

    private Registration broadcasterRegistration;

    /**
     * Erstellt die View und lädt alle Daten.
     *
     * @param lagerService   Service für Bestand, Nachbestellungen und Lieferungen
     * @param artikelService Service für Artikeldaten
     */
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
        nachbestellBlock.getElement().setAttribute("tour-id", "nachbestell-block");

        lieferungBlock.setWidthFull();
        lieferungBlock.setPadding(false);
        lieferungBlock.setSpacing(false);
        lieferungBlock.getElement().setAttribute("tour-id", "lieferung-block");

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

    /**
     * Lädt alle Sektionen der View neu (Statistiken, Nachbestellungen, Lieferungen, Tabelle).
     */
    void ladeAlles() {
        ladeStatistikKarten();
        ladeNachbestellHinweise();
        ladeLieferungshinweise();
        ladeDaten();
    }

    /**
     * Lädt und rendert die Statistikkarten neu.
     * Manager erhalten zusätzlich die Lager-Aktionen-Karte.
     */
    private void ladeStatistikKarten() {
        statistikKartenLayout.removeAll();
        try {
            int warnAnzahl   = lagerService.getMinimalbestandWarnliste().size();
            int gesamtAnzahl = artikelService.findAllArtikel().size();
            statistikKartenLayout.add(buildKritischKarte(warnAnzahl), buildGesamtKarte(gesamtAnzahl));
            if (istManager()) statistikKartenLayout.add(buildAktionenKarte());
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Lädt die Artikel unter Minimalbestand und rendert die Nachbestellhinweis-Karten.
     * Zeigt nichts wenn alle Bestände ausreichend sind.
     */
    private void ladeNachbestellHinweise() {
        nachbestellBlock.removeAll();
        try {
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
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Lädt alle ausstehenden Lieferungen und rendert die Lieferungskarten.
     * Zeigt nichts wenn keine offenen Lieferungen vorhanden sind.
     */
    private void ladeLieferungshinweise() {
        lieferungBlock.removeAll();
        try {
            List<Wareneingang> offeneLieferungen = lagerService.getAusstehendeLieferungen();
            if (offeneLieferungen.isEmpty()) return;

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

            HorizontalLayout grid = new HorizontalLayout();
            grid.setWidthFull();
            grid.setSpacing(false);
            grid.getStyle().set("padding", "1.5rem").set("gap", "1rem").set("flex-wrap", "wrap");

            for (Wareneingang w : offeneLieferungen) {
                String bestelltAm  = w.getBestelltAm().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                String bestelltVon = w.getBestelltVon() != null ? w.getBestelltVon().getBenutzername() : "Unbekannt";
                String liefertAm   = w.getBestelltAm().toLocalDate().plusDays(1)
                        .atTime(6, 0).format(DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'"));
                grid.add(buildLieferungsKarte(w.getId(), w.getArtikel().getName(),
                        w.getMenge(), bestelltAm, bestelltVon, liefertAm));
            }

            lieferungBlock.getStyle()
                    .set("background", "#f0fff4").set("border-radius", "1.25rem")
                    .set("overflow", "hidden").set("margin-bottom", "2rem");
            lieferungBlock.add(header, grid);
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Erstellt eine einzelne Lieferungskarte mit Artikelname, Menge, Bestelldaten und Aktions-Buttons.
     *
     * @param lieferungId  ID der Lieferung für Bestätigen/Stornieren ({@code null} im Demo-Modus)
     * @param artikelName  Name des bestellten Artikels
     * @param menge        bestellte Menge
     * @param bestelltAm   formatiertes Bestelldatum
     * @param bestelltVon  Benutzername des Bestellers
     * @param liefertAm    erwartetes Lieferdatum als Text
     */
    private HorizontalLayout buildLieferungsKarte(Long lieferungId, String artikelName, int menge,
                                                  String bestelltAm, String bestelltVon, String liefertAm) {
        HorizontalLayout karte = new HorizontalLayout();
        karte.setSpacing(false);
        karte.getStyle()
                .set("background", "rgba(255,255,255,0.7)").set("border-radius", "0.75rem")
                .set("padding", "1rem 1.25rem").set("gap", "1rem")
                .set("flex", "0 1 calc(33.333% - 0.75rem)").set("min-width", "260px")
                .set("flex-wrap", "wrap")
                .set("align-items", "flex-start");

        Span name = new Span(artikelName);
        name.getStyle().set("font-weight", "700").set("font-size", "0.875rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span mengeBadge = new Span("+" + menge + " Stk.");
        mengeBadge.getStyle()
                .set("background", "#155724").set("color", "white").set("border-radius", "9999px")
                .set("padding", "0.15rem 0.6rem").set("font-size", "0.75rem").set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span metaBestellung = new Span("Bestellt am " + bestelltAm + " von @" + bestelltVon);
        metaBestellung.getStyle().set("font-size", "0.75rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span metaLieferung = new Span("Lieferung erwartet: " + liefertAm);
        metaLieferung.getStyle().set("font-size", "0.75rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        VerticalLayout metaBlock = new VerticalLayout();
        metaBlock.setPadding(false);
        metaBlock.setSpacing(false);
        metaBlock.getStyle().set("gap", "0.2rem");
        metaBlock.add(mengeBadge, metaBestellung, metaLieferung);

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("flex", "1").set("gap", "0.3rem");
        info.add(name, metaBlock);

        Button bestaetigenBtn = new Button("✓  Lieferung bestätigen");
        bestaetigenBtn.getStyle()
                .set("background", "#155724").set("color", "white").set("border", "none")
                .set("border-radius", "0.75rem").set("padding", "0.6rem 1.1rem")
                .set("font-weight", "700").set("font-size", "0.8rem").set("cursor", "pointer")
                .set("white-space", "nowrap").set("font-family", "'Plus Jakarta Sans', sans-serif");
        bestaetigenBtn.addClickListener(e -> onLieferungBestaetigen(lieferungId));

        Button ablehnBtn = new Button("✕");
        ablehnBtn.getStyle()
                .set("background", "none").set("color", "#ba1a1a")
                .set("border", "1.5px solid #ba1a1a").set("border-radius", "0.75rem")
                .set("padding", "0.6rem 0.85rem").set("font-weight", "700")
                .set("font-size", "0.8rem").set("cursor", "pointer")
                .set("white-space", "nowrap").set("font-family", "'Plus Jakarta Sans', sans-serif");
        ablehnBtn.addClickListener(e -> onLieferungStornieren(lieferungId));

        HorizontalLayout btnRow = new HorizontalLayout();
        btnRow.setAlignItems(FlexComponent.Alignment.CENTER);
        btnRow.setSpacing(false);
        btnRow.getStyle().set("gap", "0.5rem").set("flex-shrink", "0").set("flex-wrap", "wrap");
        btnRow.add(bestaetigenBtn, ablehnBtn);

        karte.add(info, btnRow);
        return karte;
    }

    /**
     * Lädt die Bestandstabelle ohne Suchfilter.
     */
    @Override
    public void ladeDaten() {
        ladeDaten(null);
    }

    /**
     * Lädt die Bestandstabelle, optional gefiltert nach einem Suchbegriff.
     *
     * @param suchBegriff Suchtext oder {@code null} für alle Artikel
     */
    private void ladeDaten(String suchBegriff) {
        tabellenZeilen.removeAll();
        tabellenZeilen.add(buildTabellenHeader());
        try {
            List<Artikel> artikel = (suchBegriff != null && !suchBegriff.isBlank())
                    ? artikelService.findByName(suchBegriff)
                    : artikelService.findAllArtikel();
            artikel = artikel.stream()
                    .sorted(Comparator.comparing((Artikel a) -> a.getKategorie().getName())
                            .thenComparing(Artikel::getName))
                    .toList();
            boolean zebra = false;
            for (Artikel a : artikel) {
                tabellenZeilen.add(LagerZeileFactory.create(a, zebra));
                zebra = !zebra;
            }
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Öffnet den {@link WareneingangDialog} für einen bestimmten Artikel (aus Nachbestellhinweis).
     *
     * @param artikel der vorausgewählte Artikel
     */
    private void oeffneWareneingangFuerArtikel(Artikel artikel) {
        try {
            new WareneingangDialog(artikelService.findAllArtikel(), artikel, lagerService, this::ladeAlles).open();
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Öffnet den {@link WareneingangDialog} ohne vorausgewählten Artikel.
     */
    private void oeffneWareneingangDialog() {
        try {
            new WareneingangDialog(artikelService.findAllArtikel(), lagerService, this::ladeAlles).open();
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LIEFERUNGS-AKTIONEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Bestätigt eine Lieferung und aktualisiert die View.
     *
     * @param lieferungId ID der zu bestätigenden Lieferung, {@code null} im Demo-Modus
     */
    private void onLieferungBestaetigen(Long lieferungId) {
        if (lieferungId == null) return;
        try {
            lagerService.lieferungBestaetigen(lieferungId);
            FehlerUI.erfolg("Lieferung bestätigt.");
            Broadcaster.broadcast("lager-geaendert");
            ladeAlles();
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Storniert eine Lieferung und aktualisiert die View.
     *
     * @param lieferungId ID der zu stornierenden Lieferung, {@code null} im Demo-Modus
     */
    private void onLieferungStornieren(Long lieferungId) {
        if (lieferungId == null) return;
        try {
            lagerService.lieferungStornieren(lieferungId);
            FehlerUI.erfolg("Lieferung storniert.");
            Broadcaster.broadcast("lager-geaendert");
            ladeAlles();
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt den Seitenkopf mit Inventory-Icon und Titel "Lagerverwaltung".
     */
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

    /**
     * Erstellt die Warn-Statistikkarte für Artikel unter Minimalbestand.
     *
     * @param anzahl Anzahl der Artikel unter Minimalbestand
     */
    private VerticalLayout buildKritischKarte(int anzahl) {
        VerticalLayout karte = buildStatistikKarte("Artikel unter Minimalbestand", String.valueOf(anzahl),
                "warning", "#ba1a1a", "#ffdad6", "Nachbestellen empfohlen");
        karte.getElement().setAttribute("tour-id", "artikel-minimum");
        return karte;
    }

    /**
     * Erstellt die Statistikkarte für die Gesamtartikelanzahl.
     *
     * @param anzahl Gesamtanzahl der Artikel im System
     */
    private VerticalLayout buildGesamtKarte(int anzahl) {
        VerticalLayout karte = buildStatistikKarte("Gesamtartikel", String.valueOf(anzahl),
                "inventory", "#553722", "#ffdcc6", "Aktive Artikel im System");
        karte.getElement().setAttribute("tour-id", "gesamtartikel");
        return karte;
    }

    /**
     * Erstellt eine allgemeine Statistikkarte mit Label, Icon-Box, Zahlenwert und Hinweistext.
     *
     * @param label    Kartenüberschrift
     * @param wert     Hauptzahlenwert
     * @param iconName Material-Symbols-Icon-Name
     * @param farbe    Farbe für Wert und Icon
     * @param iconBg   Hintergrundfarbe der Icon-Box
     * @param hinweis  Hinweistext unterhalb des Werts
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

    /**
     * Erstellt die Lager-Aktionen-Karte (nur für Manager) mit dem "Bestandseingang buchen"-Button.
     */
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
        btn.addClickListener(e -> oeffneWareneingangDialog());

        karte.add(titel, text, btn);
        return karte;
    }

    // ═══════════════════════════════════════════════════════════
    // TOUR-AKTIONEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet Tour-Aktionen aus dem {@link de.fhswf.kassensystem.tour.TourManager}.
     * Unterstützt: {@code "open-wareneingang-dialog"}, {@code "demo-nachbestell"}, {@code "demo-lieferung"}.
     *
     * @param action Aktions-String aus dem Tour-Step
     */
    public void tourAktion(String action) {
        switch (action) {
            case "open-wareneingang-dialog" -> oeffneWareneingangDialog();
            case "demo-nachbestell"         -> zeigeDemoNachbestellung();
            case "demo-lieferung"           -> zeigeDemoLieferung();
            default                         -> {}
        }
    }

    /**
     * Zeigt eine Demo-Nachbestellkarte für die Onboarding-Tour.
     * Nur aktiv wenn der Nachbestellblock aktuell leer ist.
     */
    private void zeigeDemoNachbestellung() {
        if (nachbestellBlock.getChildren().findAny().isPresent()) return;

        HorizontalLayout warnHeader = new HorizontalLayout();
        warnHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        warnHeader.setSpacing(false);
        warnHeader.getStyle().set("background", "#ffdad6").set("padding", "1rem 1.5rem").set("gap", "0.75rem");
        Span wi = createIcon("notification_important");
        wi.getStyle().set("color", "#ba1a1a");
        Span wt = new Span("Nachbestellhinweise (Demo)");
        wt.getStyle().set("font-size", "0.875rem").set("font-weight", "700").set("color", "#ba1a1a")
                .set("text-transform", "uppercase").set("letter-spacing", "0.05em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        warnHeader.add(wi, wt);

        HorizontalLayout demoKarte = new HorizontalLayout();
        demoKarte.setAlignItems(FlexComponent.Alignment.CENTER);
        demoKarte.setSpacing(false);
        demoKarte.getStyle()
                .set("background", "rgba(255,255,255,0.6)").set("border-radius", "0.75rem")
                .set("padding", "1rem 1.25rem").set("gap", "1rem").set("flex", "1");

        Span demoName = new Span("Beispielartikel");
        demoName.getStyle().set("font-weight", "700").set("font-size", "0.875rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span demoBadge = new Span("2 Stk.");
        demoBadge.getStyle().set("background", "#ba1a1a").set("color", "white").set("border-radius", "9999px")
                .set("padding", "0.15rem 0.6rem").set("font-size", "0.75rem").set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span demoMeta = new Span("von 5 Stk. (Min)");
        demoMeta.getStyle().set("font-size", "0.8rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout demoRow = new HorizontalLayout();
        demoRow.setSpacing(false);
        demoRow.getStyle().set("gap", "0.4rem");
        demoRow.setAlignItems(FlexComponent.Alignment.CENTER);
        demoRow.add(demoBadge, demoMeta);

        VerticalLayout demoInfo = new VerticalLayout();
        demoInfo.setPadding(false);
        demoInfo.setSpacing(false);
        demoInfo.getStyle().set("flex", "1");
        demoInfo.add(demoName, demoRow);
        demoKarte.add(demoInfo);

        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.setSpacing(false);
        grid.getStyle().set("padding", "1.5rem").set("gap", "1rem").set("flex-wrap", "wrap");
        grid.add(demoKarte);

        nachbestellBlock.getStyle()
                .set("background", "#fff5f2").set("border-radius", "1.25rem")
                .set("overflow", "hidden").set("margin-bottom", "2rem");
        nachbestellBlock.add(warnHeader, grid);
    }

    /**
     * Zeigt eine Demo-Lieferungskarte für die Onboarding-Tour.
     * Nur aktiv wenn der Lieferungsblock aktuell leer ist.
     */
    private void zeigeDemoLieferung() {
        if (lieferungBlock.getChildren().findAny().isPresent()) return;

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.getStyle().set("background", "#d4edda").set("padding", "1rem 1.5rem").set("gap", "0.75rem");
        Span icon = createIcon("local_shipping");
        icon.getStyle().set("color", "#155724");
        Span titel = new Span("Lieferungsbescheid (Demo)");
        titel.getStyle().set("font-size", "0.875rem").set("font-weight", "700").set("color", "#155724")
                .set("text-transform", "uppercase").set("letter-spacing", "0.05em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        header.add(icon, titel);

        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.setSpacing(false);
        grid.getStyle().set("padding", "1.5rem").set("gap", "1rem").set("flex-wrap", "wrap");
        grid.add(buildLieferungsKarte(null, "Beispielartikel", 20,
                "15.04.2026 08:00", "@manager", "16.04.2026 um 06:00 Uhr"));

        lieferungBlock.getStyle()
                .set("background", "#f0fff4").set("border-radius", "1.25rem")
                .set("overflow", "hidden").set("margin-bottom", "2rem");
        lieferungBlock.add(header, grid);
    }

    // ═══════════════════════════════════════════════════════════
    // BESTANDSTABELLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt den äußeren Container der Bestandstabelle mit Kopfzeile und Zeilen-Bereich.
     */
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

    /**
     * Erstellt den Tabellenkopf mit Titel "Bestandsübersicht" und Suchfeld.
     */
    private HorizontalLayout buildTabellenKopf() {
        H3 titel = new H3("Bestandsübersicht");
        titel.getStyle().set("margin", "0").set("font-size", "1.1rem").set("font-weight", "700")
                .set("color", "#553722").set("font-family", "'Plus Jakarta Sans', sans-serif");

        TextField suchfeld = new TextField();
        suchfeld.setPlaceholder("Artikel suchen...");
        suchfeld.setPrefixComponent(createIcon("search"));
        suchfeld.setValueChangeMode(ValueChangeMode.EAGER);
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

    /**
     * Erstellt die Spaltenüberschriftenzeile der Bestandstabelle.
     */
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
                buildStatusHeaderZelle()
        );
        return header;
    }

    /**
     * Erstellt die Status-Spaltenüberschrift und setzt die Tour-ID für den Onboarding-Guide.
     */
    private Span buildStatusHeaderZelle() {
        Span zelle = headerZelle("Status", LagerZeileFactory.BREITE_STATUS);
        zelle.getElement().setAttribute("tour-id", "status-spalte");
        return zelle;
    }

    // ═══════════════════════════════════════════════════════════
    // BROADCASTER
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert den Broadcaster-Listener wenn die View geöffnet wird.
     * Reagiert auf "bestand-geaendert" und "lager-geaendert" Events
     * und lädt alle Sektionen neu damit der Manager stets den aktuellen Stand sieht.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        broadcasterRegistration = Broadcaster.register(event -> {
            if ("bestand-geaendert".equals(event) || "lager-geaendert".equals(event)) {
                attachEvent.getUI().access(() -> {
                    try {
                        ladeAlles();
                    } catch (Exception ex) {
                        FehlerUI.technischerFehler(ex);
                    }
                });
            }
        });
    }

    /**
     * Entfernt den Broadcaster-Listener wenn die View geschlossen/verlassen wird.
     * Verhindert Memory Leaks durch verwaiste Listener.
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }
}