package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.views.components.BaseDialog;

/**
 * Dialog, der nach einem Verkauf fragt, ob eine Quittung gedruckt werden soll.
 *
 * <p>Verwendet einen benutzerdefinierten Footer (Ja/Nein statt Speichern/Abbrechen).
 * Je nach Antwort wird der {@code onJa}- oder {@code onNein}-Callback aufgerufen,
 * der den Kassenbon-Druck bzw. das Leeren des Warenkorbs auslöst.
 *
 * @author Adrian
 */
public class QuittungsDialog extends BaseDialog {

    private final Runnable onJa;
    private final Runnable onNein;

    /**
     * Erstellt den Quittungsdialog.
     *
     * @param onJa   wird aufgerufen, wenn "Ja" geklickt wird (Kassenbon drucken + Warenkorb leeren)
     * @param onNein wird aufgerufen, wenn "Nein" geklickt wird (nur Warenkorb leeren)
     */
    public QuittungsDialog(Runnable onJa, Runnable onNein) {
        this.onJa   = onJa;
        this.onNein = onNein;
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
     * Erstellt den Ja/Nein-Footer als Ersatz für den Standard-Footer.
     */
    @Override
    protected HorizontalLayout buildCustomFooter() {
        Button jaBtn = new Button("Ja");
        jaBtn.getStyle()
                .set("flex", "1").set("background", "#553722").set("color", "white")
                .set("border", "none").set("border-radius", "1rem").set("padding", "0.75rem 2rem")
                .set("font-weight", "700").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("box-shadow", "0 4px 15px rgba(85,55,34,0.25)");
        jaBtn.addClickListener(e -> { close(); onJa.run(); });

        Button neinBtn = new Button("Nein");
        neinBtn.getStyle()
                .set("flex", "1").set("background", "transparent")
                .set("border", "2px solid rgba(85,55,34,0.2)").set("border-radius", "1rem")
                .set("padding", "0.75rem 2rem").set("font-weight", "700")
                .set("color", "#553722").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        neinBtn.addClickListener(e -> { close(); onNein.run(); });

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setSpacing(false);
        footer.getStyle().set("background", "#f5f2ff").set("padding", "1.25rem 1.5rem").set("gap", "1rem");
        footer.add(jaBtn, neinBtn);
        return footer;
    }

    @Override
    protected boolean onSpeichern() { return true; }
}