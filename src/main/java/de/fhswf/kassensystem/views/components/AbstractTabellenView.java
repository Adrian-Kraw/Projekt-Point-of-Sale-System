package de.fhswf.kassensystem.views.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.views.SecuredView;

/**
 * Abstrakte Zwischenschicht für alle Views mit tabellarischem Layout.
 *
 * <p>Stellt den gemeinsamen {@code tabelle}-Container sowie die
 * {@link #headerZelle(String, String)}-Hilfsmethode bereit, die in
 * {@link de.fhswf.kassensystem.views.artikel.ArtikelView},
 * {@link de.fhswf.kassensystem.views.benutzer.BenutzerView} und
 * {@link de.fhswf.kassensystem.views.lager.LagerView} identisch benötigt werden.
 *
 * <p>Unterklassen müssen {@link #buildHeader()} und {@link #ladeDaten()} implementieren.
 *
 * @author Adrian Krawietz
 */
public abstract class AbstractTabellenView extends SecuredView {

    /** Gemeinsamer Tabellen-Container – alle Unterklassen befüllen ihn via ladeDaten(). */
    protected final VerticalLayout tabelle = new VerticalLayout();

    /**
     * Initialisiert den Tabellen-Container und wendet den Standard-Hintergrund an.
     *
     * @param mindestRolle die für den Zugriff erforderliche Mindestrolle
     */
    protected AbstractTabellenView(Rolle mindestRolle) {
        super(mindestRolle);
        applyStandardBackground();

        tabelle.setWidthFull();
        tabelle.setPadding(false);
        tabelle.setSpacing(false);
        tabelle.getStyle().set("gap", "0");
    }

    /**
     * Erstellt eine einheitlich gestylte Tabellen-Kopfzelle.
     *
     * @param text   der Spaltenüberschriften-Text
     * @param breite CSS-Breite der Zelle (z.B. "20%")
     * @return gestylter {@code Span}
     */
    protected Span headerZelle(String text, String breite) {
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
     * Erstellt den View-spezifischen Seitenkopf (Titel, Buttons, Suche).
     *
     * @return fertig konfiguriertes Header-Layout
     */
    protected abstract HorizontalLayout buildHeader();

    /**
     * Lädt die Daten neu und befüllt den {@link #tabelle}-Container.
     * Wird nach jeder Änderung (Anlegen, Bearbeiten, Löschen) aufgerufen.
     */
    protected abstract void ladeDaten();
}