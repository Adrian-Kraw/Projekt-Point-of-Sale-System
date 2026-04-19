package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Header der Warenkorb-Spalte mit Titel ("Warenkorb") und Komplett-Löschen-Button.
 *
 * <p>Ein Klick auf den Löschen-Button ruft den {@code onLoeschen}-Callback auf,
 * der in {@link VerkaufView} den gesamten Warenkorb leert.
 *
 * @author Adrian Krawietz
 */
class WarenkorbHeader extends HorizontalLayout {

    /**
     * Erstellt den Warenkorb-Header.
     *
     * @param onLoeschen wird aufgerufen wenn der Löschen-Button geklickt wird
     */
    WarenkorbHeader(Runnable onLoeschen) {
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        setPadding(false);
        getStyle().set("padding", "2rem 2rem 1rem 2rem");

        add(buildTitelGruppe(), buildLoeschenButton(onLoeschen));
    }

    /**
     * Erstellt die linke Titelgruppe mit Icon-Box und "Warenkorb"-Überschrift.
     */
    private HorizontalLayout buildTitelGruppe() {
        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "rgba(85,55,34,0.1)").set("border-radius", "0.75rem")
                .set("padding", "0.75rem").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");
        Span icon = icon("receipt");
        icon.getStyle().set("color", "#553722");
        iconBox.add(icon);

        H2 titel = new H2("Warenkorb");
        titel.getStyle()
                .set("margin", "0").set("font-size", "1.25rem").set("font-weight", "700")
                .set("color", "#1a1a2e").set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout gruppe = new HorizontalLayout();
        gruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        gruppe.setSpacing(false);
        gruppe.getStyle().set("gap", "0.75rem");
        gruppe.add(iconBox, titel);
        return gruppe;
    }

    /**
     * Erstellt den Löschen-Button mit Mülleimer-Icon.
     *
     * @param onLoeschen Callback für den Klick
     */
    private Button buildLoeschenButton(Runnable onLoeschen) {
        Button btn = new Button();
        btn.getElement().appendChild(icon("delete_sweep").getElement());
        btn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("color", "#82746d").set("padding", "0.5rem")
                .set("border-radius", "9999px").set("min-width", "unset");
        btn.addClickListener(e -> onLoeschen.run());
        return btn;
    }

    /**
     * Erstellt einen Material-Symbols-Icon-Span.
     */
    private static Span icon(String name) {
        Span s = new Span(name);
        s.addClassName("material-symbols-outlined");
        s.getStyle().set("line-height", "1");
        return s;
    }
}
