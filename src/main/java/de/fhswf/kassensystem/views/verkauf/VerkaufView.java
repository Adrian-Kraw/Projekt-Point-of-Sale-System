package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.service.PdfExportService;
import de.fhswf.kassensystem.service.VerkaufService;
import de.fhswf.kassensystem.views.MainLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * VerkaufView ist die Kassier-Ansicht (Point of Sale).
 *
 * Aufbau: Zwei-Spalten-Layout
 * - Linke Spalte: Artikelauswahl mit Kategorie-Filter und Artikel-Grid (aus DB)
 * - Rechte Spalte: Warenkorb mit Positionen, MwSt, Gesamt, Bezahlen
 *
 * Session-State:
 * - aktuellerVerkauf: der laufende Verkauf in der DB
 * - warenkorbPositionen: lokale Liste für die UI (sync mit DB)
 */
@RolesAllowed({"KASSIERER", "MANAGER"})
@Route(value = "kassieren", layout = MainLayout.class)
public class VerkaufView extends HorizontalLayout implements BeforeEnterObserver {

    private final ArtikelService artikelService;
    private final VerkaufService verkaufService;
    private final PdfExportService pdfExportService;

    /*
     * Session-State des aktuellen Verkaufs.
     * aktuellerVerkauf wird beim ersten Artikel-Hinzufügen angelegt.
     */
    private Verkauf aktuellerVerkauf = null;
    private final List<WarenkorbEintrag> warenkorbListe = new ArrayList<>();

    /*
     * Instanzfelder für dynamische Bereiche.
     */
    private final VerticalLayout warenkorbPositionenLayout = new VerticalLayout();
    private final Span gesamtBetragSpan                   = new Span("0,00€");
    private final Span zwischensummeSpan                  = new Span("0,00€");
    private final Span mwst7Span                          = new Span("0,00€");
    private final Span mwst19Span                         = new Span("0,00€");
    private final Span rabattSpan                         = new Span("0,00€");
    private       BigDecimal aktuellerRabattProzent       = BigDecimal.ZERO;
    private String aktiveKategorie                        = "Alle";
    private final Div artikelGridDiv                      = new Div();
    private String aktuelleSuche                          = "";

    /**
     * Hilfsdatenklasse für einen Warenkorb-Eintrag.
     */
    private static class WarenkorbEintrag {
        Artikel artikel;
        int     menge;

        WarenkorbEintrag(Artikel artikel, int menge) {
            this.artikel = artikel;
            this.menge   = menge;
        }
    }

    public VerkaufView(ArtikelService artikelService, VerkaufService verkaufService,
                       PdfExportService pdfExportService) {
        this.artikelService = artikelService;
        this.verkaufService = verkaufService;
        this.pdfExportService = pdfExportService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("overflow", "hidden").set("height", "100vh");

        warenkorbPositionenLayout.setPadding(false);
        warenkorbPositionenLayout.setSpacing(false);
        warenkorbPositionenLayout.getStyle()
                .set("padding", "0 1.5rem")
                .set("gap", "0.25rem")
                .set("flex", "1")
                .set("overflow-y", "auto");

        add(buildArtikelSpalte(), buildWarenkorbSpalte());
    }

    // ═══════════════════════════════════════════════════════════
    // LINKE SPALTE – ARTIKELAUSWAHL
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildArtikelSpalte() {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
        spalte.getStyle()
                .set("flex", "0 0 60%")
                .set("max-width", "55%")
                .set("background", "#fcf8ff")
                .set("overflow-y", "auto")
                .set("height", "100%")
                .set("padding", "2rem")
                .set("box-sizing", "border-box");

        artikelGridDiv.addClassName("artikel-grid");
        artikelGridDiv.getStyle()
                .set("width", "100%")
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "1.25rem");

        ladeArtikelGrid();

        spalte.add(buildSuchfeld(), buildKategorieFilter(), artikelGridDiv);

        spalte.getElement().executeJs(
                "const observer = new ResizeObserver(entries => {" +
                        "  for (const entry of entries) {" +
                        "    const w = entry.contentRect.width;" +
                        "    const grid = this.querySelector('.artikel-grid');" +
                        "    if (!grid) return;" +
                        "    grid.style.gridTemplateColumns = w < 600 ? 'repeat(2, 1fr)' : 'repeat(3, 1fr)';" +
                        "  }" +
                        "});" +
                        "observer.observe(this);"
        );

        return spalte;
    }

    /**
     * Lädt Artikel aus DB und befüllt das Grid.
     * Filtert nach aktiver Kategorie und Suchbegriff.
     */
    private void ladeArtikelGrid() {
        artikelGridDiv.removeAll();

        List<Artikel> alle = aktuelleSuche.isBlank()
                ? artikelService.findAllArtikel()
                : artikelService.findByName(aktuelleSuche);

        for (Artikel a : alle) {
            // Kategorie-Filter
            if (!aktiveKategorie.equals("Alle") &&
                    !a.getKategorie().getName().equals(aktiveKategorie)) continue;
            // Nur aktive Artikel
            if (!a.isAktiv()) continue;

            boolean ausverkauft = a.getBestand() == 0;
            artikelGridDiv.add(buildArtikelKarte(a, ausverkauft));
        }
    }

    private HorizontalLayout buildSuchfeld() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("margin-bottom", "1.5rem");

        TextField search = new TextField();
        search.setWidthFull();
        search.setPlaceholder("Artikel suchen...");
        search.setPrefixComponent(createIcon("search"));
        search.addValueChangeListener(e -> {
            aktuelleSuche = e.getValue();
            ladeArtikelGrid();
        });

        row.add(search);
        return row;
    }

    /**
     * Kategorie-Chips dynamisch aus DB laden.
     */
    private HorizontalLayout buildKategorieFilter() {
        HorizontalLayout filter = new HorizontalLayout();
        filter.setSpacing(false);
        filter.getStyle()
                .set("gap", "0.75rem")
                .set("margin-bottom", "2rem")
                .set("flex-wrap", "wrap");

        // "Alle"-Chip immer zuerst
        filter.add(buildKategorieChip("Alle", true, filter));

        // Kategorien aus den aktiven Artikeln ermitteln
        List<Artikel> alle = artikelService.findAllArtikel();
        alle.stream()
                .filter(Artikel::isAktiv)
                .map(a -> a.getKategorie().getName())
                .distinct()
                .sorted()
                .forEach(katName -> filter.add(buildKategorieChip(katName, false, filter)));

        return filter;
    }

    private Button buildKategorieChip(String label, boolean aktiv,
                                      HorizontalLayout filterLayout) {
        Button chip = new Button(label);
        chip.getStyle()
                .set("border-radius", "9999px")
                .set("padding", "0.625rem 1.5rem")
                .set("font-size", "0.875rem")
                .set("font-weight", aktiv ? "700" : "500")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("background", aktiv ? "#553722" : "#e8e5ff")
                .set("color", aktiv ? "white" : "#50453e")
                .set("box-shadow", aktiv ? "0 4px 15px rgba(85,55,34,0.2)" : "none");

        chip.addClickListener(e -> {
            aktiveKategorie = label;
            // Alle Chips zurücksetzen
            filterLayout.getChildren().forEach(c -> {
                if (c instanceof Button b) {
                    boolean istAktiv = b.getText().equals(label);
                    b.getStyle()
                            .set("background", istAktiv ? "#553722" : "#e8e5ff")
                            .set("color", istAktiv ? "white" : "#50453e")
                            .set("font-weight", istAktiv ? "700" : "500")
                            .set("box-shadow", istAktiv ? "0 4px 15px rgba(85,55,34,0.2)" : "none");
                }
            });
            ladeArtikelGrid();
        });

        return chip;
    }

    private Div buildArtikelKarte(Artikel artikel, boolean ausverkauft) {
        Div karte = new Div();
        karte.getStyle()
                .set("background", ausverkauft ? "rgba(239,236,255,0.5)" : "white")
                .set("border-radius", "1.25rem")
                .set("padding", "1.25rem")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("cursor", ausverkauft ? "not-allowed" : "pointer")
                .set("transition", "all 0.2s")
                .set("opacity", ausverkauft ? "0.6" : "1")
                .set("filter", ausverkauft ? "grayscale(1)" : "none")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.04)");

        // Icon-Bereich: Artikelbild wenn vorhanden, sonst Icon
        String iconName = iconFuerKategorie(artikel.getKategorie().getName());
        Div iconWrapper = new Div();
        iconWrapper.getStyle()
                .set("width", "100%").set("height", "7rem")
                .set("background", "#efecff").set("border-radius", "0.75rem")
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("margin-bottom", "1rem")
                .set("position", "relative").set("overflow", "hidden");

        if (artikel.getBild() != null && artikel.getBild().length > 0) {
            // Artikelbild als Base64 anzeigen (kein deprecated StreamResource)
            String base64 = java.util.Base64.getEncoder().encodeToString(artikel.getBild());
            com.vaadin.flow.component.html.Image img =
                    new com.vaadin.flow.component.html.Image(
                            "data:image/jpeg;base64," + base64, artikel.getName());
            img.getStyle()
                    .set("width", "100%").set("height", "100%")
                    .set("object-fit", "cover").set("border-radius", "0.75rem");
            iconWrapper.add(img);
        } else {
            Span icon = createIcon(iconName);
            icon.getStyle().set("font-size", "3rem").set("color", "#553722");
            iconWrapper.add(icon);
        }

        if (ausverkauft) {
            Div badge = new Div(new Span("Ausverkauft"));
            badge.getStyle()
                    .set("position", "absolute").set("top", "0.5rem").set("left", "0.5rem")
                    .set("background", "white").set("border-radius", "9999px")
                    .set("padding", "0.25rem 0.75rem");
            ((Span) badge.getChildren().findFirst().get()).getStyle()
                    .set("font-size", "0.625rem").set("font-weight", "700")
                    .set("text-transform", "uppercase").set("color", "#1a1a2e");
            iconWrapper.add(badge);
        }

        H3 name = new H3(artikel.getName());
        name.getStyle()
                .set("margin", "0 0 0.5rem 0").set("font-size", "1rem")
                .set("font-weight", "700").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout preisBestand = new HorizontalLayout();
        preisBestand.setWidthFull();
        preisBestand.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        preisBestand.setAlignItems(FlexComponent.Alignment.CENTER);
        preisBestand.setPadding(false);
        preisBestand.setSpacing(false);
        preisBestand.getStyle().set("margin-top", "auto");

        Span preisSpan = new Span(String.format("%,.2f€", artikel.getPreis())
                .replace(",", "X").replace(".", ",").replace("X", "."));
        preisSpan.getStyle()
                .set("font-size", "1.25rem").set("font-weight", "800")
                .set("color", ausverkauft ? "#82746d" : "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        /*
         * Bestand: 999 = "∞" (Getränke die nicht gezählt werden müssen).
         * Für alles andere zeigen wir den echten Bestand.
         */
        String bestandText = artikel.getBestand() >= 999 ? "∞" : "Bestand: " + artikel.getBestand();
        Span bestandSpan = new Span(bestandText);
        bestandSpan.getStyle()
                .set("font-size", "0.7rem").set("font-weight", "500").set("color", "#82746d")
                .set("background", "#efecff").set("padding", "0.2rem 0.6rem")
                .set("border-radius", "0.5rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        preisBestand.add(preisSpan, bestandSpan);
        karte.add(iconWrapper, name, preisBestand);

        // Klick → Artikel zum Warenkorb hinzufügen
        if (!ausverkauft) {
            karte.addClickListener(e -> artikelZumKorbHinzufuegen(artikel));

            karte.getElement().executeJs(
                    "this.addEventListener('mouseenter', () => {" +
                            "  this.style.transform = 'translateY(-2px)';" +
                            "  this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.10)';" +
                            "});" +
                            "this.addEventListener('mouseleave', () => {" +
                            "  this.style.transform = 'none';" +
                            "  this.style.boxShadow = '0 2px 8px rgba(0,0,0,0.04)';" +
                            "});"
            );
        }

        return karte;
    }

    /**
     * Gibt ein passendes Material Symbol Icon für eine Kategorie zurück.
     */
    private String iconFuerKategorie(String kategorie) {
        return switch (kategorie.toLowerCase()) {
            case "heißgetränke" -> "local_cafe";
            case "kaltgetränke" -> "water_drop";
            case "gebäck"       -> "bakery_dining";
            case "snacks"       -> "lunch_dining";
            default             -> "category";
        };
    }

    // ═══════════════════════════════════════════════════════════
    // WARENKORB LOGIK
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt einen Artikel zum lokalen Warenkorb hinzu.
     * Erhöht die Menge wenn der Artikel bereits im Korb ist.
     */
    private void artikelZumKorbHinzufuegen(Artikel artikel) {
        // Prüfen ob schon im Korb
        for (WarenkorbEintrag e : warenkorbListe) {
            if (e.artikel.getId().equals(artikel.getId())) {
                e.menge++;
                aktualisiereWarenkorbUI();
                return;
            }
        }
        // Neu hinzufügen
        warenkorbListe.add(new WarenkorbEintrag(artikel, 1));
        aktualisiereWarenkorbUI();
    }

    /**
     * Aktualisiert die Warenkorb-UI komplett neu.
     */
    private void aktualisiereWarenkorbUI() {
        warenkorbPositionenLayout.removeAll();

        boolean zebra = false;
        for (WarenkorbEintrag e : warenkorbListe) {
            BigDecimal gesamt = e.artikel.getPreis()
                    .multiply(BigDecimal.valueOf(e.menge));
            String gesamtText = String.format("%,.2f€", gesamt)
                    .replace(",", "X").replace(".", ",").replace("X", ".");
            String einzelText = String.format("%,.2f€", e.artikel.getPreis())
                    .replace(",", "X").replace(".", ",").replace("X", ".");

            warenkorbPositionenLayout.add(
                    buildWarenkorbPosition(e.artikel.getName(), einzelText,
                            e.menge, gesamtText, zebra, e));
            zebra = !zebra;
        }

        aktualisierePreise();
    }

    /**
     * Berechnet und aktualisiert alle Preis-Anzeigen inkl. Rabatt.
     */
    private void aktualisierePreise() {
        BigDecimal zwischensumme = BigDecimal.ZERO;
        BigDecimal mwst7total    = BigDecimal.ZERO;
        BigDecimal mwst19total   = BigDecimal.ZERO;

        for (WarenkorbEintrag e : warenkorbListe) {
            BigDecimal pos = e.artikel.getPreis()
                    .multiply(BigDecimal.valueOf(e.menge));
            zwischensumme = zwischensumme.add(pos);

            BigDecimal satz = e.artikel.getMehrwertsteuer().getSatz();
            BigDecimal netto = pos.divide(
                    BigDecimal.ONE.add(satz.divide(BigDecimal.valueOf(100))),
                    4, RoundingMode.HALF_UP);
            BigDecimal mwstBetrag = pos.subtract(netto);

            if (satz.compareTo(BigDecimal.valueOf(7)) == 0) {
                mwst7total = mwst7total.add(mwstBetrag);
            } else {
                mwst19total = mwst19total.add(mwstBetrag);
            }
        }

        // Rabatt berechnen
        BigDecimal rabattBetrag = BigDecimal.ZERO;
        if (aktuellerRabattProzent.compareTo(BigDecimal.ZERO) > 0) {
            rabattBetrag = zwischensumme
                    .multiply(aktuellerRabattProzent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        BigDecimal gesamt = zwischensumme.subtract(rabattBetrag);

        zwischensummeSpan.setText(formatPreis(zwischensumme));
        mwst7Span.setText(formatPreis(mwst7total));
        mwst19Span.setText(formatPreis(mwst19total));
        rabattSpan.setText(rabattBetrag.compareTo(BigDecimal.ZERO) > 0
                ? "- " + formatPreis(rabattBetrag) : "0,00€");
        gesamtBetragSpan.setText(formatPreis(gesamt));
    }

    private String formatPreis(BigDecimal betrag) {
        return String.format("%,.2f€", betrag)
                .replace(",", "X").replace(".", ",").replace("X", ".");
    }

    // ═══════════════════════════════════════════════════════════
    // RECHTE SPALTE – WARENKORB
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildWarenkorbSpalte() {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
        spalte.getStyle()
                .set("flex", "1")
                .set("background", "#f5f2ff")
                .set("min-height", "100vh")
                .set("height", "100%")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("overflow", "hidden");

        spalte.add(
                buildWarenkorbHeader(),
                warenkorbPositionenLayout,
                buildBestellZusammenfassung()
        );
        return spalte;
    }

    private HorizontalLayout buildWarenkorbHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(false);
        header.getStyle().set("padding", "2rem 2rem 1rem 2rem");

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "0.75rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "rgba(85,55,34,0.1)").set("border-radius", "0.75rem")
                .set("padding", "0.75rem").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");
        Span receiptIcon = createIcon("receipt");
        receiptIcon.getStyle().set("color", "#553722");
        iconBox.add(receiptIcon);

        H2 titel = new H2("Warenkorb");
        titel.getStyle()
                .set("margin", "0").set("font-size", "1.25rem").set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);

        Button loeschenBtn = new Button();
        loeschenBtn.getElement().appendChild(createIcon("delete_sweep").getElement());
        loeschenBtn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("color", "#82746d").set("padding", "0.5rem")
                .set("border-radius", "9999px").set("min-width", "unset");
        loeschenBtn.addClickListener(e -> {
            warenkorbListe.clear();
            aktuellerVerkauf = null;
            aktualisiereWarenkorbUI();
        });

        // Kassenbon-Button
        Button kassenbonBtn = new Button();
        kassenbonBtn.getElement().appendChild(createIcon("receipt_long").getElement());
        kassenbonBtn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("color", "#553722").set("padding", "0.5rem")
                .set("border-radius", "9999px").set("min-width", "unset");
        kassenbonBtn.getElement().setAttribute("title", "Kassenbon drucken");
        kassenbonBtn.addClickListener(e -> druckeKassenbon());

        HorizontalLayout aktionenGruppe = new HorizontalLayout();
        aktionenGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        aktionenGruppe.setSpacing(false);
        aktionenGruppe.getStyle().set("gap", "0.25rem");
        aktionenGruppe.add(kassenbonBtn, loeschenBtn);

        header.add(titelGruppe, aktionenGruppe);
        return header;
    }

    private HorizontalLayout buildWarenkorbPosition(String name, String einzelPreis,
                                                    int menge, String gesamt,
                                                    boolean zebra,
                                                    WarenkorbEintrag eintrag) {
        HorizontalLayout position = new HorizontalLayout();
        position.setWidthFull();
        position.setAlignItems(FlexComponent.Alignment.CENTER);
        position.setSpacing(false);
        position.getStyle()
                .set("background", zebra ? "#f5f2ff" : "white")
                .set("border-radius", "1rem").set("padding", "1rem").set("gap", "0.75rem");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("flex", "1");

        H4 artikelName = new H4(name);
        artikelName.getStyle()
                .set("margin", "0").set("font-size", "0.9rem").set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph einzelPreisLabel = new Paragraph("Einzel: " + einzelPreis);
        einzelPreisLabel.getStyle()
                .set("margin", "0").set("font-size", "0.7rem").set("color", "#d4c3ba")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(artikelName, einzelPreisLabel);

        // Mengenkontrolle
        HorizontalLayout mengeKontrolle = new HorizontalLayout();
        mengeKontrolle.setAlignItems(FlexComponent.Alignment.CENTER);
        mengeKontrolle.setSpacing(false);
        mengeKontrolle.getStyle()
                .set("background", "#efecff").set("border-radius", "9999px")
                .set("padding", "0.25rem").set("gap", "0.25rem");

        Span mengeSpan = new Span(String.valueOf(menge));
        mengeSpan.getStyle()
                .set("width", "2rem").set("text-align", "center")
                .set("font-weight", "700").set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button minusBtn = buildMengeButton("remove");
        minusBtn.addClickListener(e -> {
            if (eintrag.menge > 1) {
                eintrag.menge--;
            } else {
                warenkorbListe.remove(eintrag);
            }
            aktualisiereWarenkorbUI();
        });

        Button plusBtn = buildMengeButton("add");
        plusBtn.addClickListener(e -> {
            eintrag.menge++;
            aktualisiereWarenkorbUI();
        });

        mengeKontrolle.add(minusBtn, mengeSpan, plusBtn);

        Span gesamtSpan = new Span(gesamt);
        gesamtSpan.getStyle()
                .set("width", "4rem").set("text-align", "right")
                .set("font-weight", "700").set("font-size", "0.9rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button deleteBtn = new Button();
        deleteBtn.getElement().appendChild(createIcon("delete").getElement());
        deleteBtn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("color", "#d4c3ba").set("padding", "0.25rem").set("min-width", "unset");
        deleteBtn.addClickListener(e -> {
            warenkorbListe.remove(eintrag);
            aktualisiereWarenkorbUI();
        });

        position.add(info, mengeKontrolle, gesamtSpan, deleteBtn);
        return position;
    }

    private Button buildMengeButton(String iconName) {
        Button btn = new Button();
        Span icon = createIcon(iconName);
        icon.getStyle().set("font-size", "1rem").set("line-height", "1")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("width", "2rem").set("height", "2rem").set("min-width", "2rem")
                .set("border-radius", "9999px").set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("color", "#553722").set("padding", "0");
        return btn;
    }

    private VerticalLayout buildBestellZusammenfassung() {
        VerticalLayout zusammenfassung = new VerticalLayout();
        zusammenfassung.setWidthFull();
        zusammenfassung.setPadding(false);
        zusammenfassung.setSpacing(false);
        zusammenfassung.getStyle()
                .set("background", "#e8e5ff").set("border-radius", "3rem 3rem 0 0")
                .set("padding", "1.5rem").set("gap", "0").set("flex-shrink", "0");

        zusammenfassung.add(buildRabattZeile(), buildPreisZeilen(), buildAktionsButtons());
        return zusammenfassung;
    }

    private HorizontalLayout buildRabattZeile() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(false);
        row.getStyle().set("gap", "0.75rem").set("margin-bottom", "1.25rem");

        TextField rabattFeld = new TextField();
        rabattFeld.setPlaceholder("Rabatt in %");
        rabattFeld.addClassName("rabatt-feld");
        rabattFeld.getStyle().set("flex", "1");
        if (aktuellerRabattProzent.compareTo(BigDecimal.ZERO) > 0) {
            rabattFeld.setValue(aktuellerRabattProzent.toPlainString());
        }

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
                aktualisiereWarenkorbUI();
                return;
            }
            try {
                BigDecimal pct = new BigDecimal(val.replace(",", "."));
                if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0) {
                    Notification.show("Rabatt muss zwischen 0 und 100% liegen.",
                            3000, Notification.Position.MIDDLE);
                    return;
                }
                aktuellerRabattProzent = pct;
                Notification.show("Rabatt von " + pct.toPlainString() + "% wird angewendet.",
                        2000, Notification.Position.BOTTOM_START);
                aktualisiereWarenkorbUI();
            } catch (NumberFormatException ex) {
                Notification.show("Ungültiger Wert – bitte eine Zahl eingeben.",
                        3000, Notification.Position.MIDDLE);
            }
        });

        row.add(rabattFeld, anwendenBtn);
        return row;
    }

    private VerticalLayout buildPreisZeilen() {
        VerticalLayout zeilen = new VerticalLayout();
        zeilen.setPadding(false);
        zeilen.setSpacing(false);
        zeilen.getStyle().set("gap", "0.4rem").set("margin-bottom", "1.25rem");

        zeilen.add(
                buildPreisZeile("Zwischensumme", zwischensummeSpan),
                buildPreisZeile("MwSt 7%",       mwst7Span),
                buildPreisZeile("MwSt 19%",       mwst19Span),
                buildPreisZeileRabatt()           // Rabatt-Zeile
        );

        Div trennlinie = new Div();
        trennlinie.getStyle()
                .set("border-top", "1px solid rgba(85,55,34,0.1)").set("margin", "0.75rem 0");
        zeilen.add(trennlinie);

        HorizontalLayout gesamtZeile = new HorizontalLayout();
        gesamtZeile.setWidthFull();
        gesamtZeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        gesamtZeile.setAlignItems(FlexComponent.Alignment.BASELINE);
        gesamtZeile.setPadding(false);

        Span gesamtLabel = new Span("Gesamt");
        gesamtLabel.getStyle()
                .set("font-size", "1.1rem").set("font-weight", "700").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        gesamtBetragSpan.getStyle()
                .set("font-size", "2.25rem").set("font-weight", "900").set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        gesamtZeile.add(gesamtLabel, gesamtBetragSpan);
        zeilen.add(gesamtZeile);
        return zeilen;
    }

    private HorizontalLayout buildPreisZeile(String label, Span betragSpan) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.875rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        betragSpan.getStyle()
                .set("font-size", "0.875rem").set("font-weight", "500").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        zeile.add(labelSpan, betragSpan);
        return zeile;
    }

    /**
     * Rabatt-Zeile – nur sichtbar wenn ein Rabatt gesetzt ist.
     */
    private HorizontalLayout buildPreisZeileRabatt() {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);

        Span labelSpan = new Span("Rabatt (" + aktuellerRabattProzent.toPlainString() + "%)");
        labelSpan.getStyle()
                .set("font-size", "0.875rem").set("color", "#16a34a")
                .set("font-weight", "600")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        rabattSpan.getStyle()
                .set("font-size", "0.875rem").set("font-weight", "600").set("color", "#16a34a")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        zeile.add(labelSpan, rabattSpan);
        // Nur anzeigen wenn Rabatt > 0
        zeile.setVisible(aktuellerRabattProzent.compareTo(BigDecimal.ZERO) > 0);
        return zeile;
    }

    private HorizontalLayout buildAktionsButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setSpacing(false);
        buttons.getStyle().set("gap", "0.75rem");

        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("flex", "1").set("padding", "1rem")
                .set("border", "2px solid #d4c3ba").set("background", "transparent")
                .set("border-radius", "1rem").set("font-weight", "700").set("color", "#1a1a2e")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("font-size", "0.95rem");
        abbrechenBtn.addClickListener(e -> {
            warenkorbListe.clear();
            aktuellerVerkauf = null;
            aktualisiereWarenkorbUI();
        });

        Button bezahlenBtn = new Button();
        Span zahlungsIcon = createIcon("payments");
        zahlungsIcon.getStyle()
                .set("font-variation-settings", "'FILL' 1")
                .set("font-size", "1.2rem").set("line-height", "1").set("vertical-align", "middle");
        Span bezahlenText = new Span("Bezahlen");
        bezahlenText.getStyle()
                .set("font-weight", "900").set("font-size", "1rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("line-height", "1");

        bezahlenBtn.getElement().appendChild(zahlungsIcon.getElement());
        bezahlenBtn.getElement().appendChild(bezahlenText.getElement());
        bezahlenBtn.getStyle()
                .set("flex", "2").set("padding", "1rem 1.5rem")
                .set("background", "linear-gradient(to right, #553722, #6f4e37)")
                .set("color", "white").set("border", "none").set("border-radius", "1rem")
                .set("cursor", "pointer").set("display", "flex")
                .set("flex-direction", "row").set("align-items", "center")
                .set("justify-content", "center").set("gap", "0.5rem")
                .set("box-shadow", "0 8px 25px rgba(85,55,34,0.3)");

        bezahlenBtn.addClickListener(e -> {
            if (warenkorbListe.isEmpty()) {
                Notification.show("Warenkorb ist leer.", 2000, Notification.Position.MIDDLE);
                return;
            }
            oeffneZahlungsDialog();
        });

        buttons.add(abbrechenBtn, bezahlenBtn);
        return buttons;
    }

    // ═══════════════════════════════════════════════════════════
    // ZAHLUNGS-DIALOG
    // ═══════════════════════════════════════════════════════════

    /**
     * Dialog zur Zahlungsart-Auswahl.
     * Legt den Verkauf in der DB an und bucht alle Positionen.
     */
    private void oeffneZahlungsDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("28rem");
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "0");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("background", "#f5f2ff").set("padding", "1.25rem 1.5rem");

        Span titel = new Span("Zahlungsart wählen");
        titel.getStyle()
                .set("font-size", "1.1rem").set("font-weight", "700").set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button closeBtn = new Button();
        closeBtn.getElement().appendChild(createIcon("close").getElement());
        closeBtn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("padding", "0.4rem").set("border-radius", "9999px")
                .set("min-width", "unset").set("color", "#553722");
        closeBtn.addClickListener(e -> dialog.close());
        header.add(titel, closeBtn);

        // Body
        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle()
                .set("background", "white").set("padding", "1.5rem").set("gap", "1rem");

        // Gesamtsumme anzeigen
        Span gesamtInfo = new Span("Gesamtbetrag: " + gesamtBetragSpan.getText());
        gesamtInfo.getStyle()
                .set("font-size", "1.1rem").set("font-weight", "700").set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        body.add(gesamtInfo);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setSpacing(false);
        buttons.getStyle().set("gap", "1rem");

        Button barBtn = new Button();
        Span barIcon = createIcon("wallet");
        Span barText = new Span("Bar");
        barText.getStyle().set("font-weight", "700").set("font-family", "'Plus Jakarta Sans', sans-serif");
        barBtn.getElement().appendChild(barIcon.getElement());
        barBtn.getElement().appendChild(barText.getElement());
        barBtn.getStyle()
                .set("flex", "1").set("padding", "1.25rem").set("background", "#ffdcc6")
                .set("color", "#553722").set("border", "none").set("border-radius", "1rem")
                .set("cursor", "pointer").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center").set("gap", "0.5rem")
                .set("font-size", "1rem");

        Button karteBtn = new Button();
        Span karteIcon = createIcon("credit_card");
        Span karteText = new Span("Karte");
        karteText.getStyle().set("font-weight", "700").set("font-family", "'Plus Jakarta Sans', sans-serif");
        karteBtn.getElement().appendChild(karteIcon.getElement());
        karteBtn.getElement().appendChild(karteText.getElement());
        karteBtn.getStyle()
                .set("flex", "1").set("padding", "1.25rem").set("background", "#553722")
                .set("color", "white").set("border", "none").set("border-radius", "1rem")
                .set("cursor", "pointer").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center").set("gap", "0.5rem")
                .set("font-size", "1rem");

        barBtn.addClickListener(e -> {
            verkaufAbschliessen(Zahlungsart.BAR);
            dialog.close();
        });
        karteBtn.addClickListener(e -> {
            verkaufAbschliessen(Zahlungsart.KARTE);
            dialog.close();
        });

        buttons.add(barBtn, karteBtn);
        body.add(buttons);
        layout.add(header, body);
        dialog.add(layout);

        dialog.getElement().executeJs(
                "setTimeout(() => {" +
                        "  const o = this.$.overlay;" +
                        "  if (o) { o.style.padding='0'; o.style.borderRadius='1rem'; o.style.overflow='hidden'; }" +
                        "  const c = this.$.overlay.$.content;" +
                        "  if (c) { c.style.padding='0'; c.style.borderRadius='1rem'; c.style.overflow='hidden'; }" +
                        "}, 50);"
        );

        dialog.open();
    }

    /**
     * Schließt den Verkauf in der DB ab.
     * Legt falls nötig einen neuen Verkauf an, fügt alle Positionen ein
     * und setzt Status auf ABGESCHLOSSEN.
     *
     * HINWEIS: User-ID = 1 als Fallback (kein Login-System aktiv).
     * TODO: Eingeloggten User übergeben wenn Spring Security aktiviert.
     */
    private void verkaufAbschliessen(Zahlungsart zahlungsart) {
        try {
            // Zwischensumme berechnen
            BigDecimal zwischensumme = warenkorbListe.stream()
                    .map(e -> e.artikel.getPreis().multiply(BigDecimal.valueOf(e.menge)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Rabatt abziehen
            BigDecimal rabattBetrag = BigDecimal.ZERO;
            if (aktuellerRabattProzent.compareTo(BigDecimal.ZERO) > 0) {
                rabattBetrag = zwischensumme
                        .multiply(aktuellerRabattProzent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            BigDecimal gesamtsumme = zwischensumme.subtract(rabattBetrag);

            // ── Verkauf komplett in einer Transaktion speichern ──────────────
            java.util.List<de.fhswf.kassensystem.model.Verkaufsposition> positionen =
                    new java.util.ArrayList<>();
            for (WarenkorbEintrag e : warenkorbListe) {
                de.fhswf.kassensystem.model.Verkaufsposition pos =
                        new de.fhswf.kassensystem.model.Verkaufsposition();
                pos.setArtikel(e.artikel);
                pos.setMenge(e.menge);
                pos.setEinzelpreis(e.artikel.getPreis());
                positionen.add(pos);
            }
            verkaufService.verkaufKomplett(
                    positionen, zahlungsart, aktuellerRabattProzent, gesamtsumme);
            // ─────────────────────────────────────────────────────────────────

            // Bestand für Artikel im Warenkorb reduzieren
            // Artikel mit Bestand >= 999 = "unbegrenzt" (werden nicht reduziert)
            for (WarenkorbEintrag e : warenkorbListe) {
                Artikel a = e.artikel;
                if (a.getBestand() < 999) {
                    int neuerBestand = Math.max(0, a.getBestand() - e.menge);
                    a.setBestand(neuerBestand);
                    artikelService.updateArtikel(a);
                }
            }

            // Erfolgs-Notification
            String msg = "Zahlung per " + zahlungsart.name() +
                    " erfolgreich! Betrag: " + formatPreis(gesamtsumme);
            if (rabattBetrag.compareTo(BigDecimal.ZERO) > 0) {
                msg += " (Rabatt: " + formatPreis(rabattBetrag) + ")";
            }
            Notification.show(msg, 4000, Notification.Position.BOTTOM_CENTER);

            // Reset
            warenkorbListe.clear();
            aktuellerVerkauf       = null;
            aktuellerRabattProzent = BigDecimal.ZERO;
            aktualisiereWarenkorbUI();
            ladeArtikelGrid();   // Grid neu laden damit reduzierter Bestand sichtbar ist

        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage(),
                    4000, Notification.Position.MIDDLE);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════════

    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            event.rerouteTo("login");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // KASSENBON
    // ═══════════════════════════════════════════════════════════

    /**
     * Druckt den aktuellen Warenkorb als Kassenbon-PDF.
     * Funktioniert auch bei leerem Warenkorb (zeigt leeren Bon).
     */
    private void druckeKassenbon() {
        try {
            byte[] pdfBytes = pdfExportService.exportiereKassenbon(
                    warenkorbListe.stream().map(e -> {
                        de.fhswf.kassensystem.model.Verkaufsposition pos =
                                new de.fhswf.kassensystem.model.Verkaufsposition();
                        pos.setArtikel(e.artikel);
                        pos.setMenge(e.menge);
                        pos.setEinzelpreis(e.artikel.getPreis());
                        return pos;
                    }).collect(java.util.stream.Collectors.toList()),
                    aktuellerRabattProzent
            );

            String dateiname = "Kassenbon_" +
                    java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) +
                    ".pdf";

            String base64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                    "const bytes = atob($0);" +
                            "const arr = new Uint8Array(bytes.length);" +
                            "for(let i=0;i<bytes.length;i++) arr[i]=bytes.charCodeAt(i);" +
                            "const blob = new Blob([arr],{type:'application/pdf'});" +
                            "const url = URL.createObjectURL(blob);" +
                            "const a = document.createElement('a');" +
                            "a.href = url; a.download = $1;" +
                            "document.body.appendChild(a); a.click();" +
                            "document.body.removeChild(a); URL.revokeObjectURL(url);",
                    base64, dateiname
            );

        } catch (Exception ex) {
            com.vaadin.flow.component.notification.Notification.show(
                    "Fehler beim Bon-Druck: " + ex.getMessage(), 4000,
                    com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        }
    }


}