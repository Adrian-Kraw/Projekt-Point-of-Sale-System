package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Panel für die Zahlungsarten-Aufteilung im Tagesabschluss.
 *
 * <p>Zeigt zwei Zahlungskarten (Bar/Karte) und ein umschaltbares
 * Diagramm (Torte oder Balken). Das Diagramm wird über {@link DiagrammFactory} gerendert.
 *
 * @author Adrian
 */
class ZahlungsartenPanel extends VerticalLayout {

    /**
     * Erstellt das Panel mit Zahlungskarten und Balkendiagramm als Standard.
     *
     * @param umsatzBar   Tagesumsatz per Barzahlung
     * @param umsatzKarte Tagesumsatz per Kartenzahlung
     */
    ZahlungsartenPanel(BigDecimal umsatzBar, BigDecimal umsatzKarte) {
        setPadding(false);
        setSpacing(false);
        getStyle().set("flex", "0 0 780px").set("width", "780px").set("max-width", "780px").set("gap", "1.5rem");

        String[][] daten = {
                {"Bar",   umsatzBar.setScale(2, RoundingMode.HALF_UP).toPlainString(),   "#ffdcc6"},
                {"Karte", umsatzKarte.setScale(2, RoundingMode.HALF_UP).toPlainString(), "#553722"}
        };

        Div diagrammContainer = new Div();
        diagrammContainer.setWidthFull();
        diagrammContainer.getStyle()
                .set("background", "#f5f2ff").set("border-radius", "1rem").set("padding", "1.5rem");
        diagrammContainer.add(DiagrammFactory.buildDiagramm(daten, true));

        Button torteBtn  = DiagrammFactory.buildToggleButton("Torte",  false);
        Button balkenBtn = DiagrammFactory.buildToggleButton("Balken", true);
        torteBtn.addClickListener(e -> {
            diagrammContainer.removeAll();
            diagrammContainer.add(DiagrammFactory.buildDiagramm(daten, false));
            torteBtn.getStyle().set("background", "#553722").set("color", "white");
            balkenBtn.getStyle().set("background", "transparent").set("color", "#553722");
        });
        balkenBtn.addClickListener(e -> {
            diagrammContainer.removeAll();
            diagrammContainer.add(DiagrammFactory.buildDiagramm(daten, true));
            balkenBtn.getStyle().set("background", "#553722").set("color", "white");
            torteBtn.getStyle().set("background", "transparent").set("color", "#553722");
        });

        HorizontalLayout toggle = new HorizontalLayout();
        toggle.setSpacing(false);
        toggle.getStyle().set("background", "#efecff").set("border-radius", "9999px")
                .set("padding", "0.25rem").set("gap", "0.25rem");
        toggle.add(torteBtn, balkenBtn);

        Div akzent = new Div();
        akzent.getStyle().set("width", "0.25rem").set("height", "1.5rem")
                .set("background", "#553722").set("border-radius", "9999px");
        H4 titel = new H4("Zahlungsarten");
        titel.getStyle().set("margin", "0").set("font-size", "1.1rem").set("font-weight", "700")
                .set("color", "#553722").set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "0.75rem");
        titelGruppe.add(akzent, titel);

        HorizontalLayout titelZeile = new HorizontalLayout();
        titelZeile.setWidthFull();
        titelZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        titelZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titelZeile.setPadding(false);
        titelZeile.add(titelGruppe, toggle);

        HorizontalLayout kartenZeile = new HorizontalLayout();
        kartenZeile.setWidthFull();
        kartenZeile.setSpacing(false);
        kartenZeile.getStyle().set("gap", "1rem");
        kartenZeile.add(
                buildZahlungsKarte("Bar",   BerichteUtils.fp(umsatzBar),   "wallet"),
                buildZahlungsKarte("Karte", BerichteUtils.fp(umsatzKarte), "credit_card")
        );

        add(titelZeile, kartenZeile, diagrammContainer);
    }

    /**
     * Erstellt eine einzelne Zahlungsart-Karte mit Label, Betrag und Icon.
     *
     * @param label    Zahlungsartbezeichnung (z.B. "Bar")
     * @param betrag   formatierter Betrag (z.B. "42,50€")
     * @param iconName Material-Symbols-Icon-Name
     */
    private HorizontalLayout buildZahlungsKarte(String label, String betrag, String iconName) {
        HorizontalLayout karte = new HorizontalLayout();
        karte.setAlignItems(FlexComponent.Alignment.CENTER);
        karte.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        karte.setSpacing(false);
        karte.getStyle().set("flex", "1").set("background", "#efecff")
                .set("border-radius", "1rem").set("padding", "1.5rem");

        Span l = new Span(label.toUpperCase());
        l.getStyle().set("font-size", "0.65rem").set("font-weight", "800").set("letter-spacing", "0.05em")
                .set("color", "rgba(85,55,34,0.6)").set("font-family", "'Plus Jakarta Sans', sans-serif");
        Span b = new Span(betrag);
        b.getStyle().set("font-size", "1.5rem").set("font-weight", "900").set("color", "#553722")
                .set("letter-spacing", "-0.025em").set("font-family", "'Plus Jakarta Sans', sans-serif");
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.add(l, b);

        Span ic = new Span(iconName);
        ic.addClassName("material-symbols-outlined");
        ic.getStyle().set("color", "#553722").set("font-variation-settings", "'FILL' 1").set("line-height", "1");
        Div iconBox = new Div(ic);
        iconBox.getStyle().set("width", "3rem").set("height", "3rem").set("border-radius", "9999px")
                .set("background", "white").set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)");

        karte.add(info, iconBox);
        return karte;
    }
}