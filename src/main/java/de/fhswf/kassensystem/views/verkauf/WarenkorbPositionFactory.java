package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Baut eine einzelne Position im Warenkorb.
 * FIX: Löschen-Button setzt eintrag.menge = 0, damit VerkaufView den
 *      Eintrag korrekt aus der Liste entfernt.
 */
class WarenkorbPositionFactory {

    private WarenkorbPositionFactory() {}

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
        plusBtn.addClickListener(e -> { eintrag.menge++; onAenderung.run(); });

        HorizontalLayout kontrolle = new HorizontalLayout();
        kontrolle.setAlignItems(FlexComponent.Alignment.CENTER);
        kontrolle.setSpacing(false);
        kontrolle.getStyle()
                .set("background", "#efecff").set("border-radius", "9999px")
                .set("padding", "0.25rem").set("gap", "0.25rem");
        kontrolle.add(minusBtn, mengeSpan, plusBtn);
        return kontrolle;
    }

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

    private static Span buildGesamtSpan(String gesamt) {
        Span span = new Span(gesamt);
        span.getStyle()
                .set("width", "4rem").set("text-align", "right")
                .set("font-weight", "700").set("font-size", "0.9rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return span;
    }

    private static Button buildLoeschenButton(WarenkorbEintrag eintrag, Runnable onAenderung) {
        Span icon = new Span("delete");
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("color", "#d4c3ba").set("padding", "0.25rem").set("min-width", "unset");
        // FIX: menge auf 0 setzen → aktualisiereWarenkorbUI() entfernt den Eintrag dann
        btn.addClickListener(e -> {
            eintrag.menge = 0;
            onAenderung.run();
        });
        return btn;
    }

    private static String formatPreis(java.math.BigDecimal betrag) {
        return String.format("%,.2f€", betrag)
                .replace(",", "X").replace(".", ",").replace("X", ".");
    }
}