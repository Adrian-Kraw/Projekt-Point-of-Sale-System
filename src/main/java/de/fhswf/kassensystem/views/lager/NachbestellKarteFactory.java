package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.model.Artikel;

import java.util.function.Consumer;

/**
 * Fabrikklasse für einzelne Nachbestellhinweis-Karten in der Lagerverwaltung.
 *
 * <p>Zeigt Artikelname und aktuellen Bestand im Vergleich zur Minimalgrenze.
 * Manager erhalten zusätzlich einen "Wareneingang buchen"-Button.
 *
 * @author Adrian
 */
class NachbestellKarteFactory {

    private NachbestellKarteFactory() {}

    /**
     * Erstellt eine Nachbestellhinweis-Karte für den übergebenen Artikel.
     *
     * @param artikel               der zu nachzubestellende Artikel
     * @param istManager            {@code true} wenn der Benutzer Manager ist (zeigt Bestell-Button)
     * @param onWareneingangOeffnen Callback der den WareneingangDialog für diesen Artikel öffnet
     * @return fertige Karte
     */
    static HorizontalLayout create(Artikel artikel, boolean istManager,
                                   Consumer<Artikel> onWareneingangOeffnen) {
        HorizontalLayout karte = new HorizontalLayout();
        karte.setAlignItems(FlexComponent.Alignment.CENTER);
        karte.setSpacing(false);
        karte.getStyle()
                .set("background", "rgba(255,255,255,0.6)").set("border-radius", "0.75rem")
                .set("padding", "1rem 1.25rem").set("gap", "1rem")
                .set("flex", "1").set("min-width", "200px");

        Span nameSpan = new Span(artikel.getName());
        nameSpan.getStyle()
                .set("font-weight", "700").set("font-size", "0.875rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span badge = new Span(artikel.getBestand() + " Stk.");
        badge.getStyle()
                .set("background", "#ba1a1a").set("color", "white").set("border-radius", "9999px")
                .set("padding", "0.15rem 0.6rem").set("font-size", "0.75rem").set("font-weight", "700")
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

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("flex", "1");
        info.add(nameSpan, bestandRow);

        karte.add(info);
        if (istManager) {
            Button bestellBtn = new Button("Wareneingang buchen");
            bestellBtn.getStyle()
                    .set("background", "#553722").set("color", "white").set("border", "none")
                    .set("border-radius", "0.75rem").set("padding", "0.6rem 1.25rem")
                    .set("font-weight", "700").set("font-size", "0.8rem").set("cursor", "pointer")
                    .set("white-space", "nowrap").set("font-family", "'Plus Jakarta Sans', sans-serif")
                    .set("flex-shrink", "0");
            bestellBtn.addClickListener(e -> onWareneingangOeffnen.accept(artikel));
            karte.add(bestellBtn);
        }
        return karte;
    }
}
