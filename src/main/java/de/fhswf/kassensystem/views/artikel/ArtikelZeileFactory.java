package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.views.components.FehlerUI;

/**
 * Fabrikklasse für einzelne Tabellenzeilen in der Artikelverwaltung.
 *
 * <p>Jede Zeile enthält: ID, Name, Kategorie, Preis, MwSt, Bestand,
 * Minimalgrenze, Status-Badge sowie Aktions-Buttons (Bearbeiten / Deaktivieren).
 * Die Aktions-Buttons sind dauerhaft sichtbar (kein Hover-Hide).
 *
 * @author Adrian Krawietz
 */
class ArtikelZeileFactory {

    static final String BREITE_ID        = "7%";
    static final String BREITE_NAME      = "18%";
    static final String BREITE_KATEGORIE = "14%";
    static final String BREITE_PREIS     = "9%";
    static final String BREITE_MWST      = "6%";
    static final String BREITE_BESTAND   = "12%";
    static final String BREITE_MINIMAL   = "11%";
    static final String BREITE_STATUS    = "10%";
    static final String BREITE_AKTIONEN  = "13%";

    private ArtikelZeileFactory() {}

    /**
     * Erstellt eine vollständig gestylte Tabellenzeile für den übergebenen Artikel.
     *
     * @param artikel        der darzustellende Artikel
     * @param artikelService Service für Bearbeiten- und Deaktivieren-Aktionen
     * @param onAenderung    wird nach jeder Änderung aufgerufen um die Tabelle neu zu laden
     * @return fertiges Zeilen-Layout
     */
    static HorizontalLayout create(Artikel artikel, ArtikelService artikelService,
                                   Runnable onAenderung) {
        boolean warnBestand = artikel.getBestand() < artikel.getMinimalbestand();
        String bestandText  = artikel.getBestand() == Integer.MAX_VALUE
                ? "∞" : artikel.getBestand() + " Stk.";
        String preisText    = String.format("%,.2f €", artikel.getPreis());
        String mwstText     = artikel.getMehrwertsteuer().getSatz()
                .stripTrailingZeros().toPlainString() + "%";

        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("background", "white").set("border-radius", "1rem")
                .set("padding", "0.75rem 1.5rem").set("gap", "0").set("transition", "background 0.15s");

        Span idZelle = buildZelle("#ART-" + String.format("%03d", artikel.getId()),
                BREITE_ID, "0.7rem", "#82746d", false);
        idZelle.getStyle().set("font-family", "monospace").set("opacity", "0.7");

        zeile.add(
                idZelle,
                buildZelle(artikel.getName(),                    BREITE_NAME,      "0.875rem", "#1a1a2e", true),
                buildZelle(artikel.getKategorie().getName(),     BREITE_KATEGORIE, "0.875rem", "#50453e", false),
                buildZelle(preisText,                            BREITE_PREIS,     "0.875rem", "#1a1a2e", true),
                buildZelle(mwstText,                             BREITE_MWST,      "0.875rem", "#50453e", false),
                buildBestandZelle(bestandText, warnBestand),
                buildZelle(String.valueOf(artikel.getMinimalbestand()), BREITE_MINIMAL, "0.875rem", "#82746d", false),
                new Div(buildStatusBadge(artikel.isAktiv())),
                buildAktionenZelle(artikel, artikelService, onAenderung)
        );

        ((Div) zeile.getComponentAt(7)).getStyle().set("width", BREITE_STATUS);

        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => { this.style.background = '#f0eeff'; });" +
                        "this.addEventListener('mouseleave', () => { this.style.background = 'white'; });");
        return zeile;
    }

    /**
     * Erstellt eine einfache Text-Zelle mit definierter Breite und Farbe.
     */
    private static Span buildZelle(String text, String breite, String fontSize,
                                   String color, boolean bold) {
        Span span = new Span(text);
        span.getStyle()
                .set("width", breite).set("font-size", fontSize).set("color", color)
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        if (bold) span.getStyle().set("font-weight", "700");
        return span;
    }

    /**
     * Erstellt die Bestand-Zelle – bei Warnung mit rotem Text und Warn-Icon.
     *
     * @param bestandText formatierter Bestandstext (z.B. "6 Stk.")
     * @param warn        {@code true} wenn Bestand unter Minimalgrenze liegt
     */
    private static HorizontalLayout buildBestandZelle(String bestandText, boolean warn) {
        HorizontalLayout zelle = new HorizontalLayout();
        zelle.setAlignItems(FlexComponent.Alignment.CENTER);
        zelle.setPadding(false);
        zelle.setSpacing(false);
        zelle.getStyle().set("width", BREITE_BESTAND).set("gap", "0.4rem");

        if (warn) {
            Span warnIcon = new Span("warning");
            warnIcon.addClassName("material-symbols-outlined");
            warnIcon.getStyle().set("font-size", "1rem").set("color", "#ba1a1a");
            zelle.add(warnIcon);
        }

        Span text = new Span(bestandText);
        text.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", warn ? "700" : "400")
                .set("color", warn ? "#ba1a1a" : "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        zelle.add(text);
        return zelle;
    }

    /**
     * Erstellt den Status-Badge ("Aktiv" / "Inaktiv") für die Statusspalte.
     *
     * @param aktiv {@code true} für aktiven, {@code false} für inaktiven Artikel
     */
    private static Span buildStatusBadge(boolean aktiv) {
        Span badge = new Span(aktiv ? "Aktiv" : "Inaktiv");
        badge.getStyle()
                .set("padding", "0.2rem 0.75rem").set("border-radius", "9999px")
                .set("font-size", "0.7rem").set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("background", aktiv ? "#e1e1c9" : "rgba(212,195,186,0.3)")
                .set("color", aktiv ? "#474836" : "#82746d");
        return badge;
    }

    /**
     * Erstellt die Aktionen-Zelle mit Bearbeiten- und Sichtbarkeits-Button.
     *
     * @param artikel        Artikel dessen Daten bearbeitet oder deaktiviert werden
     * @param artikelService Service für Update- und Delete-Operationen
     * @param onAenderung    Callback nach erfolgreicher Aktion
     */
    private static Div buildAktionenZelle(Artikel artikel, ArtikelService artikelService,
                                          Runnable onAenderung) {
        Div zelle = new Div();
        zelle.getStyle()
                .set("width", BREITE_AKTIONEN).set("display", "flex")
                .set("justify-content", "flex-start").set("gap", "0.5rem")
                .set("opacity", "1");
        zelle.getElement().setAttribute("tour-id", "artikel-aktionen");

        Button editBtn = buildAktionsButton("edit", "#553722", "#ffdcc6");
        editBtn.getElement().setAttribute("tour-id", "artikel-bearbeiten-btn");
        editBtn.addClickListener(e -> {
            NeuerArtikelDialog dialog = new NeuerArtikelDialog(artikelService, artikel);
            dialog.addOpenedChangeListener(ev -> { if (!ev.isOpened()) onAenderung.run(); });
            dialog.open();
        });

        Button sichtbarBtn = buildAktionsButton(
                artikel.isAktiv() ? "visibility_off" : "visibility",
                artikel.isAktiv() ? "#ba1a1a" : "#553722",
                artikel.isAktiv() ? "#ffdad6" : "#ffdcc6");
        sichtbarBtn.getElement().setAttribute("tour-id", "artikel-deaktivieren-btn");
        sichtbarBtn.addClickListener(e -> {
            try {
                if (artikel.isAktiv()) {
                    artikelService.deleteArtikel(artikel.getId());
                } else {
                    artikel.setAktiv(true);
                    artikelService.updateArtikel(artikel);
                }
                onAenderung.run();
            } catch (KassensystemException ex) {
                FehlerUI.fehler(ex.getMessage());
            } catch (Exception ex) {
                FehlerUI.technischerFehler(ex);
            }
        });

        zelle.add(editBtn, sichtbarBtn);
        return zelle;
    }

    /**
     * Erstellt einen Icon-Button für die Aktionsspalte mit Hover-Hintergrund.
     *
     * @param iconName   Material-Symbols-Icon-Name (z.B. "edit")
     * @param iconFarbe  Standardfarbe des Icons
     * @param hoverFarbe Hintergrundfarbe beim Hover
     */
    private static Button buildAktionsButton(String iconName, String iconFarbe, String hoverFarbe) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("font-size", "1.1rem").set("color", iconFarbe);

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("padding", "0.4rem").set("border-radius", "0.5rem")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                .set("min-width", "unset").set("transition", "background 0.15s");
        btn.getElement().executeJs(
                "this.addEventListener('mouseenter', () => this.style.background = '" + hoverFarbe + "');" +
                        "this.addEventListener('mouseleave', () => this.style.background = 'none');");
        return btn;
    }
}