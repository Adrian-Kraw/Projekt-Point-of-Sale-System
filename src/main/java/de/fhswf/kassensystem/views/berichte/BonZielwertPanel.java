package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Inline-Panel für den Bon-Zielwert in der Tagesabschluss-Karte.
 *
 * <p>Kassierer sehen nur die aktuelle Zielwert-Anzeige.
 * Manager erhalten zusätzlich einen Bearbeiten-Button, der ein Eingabefeld
 * und einen Bestätigen-Button einblendet.
 *
 * <p>Beim Bestätigen wird der Wert über den {@code onSpeichern}-Callback
 * an die {@link BerichteView} übergeben, die ihn persistent via
 * {@link de.fhswf.kassensystem.service.EinstellungService} speichert.
 *
 * @author Adrian Krawietz
 */
public class BonZielwertPanel extends HorizontalLayout {

    /**
     * Erstellt das Panel.
     *
     * @param aktuellerZielwert der aktuell gespeicherte Zielwert (kann {@code null} oder 0 sein)
     * @param onSpeichern       Callback zum Speichern des neuen Werts; {@code null} für Kassierer (nur Anzeige)
     */
    public BonZielwertPanel(BigDecimal aktuellerZielwert, Consumer<BigDecimal> onSpeichern) {
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(false);
        getStyle()
                .set("gap", "0.5rem")
                .set("margin-top", "0.25rem");

        Span anzeige = new Span(formatZielwert(aktuellerZielwert));
        anzeige.getStyle()
                .set("font-size", "0.8rem").set("font-weight", "700")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Kein onSpeichern = Kassierer: nur Anzeige, kein Edit-Button
        if (onSpeichern == null) {
            add(anzeige);
            return;
        }

        // Edit-Button – nur Icon, kein Label
        Button editBtn = new Button();
        Span editIcon = new Span("edit");
        editIcon.addClassName("material-symbols-outlined");
        editIcon.getStyle().set("line-height", "1").set("font-size", "0.9rem");
        editBtn.getElement().appendChild(editIcon.getElement());
        editBtn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("padding", "0.2rem").set("min-width", "unset").set("color", "#82746d")
                .set("border-radius", "0.4rem");

        // Eingabefeld + Bestätigen (initial versteckt)
        TextField eingabe = new TextField();
        eingabe.setPlaceholder("Zielwert in €");
        eingabe.getStyle().set("width", "8rem").set("display", "none");

        Button okBtn = new Button("✓");
        okBtn.getStyle()
                .set("background", "#553722").set("color", "white").set("border", "none")
                .set("border-radius", "0.5rem").set("padding", "0.25rem 0.5rem")
                .set("cursor", "pointer").set("font-weight", "700").set("min-width", "unset")
                .set("display", "none");

        // Edit klicken → Eingabefeld einblenden
        editBtn.addClickListener(e -> {
            boolean editModus = eingabe.getStyle().get("display").equals("none");
            eingabe.getStyle().set("display", editModus ? "block" : "none");
            okBtn.getStyle().set("display", editModus ? "block" : "none");
            editBtn.getStyle().set("color", editModus ? "#553722" : "#82746d");
            if (editModus && aktuellerZielwert.compareTo(BigDecimal.ZERO) > 0) {
                eingabe.setValue(aktuellerZielwert.toPlainString().replace(".", ","));
            }
        });

        // Bestätigen → Wert speichern und Anzeige aktualisieren
        okBtn.addClickListener(e -> {
            String val = eingabe.getValue().trim().replace(",", ".");
            if (val.isBlank()) {
                onSpeichern.accept(BigDecimal.ZERO);
                anzeige.setText("Zielwert: –");
            } else {
                try {
                    BigDecimal wert = new BigDecimal(val);
                    if (wert.compareTo(BigDecimal.ZERO) < 0) {
                        Notification.show("Wert muss positiv sein.", 2000, Notification.Position.MIDDLE);
                        return;
                    }
                    onSpeichern.accept(wert);
                    anzeige.setText(formatZielwert(wert));
                } catch (NumberFormatException ex) {
                    Notification.show("Ungültiger Wert.", 2000, Notification.Position.MIDDLE);
                    return;
                }
            }
            // Zurück in Anzeigemodus
            eingabe.getStyle().set("display", "none");
            okBtn.getStyle().set("display", "none");
            editBtn.getStyle().set("color", "#82746d");
        });

        add(anzeige, editBtn, eingabe, okBtn);
    }

    /**
     * Formatiert den Zielwert als deutschen Währungsstring, z.B. {@code "Zielwert: 50,00€"}.
     * Gibt {@code "Zielwert: –"} zurück wenn der Wert null oder 0 ist.
     *
     * @param wert der zu formatierende Betrag
     * @return formatierter Anzeigetext
     */
    private String formatZielwert(BigDecimal wert) {
        if (wert == null || wert.compareTo(BigDecimal.ZERO) == 0) return "Zielwert: –";
        return String.format("Zielwert: %,.2f€", wert)
                .replace(",", "X").replace(".", ",").replace("X", ".");
    }
}
