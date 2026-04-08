package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.enums.Rolle;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.service.LagerService;
import de.fhswf.kassensystem.views.MainLayout;
import de.fhswf.kassensystem.views.SecuredView;

import java.time.LocalDate;
import java.util.List;

/**
 * LagerView zeigt die Bestandsübersicht des Cafés.
 *
 * Aufbau:
 * - Header: Titel + Untertitel
 * - Statistik-Karten: Kritische Artikel, Gesamtartikel, Lager-Aktionen
 * - Nachbestellhinweise: Artikel unter Minimalbestand (aus DB)
 * - Bestandstabelle: alle Artikel mit Status-Ampel (aus DB)
 *
 * Daten kommen aus LagerService und ArtikelService.
 */
@Route(value = "lager", layout = MainLayout.class)
public class LagerView extends SecuredView {

    /*
     * Spaltenbreiten der Bestandstabelle – zentral definiert.
     */
    private static final String BREITE_ARTIKEL   = "30%";
    private static final String BREITE_KATEGORIE = "20%";
    private static final String BREITE_BESTAND   = "15%";
    private static final String BREITE_MINIMAL   = "15%";
    private static final String BREITE_STATUS    = "10%";
    private static final String BREITE_AKTION    = "10%";

    private final LagerService   lagerService;
    private final ArtikelService artikelService;

    /*
     * Dynamische Bereiche als Instanzfelder damit ladeAlles()
     * sie neu befüllen kann.
     */
    private final HorizontalLayout statistikKartenLayout   = new HorizontalLayout();
    private final VerticalLayout   nachbestellBlock        = new VerticalLayout();
    private final VerticalLayout   tabellenZeilen          = new VerticalLayout();

    public LagerView(LagerService lagerService, ArtikelService artikelService) {
        super(Rolle.KASSIERER);
        this.lagerService   = lagerService;
        this.artikelService = artikelService;

        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box");

        statistikKartenLayout.setWidthFull();
        statistikKartenLayout.setSpacing(false);
        statistikKartenLayout.getStyle()
                .set("gap", "1.5rem")
                .set("margin-bottom", "2rem");

        nachbestellBlock.setWidthFull();
        nachbestellBlock.setPadding(false);
        nachbestellBlock.setSpacing(false);

        tabellenZeilen.setWidthFull();
        tabellenZeilen.setPadding(false);
        tabellenZeilen.setSpacing(false);
        tabellenZeilen.getStyle().set("gap", "0");

        add(
                buildHeader(),
                statistikKartenLayout,
                buildNachbestellWrapper(),
                buildBestandsTabelle()
        );

        ladeAlles();
    }

    // ═══════════════════════════════════════════════════════════
    // DATEN LADEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Lädt alle dynamischen Bereiche neu aus der Datenbank.
     */
    private void ladeAlles() {
        ladeStatistikKarten();
        ladeNachbestellHinweise();
        ladeBestandsZeilen(null);
    }

    /**
     * Befüllt die Statistik-Karten mit echten Zahlen.
     */
    private void ladeStatistikKarten() {
        statistikKartenLayout.removeAll();

        List<Artikel> warnliste = lagerService.getMinimalbestandWarnliste();
        List<Artikel> alle      = artikelService.findAllArtikel();

        statistikKartenLayout.add(
                buildKritischKarte(warnliste.size()),
                buildGesamtKarte(alle.size())
        );
        // Lager-Aktionen nur für Manager (Lastenheft §3.5)
        if (istManager()) {
            statistikKartenLayout.add(buildAktionenKarte());
        }
    }

    /**
     * Befüllt den Nachbestellblock mit Artikeln unter Minimalbestand.
     */
    private void ladeNachbestellHinweise() {
        nachbestellBlock.removeAll();

        List<Artikel> warnliste = lagerService.getMinimalbestandWarnliste();
        if (warnliste.isEmpty()) return;

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

        HorizontalLayout kartenGrid = new HorizontalLayout();
        kartenGrid.setWidthFull();
        kartenGrid.setSpacing(false);
        kartenGrid.getStyle()
                .set("padding", "1.5rem")
                .set("gap", "1rem")
                .set("flex-wrap", "wrap");

        for (Artikel a : warnliste) {
            kartenGrid.add(buildNachbestellKarte(a));
        }

        nachbestellBlock.getStyle()
                .set("background", "#fff5f2")
                .set("border-radius", "1.25rem")
                .set("overflow", "hidden")
                .set("margin-bottom", "2rem");

        nachbestellBlock.add(warnHeader, kartenGrid);
    }

    /**
     * Befüllt die Bestandstabelle mit allen Artikeln.
     * Filtert bei suchBegriff != null nach Name oder Kategorie.
     *
     * @param suchBegriff Suchbegriff oder null für alle
     */
    private void ladeBestandsZeilen(String suchBegriff) {
        tabellenZeilen.removeAll();
        tabellenZeilen.add(buildTabellenHeader());

        List<Artikel> artikel;
        if (suchBegriff != null && !suchBegriff.isBlank()) {
            artikel = artikelService.findByName(suchBegriff);
        } else {
            artikel = artikelService.findAllArtikel();
        }

        boolean zebra = false;
        for (Artikel a : artikel) {
            String status;
            int bestand   = a.getBestand();
            int minimal   = a.getMinimalbestand();
            // Rot:  Bestand unter Minimalbestand
            // Gelb: Bestand noch innerhalb von 20% über Minimalbestand (Warnschwelle)
            // Grün: genug Bestand
            if (bestand < minimal) {
                status = "kritisch";
            } else if (bestand < minimal * 1.2) {
                status = "warn";
            } else {
                status = "ok";
            }

            tabellenZeilen.add(buildLagerZeile(
                    a.getName(),
                    a.getKategorie().getName(),
                    a.getBestand() + " Stk.",
                    a.getMinimalbestand() + " Stk.",
                    status,
                    zebra
            ));
            zebra = !zebra;
        }
    }


    /**
     * Prüft ob der eingeloggte User Manager ist.
     * Nur Manager dürfen Wareneingänge buchen (Lastenheft §3.5).
     */
    protected boolean istManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);
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
     * Karte: Artikel unter Minimalbestand (rot hervorgehoben).
     *
     * @param anzahl Anzahl kritischer Artikel aus DB
     */
    private VerticalLayout buildKritischKarte(int anzahl) {
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

        Span wert = new Span(String.valueOf(anzahl));
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
     *
     * @param anzahl Gesamtanzahl Artikel aus DB
     */
    private VerticalLayout buildGesamtKarte(int anzahl) {
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

        Span wert = new Span(String.valueOf(anzahl));
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

        Paragraph hinweis = new Paragraph("Aktive Artikel im System");
        hinweis.getStyle()
                .set("margin", "0")
                .set("font-size", "0.8rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        karte.add(label, wertZeile, hinweis);
        return karte;
    }

    /**
     * Karte: Lager-Aktionen mit Bestandseingang Dialog.
     * Öffnet einen Dialog zum Buchen eines Wareneingangs.
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

        /*
         * Bestandseingang-Dialog öffnen.
         * Nach Schließen alles neu laden.
         */
        eingangBtn.addClickListener(e -> {
            Dialog dialog = buildWareneingangDialog();
            dialog.addOpenedChangeListener(ev -> {
                if (!ev.isOpened()) ladeAlles();
            });
            dialog.open();
        });

        karte.add(aktionenTitel, aktionenText, eingangBtn);
        return karte;
    }

    // ═══════════════════════════════════════════════════════════
    // WARENEINGANG DIALOG
    // ═══════════════════════════════════════════════════════════

    /**
     * Dialog zum Buchen eines Wareneingangs.
     * Felder: Artikel (Dropdown), Menge, Lieferant (optional), Kommentar (optional).
     */
    private Dialog buildWareneingangDialog() {
        return buildWareneingangDialog(null);
    }

    private Dialog buildWareneingangDialog(Artikel vorausgewaehlt) {
        Dialog dialog = new Dialog();
        dialog.setWidth("32rem");
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "0");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "1.25rem 1.5rem");

        Span titel = new Span("Wareneingang buchen");
        titel.getStyle()
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button closeBtn = new Button();
        closeBtn.getElement().appendChild(createIcon("close").getElement());
        closeBtn.getStyle()
                .set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("padding", "0.4rem")
                .set("border-radius", "9999px").set("min-width", "unset")
                .set("color", "#553722");
        closeBtn.addClickListener(e -> dialog.close());
        header.add(titel, closeBtn);

        // Body
        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle()
                .set("padding", "1.5rem")
                .set("gap", "1.25rem")
                .set("background", "white");

        com.vaadin.flow.component.select.Select<Artikel> artikelSelect =
                new com.vaadin.flow.component.select.Select<>();
        artikelSelect.setWidthFull();
        List<Artikel> alleArtikel = artikelService.findAllArtikel();
        artikelSelect.setItems(alleArtikel);
        artikelSelect.setItemLabelGenerator(Artikel::getName);
        artikelSelect.addClassName("dialog-feld");
        // Vorausgewählten Artikel setzen wenn aus Nachbestellhinweis geöffnet.
        // Wichtig: per ID aus der neu geladenen Liste suchen, da setValue()
        // per Objektreferenz vergleicht und sonst scheitert.
        if (vorausgewaehlt != null) {
            final Long vorId = vorausgewaehlt.getId();
            alleArtikel.stream()
                    .filter(a -> vorId.equals(a.getId()))
                    .findFirst()
                    .ifPresent(artikelSelect::setValue);
        }

        IntegerField mengeFeld = new IntegerField();
        mengeFeld.setWidthFull();
        mengeFeld.setPlaceholder("Menge");
        mengeFeld.setMin(1);
        mengeFeld.addClassName("dialog-feld");

        TextField lieferantFeld = new TextField();
        lieferantFeld.setWidthFull();
        lieferantFeld.setPlaceholder("z.B. Bäckerei Meier (optional)");
        lieferantFeld.addClassName("dialog-feld");

        TextField kommentarFeld = new TextField();
        kommentarFeld.setWidthFull();
        kommentarFeld.setPlaceholder("Kommentar (optional)");
        kommentarFeld.addClassName("dialog-feld");

        body.add(
                buildDialogFeld("ARTIKEL",    artikelSelect),
                buildDialogFeld("MENGE",      mengeFeld),
                buildDialogFeld("LIEFERANT",  lieferantFeld),
                buildDialogFeld("KOMMENTAR",  kommentarFeld)
        );

        // Footer
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setSpacing(false);
        footer.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "1.25rem 1.5rem")
                .set("gap", "1rem");

        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("background", "transparent")
                .set("border", "2px solid rgba(85,55,34,0.2)")
                .set("border-radius", "1rem").set("padding", "0.75rem 2rem")
                .set("font-weight", "700").set("color", "#553722")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("flex", "1");
        abbrechenBtn.addClickListener(e -> dialog.close());

        Button buchenBtn = new Button("Buchen");
        buchenBtn.getStyle()
                .set("background", "#553722").set("color", "white")
                .set("border", "none").set("border-radius", "1rem")
                .set("padding", "0.75rem 2rem").set("font-weight", "700")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("box-shadow", "0 4px 15px rgba(85,55,34,0.25)")
                .set("flex", "1");

        buchenBtn.addClickListener(e -> {
            if (artikelSelect.isEmpty() || mengeFeld.isEmpty()) {
                Notification.show("Bitte Artikel und Menge angeben.", 3000,
                        Notification.Position.MIDDLE);
                return;
            }

            Wareneingang eingang = new Wareneingang();
            eingang.setArtikel(artikelSelect.getValue());
            eingang.setMenge(mengeFeld.getValue());
            eingang.setDatum(LocalDate.now());
            if (!lieferantFeld.isEmpty()) {
                eingang.setLieferant(lieferantFeld.getValue());
            }
            if (!kommentarFeld.isEmpty()) {
                eingang.setKommentar(kommentarFeld.getValue());
            }

            lagerService.wareneingangBuchen(eingang);

            Notification.show("Wareneingang für \"" +
                            artikelSelect.getValue().getName() + "\" gebucht.",
                    3000, Notification.Position.BOTTOM_START);
            dialog.close();
        });

        footer.add(abbrechenBtn, buchenBtn);
        layout.add(header, body, footer);
        dialog.add(layout);

        dialog.getElement().executeJs(
                "setTimeout(() => {" +
                        "  const o = this.$.overlay;" +
                        "  if (o) { o.style.padding='0'; o.style.borderRadius='1rem'; o.style.overflow='hidden'; }" +
                        "  const c = this.$.overlay.$.content;" +
                        "  if (c) { c.style.padding='0'; c.style.borderRadius='1rem'; c.style.overflow='hidden'; }" +
                        "}, 50);"
        );

        return dialog;
    }

    /**
     * Label + Feld als Block für den Wareneingang-Dialog.
     */
    private VerticalLayout buildDialogFeld(String label,
                                           com.vaadin.flow.component.Component feld) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "0.4rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.6rem").set("font-weight", "800")
                .set("text-transform", "uppercase").set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        block.add(labelSpan, feld);
        return block;
    }

    // ═══════════════════════════════════════════════════════════
    // NACHBESTELLHINWEISE WRAPPER
    // ═══════════════════════════════════════════════════════════

    /**
     * Wrapper-Container für den dynamischen Nachbestellblock.
     */
    private Div buildNachbestellWrapper() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.add(nachbestellBlock);
        return wrapper;
    }

    /**
     * Einzelne Nachbestellkarte für einen Artikel unter Minimalbestand.
     *
     * @param artikel Artikel-Entity aus DB
     */
    private HorizontalLayout buildNachbestellKarte(Artikel artikel) {
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

        Span nameSpan = new Span(artikel.getName());
        nameSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "0.875rem")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span badge = new Span(artikel.getBestand() + " Stk.");
        badge.getStyle()
                .set("background", "#ba1a1a").set("color", "white")
                .set("border-radius", "9999px").set("padding", "0.15rem 0.6rem")
                .set("font-size", "0.75rem").set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span vonText = new Span("von " + artikel.getMinimalbestand() + " Stk. (Min)");
        vonText.getStyle()
                .set("font-size", "0.8rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout bestandRow = new HorizontalLayout();
        bestandRow.setAlignItems(FlexComponent.Alignment.CENTER);
        bestandRow.setSpacing(false);
        bestandRow.getStyle().set("gap", "0.4rem");
        bestandRow.add(badge, vonText);

        info.add(nameSpan, bestandRow);

        Button bestellBtn = new Button("Wareneingang buchen");
        bestellBtn.getStyle()
                .set("background", "#553722").set("color", "white")
                .set("border", "none").set("border-radius", "0.75rem")
                .set("padding", "0.6rem 1.25rem").set("font-weight", "700")
                .set("font-size", "0.8rem").set("cursor", "pointer")
                .set("white-space", "nowrap")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("flex-shrink", "0");

        bestellBtn.addClickListener(e -> {
            Dialog dialog = buildWareneingangDialog(artikel);
            dialog.addOpenedChangeListener(ev -> {
                if (!ev.isOpened()) ladeAlles();
            });
            dialog.open();
        });

        karte.add(info);
        // Wareneingang buchen nur für Manager (Lastenheft §3.5)
        if (istManager()) {
            karte.add(bestellBtn);
        }
        return karte;
    }

    // ═══════════════════════════════════════════════════════════
    // BESTANDSTABELLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Vollständige Bestandstabelle mit Suchfeld.
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

        container.add(buildTabellenKopf(), tabellenZeilen);
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
        suchfeld.addValueChangeListener(e -> ladeBestandsZeilen(e.getValue()));

        kopf.add(titel, suchfeld);
        return kopf;
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
                buildHeaderZelle("Artikel",        BREITE_ARTIKEL),
                buildHeaderZelle("Kategorie",      BREITE_KATEGORIE),
                buildHeaderZelle("Bestand",        BREITE_BESTAND),
                buildHeaderZelle("Minimalbestand", BREITE_MINIMAL),
                buildHeaderZelle("Status",         BREITE_STATUS),
                buildHeaderZelle("",               BREITE_AKTION)
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
     * - "kritisch" → roter Punkt
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

        Button aktionBtn = new Button();
        aktionBtn.getElement().appendChild(createIcon("more_vert").getElement());
        aktionBtn.getStyle()
                .set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("padding", "0.25rem")
                .set("border-radius", "9999px").set("color", "#82746d")
                .set("min-width", "unset").set("opacity", "0")
                .set("transition", "opacity 0.15s");
        aktionBtn.addClassName("zeilen-aktion");

        Div aktionZelle = new Div(aktionBtn);
        aktionZelle.getStyle()
                .set("width", BREITE_AKTION)
                .set("display", "flex")
                .set("justify-content", "flex-end");

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