package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.fhswf.kassensystem.views.components.FehlerUI;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Unterer Bereich der Warenkorb-Spalte mit Rabattzeile, Preisübersicht und Bezahlen-Button.
 *
 * <p>Zeigt Zwischensumme, MwSt 7%, MwSt 19%, optionale Rabattzeile und Gesamtbetrag.
 * Der Rabatt wird als Prozentsatz eingegeben und auf die Zwischensumme angewendet.
 *
 * <p>Über {@link #aktualisierePreise(List)} wird die Preisanzeige
 * nach jeder Warenkorb-Änderung aktualisiert.
 *
 * @author Adrian Krawietz
 */
class WarenkorbZusammenfassung extends VerticalLayout {

    private final Span gesamtBetragSpan  = new Span("0,00€");
    private final Span zwischensummeSpan = new Span("0,00€");
    private final Span mwst7Span         = new Span("0,00€");
    private final Span mwst19Span        = new Span("0,00€");
    private final Span rabattBetragSpan  = new Span("0,00€");
    private final Span rabattLabelSpan   = new Span("Rabatt (0%)");
    private final HorizontalLayout rabattAnzeigeZeile = new HorizontalLayout();

    private BigDecimal aktuellerRabattProzent = BigDecimal.ZERO;
    private final Runnable onRabattGeaendert;

    /**
     * Erstellt die Warenkorb-Zusammenfassung.
     *
     * @param onAbbrechen       wird beim Klick auf "Abbrechen" aufgerufen (Warenkorb leeren)
     * @param onBezahlen        wird beim Klick auf "Bezahlen" mit dem Gesamtbetragtext aufgerufen
     * @param onRabattGeaendert wird nach jeder Rabattänderung aufgerufen (Warenkorb-UI neu zeichnen)
     */
    WarenkorbZusammenfassung(Runnable onAbbrechen, Consumer<String> onBezahlen,
                             Runnable onRabattGeaendert) {
        this.onRabattGeaendert = onRabattGeaendert;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#e8e5ff").set("border-radius", "3rem 3rem 0 0")
                .set("padding", "1.5rem").set("gap", "0").set("flex-shrink", "0");

        // Rabatt-Anzeigezeile vorbereiten
        rabattAnzeigeZeile.setWidthFull();
        rabattAnzeigeZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        rabattAnzeigeZeile.setAlignItems(FlexComponent.Alignment.CENTER);
        rabattAnzeigeZeile.setPadding(false);
        rabattAnzeigeZeile.setVisible(false);

        rabattLabelSpan.getStyle()
                .set("font-size", "0.875rem").set("color", "#16a34a").set("font-weight", "600")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        rabattBetragSpan.getStyle()
                .set("font-size", "0.875rem").set("font-weight", "600").set("color", "#16a34a")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        rabattAnzeigeZeile.add(rabattLabelSpan, rabattBetragSpan);

        add(buildRabattZeile(), buildPreisZeilen(), buildAktionsButtons(onAbbrechen, onBezahlen));
    }

    // ═══════════════════════════════════════════════════════════
    // ÖFFENTLICHE API
    // ═══════════════════════════════════════════════════════════

    /**
     * Aktualisiert alle Preisanzeigen anhand der aktuellen Warenkorb-Einträge.
     * Berechnet MwSt 7%/19%, Rabattbetrag und Gesamtsumme.
     *
     * @param warenkorbListe aktuelle Warenkorb-Einträge
     */
    void aktualisierePreise(List<WarenkorbEintrag> warenkorbListe) {
        BigDecimal zwischensumme = BigDecimal.ZERO;
        BigDecimal mwst7total    = BigDecimal.ZERO;
        BigDecimal mwst19total   = BigDecimal.ZERO;

        for (WarenkorbEintrag e : warenkorbListe) {
            BigDecimal pos    = e.artikel.getPreis().multiply(BigDecimal.valueOf(e.menge));
            zwischensumme     = zwischensumme.add(pos);
            BigDecimal satz   = e.artikel.getMehrwertsteuer().getSatz();
            BigDecimal netto  = pos.divide(
                    BigDecimal.ONE.add(satz.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)), 4, RoundingMode.HALF_UP);
            BigDecimal mwstBetrag = pos.subtract(netto);
            if (satz.compareTo(BigDecimal.valueOf(7)) == 0) mwst7total  = mwst7total.add(mwstBetrag);
            else                                             mwst19total = mwst19total.add(mwstBetrag);
        }

        BigDecimal rabattBetrag = BigDecimal.ZERO;
        if (aktuellerRabattProzent.compareTo(BigDecimal.ZERO) > 0) {
            rabattBetrag = zwischensumme.multiply(aktuellerRabattProzent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        zwischensummeSpan.setText(format(zwischensumme));
        mwst7Span.setText(format(mwst7total));
        mwst19Span.setText(format(mwst19total));
        gesamtBetragSpan.setText(format(zwischensumme.subtract(rabattBetrag)));

        if (rabattBetrag.compareTo(BigDecimal.ZERO) > 0) {
            rabattLabelSpan.setText("Rabatt (" + aktuellerRabattProzent.toPlainString() + "%)");
            rabattBetragSpan.setText("- " + format(rabattBetrag));
            rabattAnzeigeZeile.setVisible(true);
        } else {
            rabattAnzeigeZeile.setVisible(false);
        }
    }

    /**
     * Setzt den Rabatt auf 0% zurück und blendet die Rabattzeile aus.
     * Wird beim Leeren des Warenkorbs aufgerufen.
     */
    void resetRabatt() {
        aktuellerRabattProzent = BigDecimal.ZERO;
        rabattAnzeigeZeile.setVisible(false);
    }

    /**
     * Gibt den aktuell eingestellten Rabattprozentsatz zurück.
     *
     * @return Rabatt in Prozent, oder {@code BigDecimal.ZERO} wenn kein Rabatt aktiv
     */
    BigDecimal getAktuellerRabattProzent() { return aktuellerRabattProzent; }

    /**
     * Gibt den aktuell angezeigten Gesamtbetrag als formatierten String zurück.
     *
     * @return Gesamtbetrag-Text (z.B. "12,99€")
     */
    String getGesamtBetragText() { return gesamtBetragSpan.getText(); }

    // ═══════════════════════════════════════════════════════════
    // UI-BUILDER
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt die Rabatteingabe-Zeile mit Textfeld und "Anwenden"-Button.
     * Validiert den eingegebenen Wert (0–100%) und zeigt Fehlermeldungen bei ungültiger Eingabe.
     */
    private HorizontalLayout buildRabattZeile() {
        TextField rabattFeld = new TextField();
        rabattFeld.setPlaceholder("Rabatt in %");
        rabattFeld.addClassName("rabatt-feld");
        rabattFeld.getStyle().set("flex", "1");

        Button anwendenBtn = new Button("Anwenden");
        anwendenBtn.getStyle()
                .set("background", "#e2e0fc").set("color", "#553722").set("font-weight", "700")
                .set("border", "none").set("border-radius", "0.75rem")
                .set("padding", "0.75rem 1.25rem").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("white-space", "nowrap").set("flex-shrink", "0");

        anwendenBtn.addClickListener(e -> {
            String val = rabattFeld.getValue().trim();
            if (val.isBlank()) {
                aktuellerRabattProzent = BigDecimal.ZERO;
                onRabattGeaendert.run();
                return;
            }
            try {
                BigDecimal pct = new BigDecimal(val.replace(",", "."));
                if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0) {
                    FehlerUI.fehler("Rabatt muss zwischen 0 und 100% liegen.");
                    return;
                }
                aktuellerRabattProzent = pct;
                FehlerUI.erfolg("Rabatt von " + pct.toPlainString() + "% wird angewendet.");
                onRabattGeaendert.run();
            } catch (NumberFormatException ex) {
                FehlerUI.fehler("Ungültiger Wert – bitte eine Zahl eingeben.");
            }
        });

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(false);
        row.getStyle().set("gap", "0.75rem").set("margin-bottom", "1.25rem");
        row.getElement().setAttribute("tour-id", "rabatt-zeile");
        row.add(rabattFeld, anwendenBtn);
        return row;
    }

    /**
     * Erstellt den Preisblock mit Zwischensumme, MwSt-Zeilen, Rabattzeile und Gesamtbetrag.
     */
    private VerticalLayout buildPreisZeilen() {
        VerticalLayout zeilen = new VerticalLayout();
        zeilen.setPadding(false);
        zeilen.setSpacing(false);
        zeilen.getStyle().set("gap", "0.4rem").set("margin-bottom", "1.25rem");

        zeilen.add(
                preisZeile("Zwischensumme", zwischensummeSpan),
                preisZeile("MwSt 7%",       mwst7Span),
                preisZeile("MwSt 19%",      mwst19Span),
                rabattAnzeigeZeile
        );

        Div linie = new Div();
        linie.getStyle().set("border-top", "1px solid rgba(85,55,34,0.1)").set("margin", "0.75rem 0");
        zeilen.add(linie);

        Span gesamtLabel = new Span("Gesamt");
        gesamtLabel.getStyle()
                .set("font-size", "1.1rem").set("font-weight", "700").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        gesamtBetragSpan.getStyle()
                .set("font-size", "2.25rem").set("font-weight", "900").set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em").set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout gesamtZeile = new HorizontalLayout();
        gesamtZeile.setWidthFull();
        gesamtZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        gesamtZeile.setAlignItems(FlexComponent.Alignment.BASELINE);
        gesamtZeile.setPadding(false);
        gesamtZeile.add(gesamtLabel, gesamtBetragSpan);
        zeilen.add(gesamtZeile);
        return zeilen;
    }

    /**
     * Erstellt eine einzelne Preiszeile mit Label und Betrag-Span.
     *
     * @param label      Beschriftungstext (z.B. "Zwischensumme")
     * @param betragSpan Span der den Betrag enthält und live aktualisiert wird
     */
    private HorizontalLayout preisZeile(String label, Span betragSpan) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        Span lbl = new Span(label);
        lbl.getStyle().set("font-size", "0.875rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        betragSpan.getStyle().set("font-size", "0.875rem").set("font-weight", "500").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        zeile.add(lbl, betragSpan);
        return zeile;
    }

    /**
     * Erstellt die Zeile mit "Abbrechen"- und "Bezahlen"-Button.
     *
     * @param onAbbrechen Callback für den Abbrechen-Button
     * @param onBezahlen  Callback für den Bezahlen-Button (erhält den Gesamtbetragtext)
     */
    private HorizontalLayout buildAktionsButtons(Runnable onAbbrechen, Consumer<String> onBezahlen) {
        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("flex", "1").set("padding", "1rem")
                .set("border", "2px solid #d4c3ba").set("background", "transparent")
                .set("border-radius", "1rem").set("font-weight", "700").set("color", "#1a1a2e")
                .set("cursor", "pointer").set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("font-size", "0.95rem");
        abbrechenBtn.addClickListener(e -> onAbbrechen.run());

        Span zahlungsIcon = new Span("payments");
        zahlungsIcon.addClassName("material-symbols-outlined");
        zahlungsIcon.getStyle().set("font-variation-settings", "'FILL' 1")
                .set("font-size", "1.2rem").set("line-height", "1").set("vertical-align", "middle");
        Span bezahlenText = new Span("Bezahlen");
        bezahlenText.getStyle().set("font-weight", "900").set("font-size", "1rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("line-height", "1");

        Button bezahlenBtn = new Button();
        bezahlenBtn.getElement().appendChild(zahlungsIcon.getElement());
        bezahlenBtn.getElement().appendChild(bezahlenText.getElement());
        bezahlenBtn.getStyle()
                .set("flex", "2").set("padding", "1rem 1.5rem")
                .set("background", "linear-gradient(to right, #553722, #6f4e37)")
                .set("color", "white").set("border", "none").set("border-radius", "1rem")
                .set("cursor", "pointer").set("display", "flex").set("flex-direction", "row")
                .set("align-items", "center").set("justify-content", "center").set("gap", "0.5rem")
                .set("box-shadow", "0 8px 25px rgba(85,55,34,0.3)");
        bezahlenBtn.getElement().setAttribute("tour-id", "bezahlen-btn");
        bezahlenBtn.addClickListener(e -> onBezahlen.accept(gesamtBetragSpan.getText()));

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setSpacing(false);
        buttons.getStyle().set("gap", "0.75rem");
        buttons.add(abbrechenBtn, bezahlenBtn);
        return buttons;
    }

    /**
     * Formatiert einen Betrag als deutschen Währungsstring (z.B. "12,99€").
     *
     * @param betrag der zu formatierende Betrag
     * @return formatierter String, oder "0,00€" bei null
     */
    static String format(BigDecimal betrag) {
        if (betrag == null) return "0,00€";
        return String.format(Locale.GERMANY, "%,.2f€", betrag);
    }
}