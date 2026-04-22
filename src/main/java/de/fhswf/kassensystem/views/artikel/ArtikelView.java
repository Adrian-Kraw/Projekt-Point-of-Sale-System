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
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.views.MainLayout;
import de.fhswf.kassensystem.views.components.AbstractTabellenView;
import de.fhswf.kassensystem.views.components.FehlerUI;
import de.fhswf.kassensystem.views.components.StatistikKarte;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Artikelverwaltungs-View – zeigt alle Artikel in einer Tabelle
 * und erlaubt Suche, Anlage, Bearbeitung und Deaktivierung.
 *
 * <p>Unterhalb der Tabelle werden vier {@link de.fhswf.kassensystem.views.components.StatistikKarte Statistikkarten}
 * angezeigt: Gesamtartikel, aktive Artikel, Artikel mit niedrigem Bestand und Kategorienanzahl.
 *
 * <p>Zugriff: Rolle {@code MANAGER}.
 *
 * @author Adrian Krawietz
 */
@Route(value = "artikel", layout = MainLayout.class)
public class ArtikelView extends AbstractTabellenView {

    private final ArtikelService artikelService;
    private final HorizontalLayout statistikKartenLayout = new HorizontalLayout();
    private String aktuelleSuche = "";

    /**
     * Erstellt die View, lädt Daten und Statistikkarten.
     *
     * @param artikelService Service für alle Artikel-Operationen
     */
    public ArtikelView(ArtikelService artikelService) {
        super(Rolle.MANAGER);
        this.artikelService = artikelService;

        statistikKartenLayout.setWidthFull();
        statistikKartenLayout.setSpacing(false);
        statistikKartenLayout.getStyle().set("gap", "1.5rem").set("margin-top", "1.5rem");

        add(buildHeader(), buildTabellenBereich(), statistikKartenLayout);
        ladeDaten();
        ladeStatistikKarten();
    }

    /**
     * Baut den Seitenkopf mit Titel-Icon, Suche und "Neuer Artikel"-Button.
     */
    @Override
    protected HorizontalLayout buildHeader() {
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
     * Lädt alle Artikel (ggf. gefiltert nach Suchbegriff) und befüllt die Tabelle.
     */
    @Override
    public void ladeDaten() {
        tabelle.removeAll();
        tabelle.add(buildTabellenHeader());
        try {
            List<Artikel> artikel = aktuelleSuche.isBlank()
                    ? artikelService.findAllArtikel()
                    : artikelService.findByName(aktuelleSuche);
            artikel.sort(java.util.Comparator.comparing(Artikel::getId));
            for (Artikel a : artikel) {
                tabelle.add(ArtikelZeileFactory.create(a, artikelService, this::refresh));
            }
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Aktualisiert Tabelle und Statistikkarten nach einer Änderung.
     */
    private void refresh() {
        ladeDaten();
        ladeStatistikKarten();
    }

    /**
     * Berechnet und rendert die vier Statistikkarten unter der Tabelle.
     */
    private void ladeStatistikKarten() {
        statistikKartenLayout.removeAll();
        try {
            List<Artikel> alle  = artikelService.findAllArtikel();
            long gesamtArtikel  = alle.size();
            long aktiv          = alle.stream().filter(Artikel::isAktiv).count();
            long niedrigBestand = alle.stream().filter(a -> a.getBestand() < a.getMinimalbestand()).count();
            long kategorien     = alle.stream().map(a -> a.getKategorie().getId()).collect(Collectors.toSet()).size();

            statistikKartenLayout.add(
                    new StatistikKarte("Gesamtartikel",     String.valueOf(gesamtArtikel), "inventory_2",  false),
                    new StatistikKarte("Aktiv",             String.valueOf(aktiv),          "check_circle", false),
                    new StatistikKarte("Niedriger Bestand", String.valueOf(niedrigBestand), "warning",      true),
                    new StatistikKarte("Kategorien",        String.valueOf(kategorien),     "category",     false)
            );
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Erstellt die Titelgruppe (Icon-Box + Überschrift "Artikelverwaltung").
     */
    private HorizontalLayout buildTitel() {
        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "1rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "#e2e0fc").set("border-radius", "1rem").set("padding", "0.75rem")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        Span icon = createIcon("label");
        icon.getStyle().set("color", "#553722").set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Artikelverwaltung");
        titel.getStyle()
                .set("margin", "0").set("font-size", "1.75rem").set("font-weight", "800")
                .set("color", "#1a1a2e").set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);
        return titelGruppe;
    }

    /**
     * Erstellt den rechten Teil des Headers mit Suchfeld und "Neuer Artikel"-Button.
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
        suchfeld.getElement().setAttribute("tour-id", "artikel-suchfeld");
        suchfeld.addValueChangeListener(e -> { aktuelleSuche = e.getValue(); ladeDaten(); });

        aktionen.add(suchfeld, buildNeuerArtikelButton());
        return aktionen;
    }

    /**
     * Erstellt den "Neuer Artikel"-Button, der beim Klick den {@link NeuerArtikelDialog} öffnet.
     */
    private Button buildNeuerArtikelButton() {
        Span plusIcon = createIcon("add");
        plusIcon.getStyle().set("font-size", "1.1rem");
        Span btnText = new Span("Neuer Artikel");
        btnText.getStyle().set("font-weight", "700").set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button btn = new Button();
        btn.getElement().setAttribute("tour-id", "neuer-artikel-btn");
        btn.getElement().appendChild(plusIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("background", "linear-gradient(135deg, #553722, #6f4e37)").set("color", "white")
                .set("border", "none").set("border-radius", "1rem").set("padding", "0.75rem 1.5rem")
                .set("cursor", "pointer").set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("gap", "0.5rem").set("white-space", "nowrap")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        btn.addClickListener(e -> oeffneNeuerArtikelDialog());
        return btn;
    }

    /**
     * Verarbeitet Tour-Aktionen aus dem {@link de.fhswf.kassensystem.tour.TourManager}.
     * Aktuell unterstützt: {@code "open-neuer-artikel-dialog"}.
     *
     * @param action Aktions-String aus dem Tour-Step
     */
    public void tourAktion(String action) {
        switch (action) {
            case "open-neuer-artikel-dialog" -> oeffneNeuerArtikelDialog();
            default -> {}
        }
    }

    /**
     * Öffnet den {@link NeuerArtikelDialog} und lädt nach dem Schließen die Daten neu.
     */
    private void oeffneNeuerArtikelDialog() {
        NeuerArtikelDialog dialog = new NeuerArtikelDialog(artikelService);
        dialog.addOpenedChangeListener(ev -> { if (!ev.isOpened()) refresh(); });
        dialog.open();
    }

    /**
     * Umhüllt die Tabelle in einem styled Container und setzt die Tour-ID.
     */
    private VerticalLayout buildTabellenBereich() {
        VerticalLayout bereich = new VerticalLayout();
        bereich.setWidthFull();
        bereich.setPadding(false);
        bereich.setSpacing(false);
        bereich.getElement().setAttribute("tour-id", "artikel-tabelle");
        bereich.getStyle()
                .set("background", "#f5f2ff").set("border-radius", "1.25rem").set("padding", "1.5rem");
        bereich.add(tabelle);
        return bereich;
    }

    /**
     * Erstellt die Kopfzeile der Tabelle mit allen Spaltenüberschriften.
     */
    private HorizontalLayout buildTabellenHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("padding", "0 1.5rem 0.5rem 1.5rem").set("gap", "0");
        Span aktionenHeader = headerZelle("Aktionen", ArtikelZeileFactory.BREITE_AKTIONEN);
        aktionenHeader.getElement().setAttribute("tour-id", "artikel-aktionen-header");
        header.add(
                headerZelle("ID",            ArtikelZeileFactory.BREITE_ID),
                headerZelle("Name",          ArtikelZeileFactory.BREITE_NAME),
                headerZelle("Kategorie",     ArtikelZeileFactory.BREITE_KATEGORIE),
                headerZelle("Preis",         ArtikelZeileFactory.BREITE_PREIS),
                headerZelle("MwSt",          ArtikelZeileFactory.BREITE_MWST),
                headerZelle("Bestand",       ArtikelZeileFactory.BREITE_BESTAND),
                headerZelle("Minimalgrenze", ArtikelZeileFactory.BREITE_MINIMAL),
                headerZelle("Status",        ArtikelZeileFactory.BREITE_STATUS),
                aktionenHeader
        );
        return header;
    }
}