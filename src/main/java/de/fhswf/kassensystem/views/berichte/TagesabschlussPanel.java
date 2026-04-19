package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.model.dto.TagesabschlussDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

/**
 * Panel für den "Tagesabschluss"-Tab in der Berichte-View.
 *
 * <p>Zeigt drei Metric-Karten (Gesamtumsatz, Transaktionen, ø Bon-Wert) sowie
 * darunter das {@link ZahlungsartenPanel} und das {@link TopSellerPanel}.
 * Manager können zusätzlich den Bon-Zielwert über das {@link BonZielwertPanel} setzen.
 *
 * @author Adrian Krawietz
 */
class TagesabschlussPanel extends VerticalLayout {

    /**
     * Erstellt das Panel mit allen Metric-Karten und dem unteren Bereich.
     *
     * @param dto                  Tagesabschlussdaten
     * @param bonZielwert          aktuell gesetzter Bon-Zielwert
     * @param istManager           {@code true} wenn der Benutzer Manager ist (zeigt Zielwert-Editor)
     * @param onZielwertSpeichern  Callback zum Speichern des Zielwerts
     */
    TagesabschlussPanel(TagesabschlussDTO dto, BigDecimal bonZielwert,
                        boolean istManager, Consumer<BigDecimal> onZielwertSpeichern) {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "2rem");

        BigDecimal gesamtumsatz = BerichteUtils.safe(dto.getGesamtumsatz());
        BigDecimal umsatzBar    = BerichteUtils.safe(dto.getUmsatzBar());
        BigDecimal umsatzKarte  = BerichteUtils.safe(dto.getUmsatzKarte());
        int        trans        = dto.getAnzahlTransaktionen();
        BigDecimal bonWert      = trans > 0
                ? gesamtumsatz.divide(BigDecimal.valueOf(trans), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        add(buildMetricKarten(gesamtumsatz, trans, bonWert, bonZielwert, istManager, onZielwertSpeichern),
                buildUntererBereich(umsatzBar, umsatzKarte, dto));
    }

    /**
     * Erstellt die Zeile mit den drei Metric-Karten (Umsatz, Transaktionen, Bon-Wert).
     */
    private HorizontalLayout buildMetricKarten(BigDecimal gesamtumsatz, int trans, BigDecimal bonWert,
                                               BigDecimal bonZielwert, boolean istManager,
                                               Consumer<BigDecimal> onZielwertSpeichern) {
        HorizontalLayout karten = new HorizontalLayout();
        karten.setWidthFull();
        karten.setSpacing(false);
        karten.getStyle().set("gap", "1.5rem");
        karten.add(
                buildMetricKarte("Gesamtumsatz",  BerichteUtils.fp(gesamtumsatz), "payments",     "Tagesumsatz",         false),
                buildMetricKarte("Transaktionen", String.valueOf(trans),           "receipt_long", "Abgeschl. Verkaeufe",  false),
                buildBonWertKarte(bonWert, bonZielwert, istManager, onZielwertSpeichern)
        );
        return karten;
    }

    /**
     * Erstellt eine einzelne Metric-Karte mit Label, Wert, Icon und optionalem Subtext.
     *
     * @param label    Kartenüberschrift
     * @param wert     Hauptwert (groß dargestellt)
     * @param iconName Material-Symbols-Icon-Name für den Hintergrund
     * @param subtext  optionaler Hinweistext unterhalb des Werts (kann {@code null} sein)
     * @param positiv  {@code true} für grüne Subtext-Farbe
     */
    private VerticalLayout buildMetricKarte(String label, String wert, String iconName,
                                            String subtext, boolean positiv) {
        VerticalLayout karte = new VerticalLayout();
        karte.setPadding(false);
        karte.setSpacing(false);
        karte.getStyle()
                .set("flex", "1").set("background", "white").set("border-radius", "1.25rem")
                .set("padding", "2rem").set("gap", "0.5rem")
                .set("position", "relative").set("overflow", "hidden");

        Span bgIcon = icon(iconName);
        bgIcon.getStyle().set("position", "absolute").set("top", "0").set("right", "0")
                .set("font-size", "6rem").set("color", "#553722").set("opacity", "0.05")
                .set("pointer-events", "none").set("line-height", "1");

        Span lbl = new Span(label.toUpperCase());
        lbl.getStyle().set("font-size", "0.65rem").set("font-weight", "800")
                .set("letter-spacing", "0.1em").set("color", "rgba(85,55,34,0.6)")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("position", "relative");

        Span w = new Span(wert);
        w.getStyle().set("font-size", "3rem").set("font-weight", "900").set("color", "#553722")
                .set("letter-spacing", "-0.025em").set("line-height", "1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("margin-top", "0.5rem").set("position", "relative");

        karte.add(bgIcon, lbl, w);
        if (subtext != null) {
            Span sub = new Span(subtext);
            sub.getStyle().set("font-size", "0.8rem").set("font-weight", "700")
                    .set("color", positiv ? "#16a34a" : "#82746d")
                    .set("font-family", "'Plus Jakarta Sans', sans-serif");
            karte.add(sub);
        }
        return karte;
    }

    /**
     * Erstellt die Bon-Wert-Karte mit integriertem {@link BonZielwertPanel}.
     */
    private VerticalLayout buildBonWertKarte(BigDecimal bonWert, BigDecimal bonZielwert,
                                             boolean istManager, Consumer<BigDecimal> onZielwertSpeichern) {
        VerticalLayout karte = buildMetricKarte("ø Bon-Wert", BerichteUtils.fp(bonWert), "coffee", null, false);
        karte.add(new BonZielwertPanel(bonZielwert, istManager ? onZielwertSpeichern : null));
        return karte;
    }

    /**
     * Erstellt den unteren Bereich mit {@link ZahlungsartenPanel} (links/wachsend)
     * und {@link TopSellerPanel} (rechts/fixe Breite).
     */
    private HorizontalLayout buildUntererBereich(BigDecimal umsatzBar, BigDecimal umsatzKarte,
                                                 TagesabschlussDTO dto) {
        HorizontalLayout bereich = new HorizontalLayout();
        bereich.setWidthFull();
        bereich.setAlignItems(FlexComponent.Alignment.START);
        bereich.setSpacing(false);
        bereich.getStyle().set("gap", "3rem").set("flex-wrap", "wrap");

        // ZahlungsartenPanel wächst, TopSeller bleibt fix rechts
        ZahlungsartenPanel zahlungen = new ZahlungsartenPanel(umsatzBar, umsatzKarte);
        zahlungen.getStyle().set("flex", "1");

        TopSellerPanel topSeller = new TopSellerPanel(dto);
        // FIX: TopSeller nach ganz rechts schieben
        topSeller.getStyle().set("margin-left", "auto");

        bereich.add(zahlungen, topSeller);
        return bereich;
    }

    /**
     * Erstellt einen Material-Symbols-Icon-Span.
     *
     * @param name Icon-Name
     */
    private static Span icon(String name) {
        Span s = new Span(name);
        s.addClassName("material-symbols-outlined");
        s.getStyle().set("line-height", "1");
        return s;
    }
}