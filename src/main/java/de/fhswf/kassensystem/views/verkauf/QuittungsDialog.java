package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.views.components.BaseDialog;

/**
 * Dialog, der nach einem Verkauf fragt, ob eine Quittung gedruckt werden soll.
 *
 * <p>Verwendet einen benutzerdefinierten Footer mit drei Aktionen: Ja (Kassenbon drucken),
 * Nein (ohne Quittung fortfahren) und Stornieren (Verkauf rückgängig machen).
 * Der Stornieren-Button erscheint nur wenn der entsprechende Callback übergeben wird.
 *
 * @author Adrian Krawietz
 */
public class QuittungsDialog extends BaseDialog {

    private final Runnable onJa;
    private final Runnable onNein;
    private final Runnable onStornieren;

    /**
     * Erstellt den Quittungsdialog.
     *
     * @param onJa         wird aufgerufen, wenn "Ja" geklickt wird (Kassenbon drucken + Warenkorb leeren)
     * @param onNein       wird aufgerufen, wenn "Nein" geklickt wird (nur Warenkorb leeren)
     * @param onStornieren wird aufgerufen, wenn "Stornieren" geklickt wird;
     *                     {@code null} blendet den Stornieren-Button aus
     */
    public QuittungsDialog(Runnable onJa, Runnable onNein, Runnable onStornieren) {
        this.onJa         = onJa;
        this.onNein       = onNein;
        this.onStornieren = onStornieren;
        init("Quittung drucken?", null);
    }

    /**
     * Erstellt den Dialog-Body mit dem Hinweistext.
     */
    @Override
    protected VerticalLayout buildBody() {
        Span hinweis = new Span("Möchten Sie eine Quittung für diesen Verkauf drucken?");
        hinweis.getStyle()
                .set("font-size", "0.9rem").set("color", "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("background", "white").set("padding", "1.5rem");
        body.add(hinweis);
        return body;
    }

    /**
     * Erstellt den Footer mit Ja/Nein in der oberen Zeile und optional
     * Stornieren in der unteren Zeile.
     */
    @Override
    protected HorizontalLayout buildCustomFooter() {
        Button jaBtn = buildFooterButton("Ja", "#553722", "white", true);
        jaBtn.addClickListener(e -> onJaGeklickt());

        Button neinBtn = buildFooterButton("Nein", "transparent", "#553722", false);
        neinBtn.addClickListener(e -> onNeinGeklickt());

        HorizontalLayout obenLayout = new HorizontalLayout();
        obenLayout.setWidthFull();
        obenLayout.setSpacing(false);
        obenLayout.getStyle().set("gap", "1rem");
        obenLayout.add(jaBtn, neinBtn);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setSpacing(false);
        footer.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "1.25rem 1.5rem")
                .set("gap", "0.75rem")
                .set("flex-direction", "column");

        footer.add(obenLayout);

        if (onStornieren != null) {
            Button stornoBtn = buildFooterButton("Stornieren", "#ffdad6", "#ba1a1a", false);
            stornoBtn.addClickListener(e -> onStornierenGeklickt());
            footer.add(stornoBtn);
        }

        return footer;
    }

    /**
     * Erstellt einen gestylten Footer-Button.
     *
     * @param label  Beschriftung des Buttons
     * @param bg     Hintergrundfarbe
     * @param color  Textfarbe
     * @param shadow {@code true} für einen Schatten-Effekt (Primär-Button)
     * @return fertig gestylter Button
     */
    private Button buildFooterButton(String label, String bg, String color, boolean shadow) {
        Button btn = new Button(label);
        btn.getStyle()
                .set("flex", "1").set("background", bg).set("color", color)
                .set("border", "none").set("border-radius", "1rem").set("padding", "0.75rem 2rem")
                .set("font-weight", "700").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        if (shadow) btn.getStyle().set("box-shadow", "0 4px 15px rgba(85,55,34,0.25)");
        return btn;
    }

    /**
     * Schließt den Dialog und führt den Ja-Callback aus (Kassenbon drucken).
     */
    private void onJaGeklickt() {
        close();
        onJa.run();
    }

    /**
     * Schließt den Dialog und führt den Nein-Callback aus (nur Warenkorb leeren).
     */
    private void onNeinGeklickt() {
        close();
        onNein.run();
    }

    /**
     * Schließt den Dialog und führt den Stornieren-Callback aus (Verkauf rückgängig machen).
     */
    private void onStornierenGeklickt() {
        close();
        onStornieren.run();
    }

    /**
     * Überschreibt die Standard-Speichern-Logik, da eigene Buttons die Aktionen auslösen.
     */
    @Override
    protected boolean onSpeichern() { return true; }
}