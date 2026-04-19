package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.views.components.BaseDialog;

import java.util.function.Consumer;

/**
 * Dialog zur Auswahl der Zahlungsart (Bar oder Karte) nach dem Kassieren.
 *
 * <p>Zeigt den Gesamtbetrag und zwei Zahlung-Buttons. Ein Klick auf einen Button
 * übergibt die gewählte {@link de.fhswf.kassensystem.model.enums.Zahlungsart}
 * an den {@code onZahlung}-Callback und schließt den Dialog.
 *
 * @author Adrian Krawietz
 */
class ZahlungsDialog extends BaseDialog {

    private final Consumer<Zahlungsart> onZahlung;
    private final String gesamtBetrag;

    /**
     * Erstellt den Zahlungsdialog.
     *
     * @param gesamtBetrag formatierter Gesamtbetrag (z.B. "12,99 €"), wird im Dialog angezeigt
     * @param onZahlung    wird mit der gewählten Zahlungsart aufgerufen
     */
    ZahlungsDialog(String gesamtBetrag, Consumer<Zahlungsart> onZahlung) {
        this.gesamtBetrag = gesamtBetrag;
        this.onZahlung    = onZahlung;
        init("Zahlungsart wählen", null);
    }

    /**
     * Erstellt den Dialog-Body mit Gesamtbetrags-Anzeige und Zahlung-Buttons.
     */
    @Override
    protected VerticalLayout buildBody() {
        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("background", "white").set("padding", "1.5rem").set("gap", "1rem");

        Span gesamtInfo = new Span("Gesamtbetrag: " + gesamtBetrag);
        gesamtInfo.getStyle()
                .set("font-size", "1.1rem").set("font-weight", "700").set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        body.add(gesamtInfo, buildZahlungsButtons());
        return body;
    }

    /**
     * Erstellt die horizontale Zeile mit Bar- und Karte-Button.
     */
    private HorizontalLayout buildZahlungsButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setSpacing(false);
        buttons.getStyle().set("gap", "1rem");
        buttons.add(
                buildZahlungButton("wallet",      "Bar",   "#ffdcc6", "#553722", Zahlungsart.BAR),
                buildZahlungButton("credit_card", "Karte", "#553722", "white",   Zahlungsart.KARTE)
        );
        return buttons;
    }

    /**
     * Erstellt einen einzelnen Zahlungsart-Button.
     *
     * @param iconName Material-Symbols-Icon-Name
     * @param label    Beschriftungstext (z.B. "Bar")
     * @param bg       Hintergrundfarbe
     * @param color    Schriftfarbe
     * @param art      die zugeordnete Zahlungsart
     */
    private Button buildZahlungButton(String iconName, String label,
                                      String bg, String color, Zahlungsart art) {
        Span btnIcon = new Span(iconName);
        btnIcon.addClassName("material-symbols-outlined");
        btnIcon.getStyle().set("line-height", "1");

        Span btnText = new Span(label);
        btnText.getStyle().set("font-weight", "700").set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button btn = new Button();
        btn.getElement().appendChild(btnIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("flex", "1").set("padding", "1.25rem").set("background", bg)
                .set("color", color).set("border", "none").set("border-radius", "1rem")
                .set("cursor", "pointer").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("gap", "0.5rem").set("font-size", "1rem");
        btn.addClickListener(e -> { onZahlung.accept(art); close(); });
        return btn;
    }

    @Override
    protected boolean onSpeichern() { return true; }
}