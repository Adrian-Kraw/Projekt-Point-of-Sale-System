package de.fhswf.kassensystem.views.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.views.SecuredView;

/**
 * Zwischenschicht für Views mit Tabellen-Layout.
 *
 * Liefert:
 *  - tabelle-Instanzfeld (in ArtikelView, BenutzerView, LagerView identisch initialisiert)
 *  - headerZelle()       (war wörtlich identisch in ArtikelView und BenutzerView)
 *  - buildHeader() / ladeDaten() als abstrakte Pflicht für Unterklassen
 */
public abstract class AbstractTabellenView extends SecuredView {

    /** Gemeinsamer Tabellen-Container – alle Unterklassen befüllen ihn via ladeDaten(). */
    protected final VerticalLayout tabelle = new VerticalLayout();

    protected AbstractTabellenView(Rolle mindestRolle) {
        super(mindestRolle);
        applyStandardBackground();

        tabelle.setWidthFull();
        tabelle.setPadding(false);
        tabelle.setSpacing(false);
        tabelle.getStyle().set("gap", "0");
    }

    /**
     * Baut eine einheitliche Tabellen-Kopfzelle.
     * War wörtlich identisch in ArtikelView (Z. 346) und BenutzerView (Z. 155).
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

    /** Jede Unterklasse definiert ihren View-spezifischen Header. */
    protected abstract HorizontalLayout buildHeader();

    /** Jede Unterklasse lädt ihre Daten in tabelle. */
    protected abstract void ladeDaten();
}
