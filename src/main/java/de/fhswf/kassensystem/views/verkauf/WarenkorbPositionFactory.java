package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Fabrikklasse für einzelne Positionen im Warenkorb der Kassier-View.
 *
 * <p>Jede Position zeigt Artikelname, Einzelpreis, Mengenkontrolle (−/+),
 * Gesamtpreis und Löschen-Button.
 *
 * @author Adrian Krawietz & Paula Martin
 */
class WarenkorbPositionFactory {

    private WarenkorbPositionFactory() {}

    /**
     * Erstellt eine vollständig gestylte Warenkorb-Zeile.
     *
     * @param eintrag     der darzustellende Warenkorb-Eintrag
     * @param zebra       {@code true} für abwechselnden Zeilenhintergrund
     * @param onAenderung wird nach jeder Mengenänderung oder Löschung aufgerufen
     * @return fertig gestyltes Positions-Layout
     */
    static HorizontalLayout create(WarenkorbEintrag eintrag, boolean zebra, Runnable onAenderung) {
        String gesamtText = formatPreis(eintrag.artikel.getPreis()
                .multiply(java.math.BigDecimal.valueOf(eintrag.menge)));
        String einzelText = formatPreis(eintrag.artikel.getPreis());

        HorizontalLayout position = new HorizontalLayout();
        position.setWidthFull();
        position.setAlignItems(FlexComponent.Alignment.CENTER);
        position.setSpacing(false);
        position.getStyle()
                .set("background", zebra ? "#f5f2ff" : "white")
                .set("border-radius", "1rem").set("padding", "1rem").set("gap", "0.75rem");

        position.add(
                buildInfo(eintrag.artikel.getName(), einzelText),
                buildMengeKontrolle(eintrag, onAenderung),
                buildGesamtSpan(gesamtText),
                buildLoeschenButton(eintrag, onAenderung)
        );
        return position;
    }

    /**
     * Erstellt den Infoblock mit Artikelname und Einzelpreis.
     */
    private static VerticalLayout buildInfo(String name, String einzelPreis) {
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("flex", "1");

        H4 artikelName = new H4(name);
        artikelName.getStyle()
                .set("margin", "0").set("font-size", "0.9rem").set("font-weight", "700")
                .set("color", "#1a1a2e").set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph einzelPreisLabel = new Paragraph("Einzel: " + einzelPreis);
        einzelPreisLabel.getStyle()
                .set("margin", "0").set("font-size", "0.7rem").set("color", "#d4c3ba")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(artikelName, einzelPreisLabel);
        return info;
    }

    /**
     * Erstellt die Mengenkontrolle mit Minus-, Anzeige- und Plus-Element.
     * Plus prüft ob der Bestand ausreicht; Minus stoppt bei 1.
     *
     * @param eintrag     Eintrag dessen Menge verändert wird
     * @param onAenderung Callback nach jeder Mengenänderung
     */
    private static HorizontalLayout buildMengeKontrolle(WarenkorbEintrag eintrag, Runnable onAenderung) {
        Span mengeSpan = new Span(String.valueOf(eintrag.menge));
        mengeSpan.getStyle()
                .set("width", "2rem").set("text-align", "center")
                .set("font-weight", "700").set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button minusBtn = buildMengeButton("remove");
        minusBtn.addClickListener(e -> {
            if (eintrag.menge > 1) eintrag.menge--;
            onAenderung.run();
        });

        Button plusBtn = buildMengeButton("add");
        plusBtn.addClickListener(e -> {
            if (eintrag.artikel.getBestand() < 999 && eintrag.menge >= eintrag.artikel.getBestand()) {
                com.vaadin.flow.component.notification.Notification.show(
                        "Nicht mehr Bestand vorhanden als bereits im Warenkorb.",
                        2500, Notification.Position.MIDDLE);
                return;
            }
            eintrag.menge++;
            onAenderung.run();

        });

        HorizontalLayout kontrolle = new HorizontalLayout();
        kontrolle.setAlignItems(FlexComponent.Alignment.CENTER);
        kontrolle.setSpacing(false);
        kontrolle.getStyle()
                .set("background", "#efecff").set("border-radius", "9999px")
                .set("padding", "0.25rem").set("gap", "0.25rem");
        kontrolle.add(minusBtn, mengeSpan, plusBtn);
        return kontrolle;
    }

    /**
     * Erstellt einen runden Mengen-Button (+ oder −) mit Material-Icon.
     *
     * @param iconName "add" oder "remove"
     */
    private static Button buildMengeButton(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("font-size", "1rem").set("line-height", "1")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("width", "2rem").set("height", "2rem").set("min-width", "2rem")
                .set("border-radius", "9999px").set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("color", "#553722").set("padding", "0");
        return btn;
    }

    /**
     * Erstellt den rechtsbündigen Gesamtpreis-Span.
     *
     * @param gesamt formatierter Gesamtbetrag (z.B. "2,97€")
     */
    private static Span buildGesamtSpan(String gesamt) {
        Span span = new Span(gesamt);
        span.getStyle()
                .set("width", "4rem").set("text-align", "right")
                .set("font-weight", "700").set("font-size", "0.9rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return span;
    }

    /**
     * Erstellt den Löschen-Button. Setzt {@code eintrag.menge = 0} statt {@code remove()},
     * damit {@link VerkaufView} den Eintrag via {@code removeIf(e -> e.menge <= 0)} entfernt.
     */
    private static Button buildLoeschenButton(WarenkorbEintrag eintrag, Runnable onAenderung) {
        Span icon = new Span("delete");
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("color", "#d4c3ba").set("padding", "0.25rem").set("min-width", "unset");
        btn.addClickListener(e -> {
            eintrag.menge = 0;
            onAenderung.run();
        });
        return btn;
    }

    /**
     * Formatiert einen Betrag als deutschen Währungsstring (z.B. "1,99€").
     *
     * @param betrag der zu formatierende Betrag
     * @return formatierter String
     */
    private static String formatPreis(java.math.BigDecimal betrag) {
        return String.format("%,.2f€", betrag)
                .replace(",", "X").replace(".", ",").replace("X", ".");
    }
}