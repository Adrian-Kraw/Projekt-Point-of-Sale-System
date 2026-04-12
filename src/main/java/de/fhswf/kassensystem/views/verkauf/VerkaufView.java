package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.enums.Zahlungsart;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.service.PdfExportService;
import de.fhswf.kassensystem.service.VerkaufService;
import de.fhswf.kassensystem.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kassier-View (Point of Sale).
 *
 * FIX: Bestand-Badge wird live aktualisiert wenn ein Artikel in den
 *      Warenkorb gelegt oder daraus entfernt wird.
 *      Karten-Map merkt sich welche Div-Instanz zu welchem Artikel gehört.
 */
@RolesAllowed({"KASSIERER", "MANAGER"})
@Route(value = "kassieren", layout = MainLayout.class)
public class VerkaufView extends HorizontalLayout implements BeforeEnterObserver {

    private final ArtikelService           artikelService;
    private final VerkaufService           verkaufService;
    private final PdfExportService         pdfExportService;
    private final WarenkorbZusammenfassung zusammenfassung;

    private final List<WarenkorbEintrag>  warenkorbListe  = new ArrayList<>();
    private final Map<Long, Div>          kartenMap       = new HashMap<>(); // artikelId → Karte
    private String aktiveKategorie = "Alle";
    private String aktuelleSuche   = "";

    private final VerticalLayout warenkorbPositionenLayout = new VerticalLayout();
    private final Div            artikelGridDiv            = new Div();

    public VerkaufView(ArtikelService artikelService, VerkaufService verkaufService,
                       PdfExportService pdfExportService) {
        this.artikelService   = artikelService;
        this.verkaufService   = verkaufService;
        this.pdfExportService = pdfExportService;
        this.zusammenfassung  = new WarenkorbZusammenfassung(
                this::warenkorbLeeren,
                gesamtBetrag -> {
                    if (warenkorbListe.isEmpty()) {
                        Notification.show("Warenkorb ist leer.", 2000, Notification.Position.MIDDLE);
                        return;
                    }
                    String betrag = gesamtBetrag != null ? gesamtBetrag : "0,00€";
                    new ZahlungsDialog(betrag, this::verkaufAbschliessen).open();
                },
                this::aktualisiereWarenkorbUI
        );
        zusammenfassung.getElement().setAttribute("tour-id", "zusammenfassung");

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("overflow", "hidden").set("height", "100vh");

        warenkorbPositionenLayout.setPadding(false);
        warenkorbPositionenLayout.setSpacing(false);
        warenkorbPositionenLayout.getStyle()
                .set("padding", "0 1.5rem").set("gap", "0.25rem")
                .set("flex", "1").set("overflow-y", "auto");

        add(buildArtikelSpalte(), buildWarenkorbSpalte());
    }

    // ═══════════════════════════════════════════════════════════
    // LINKE SPALTE
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildArtikelSpalte() {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
        spalte.getStyle()
                .set("flex", "0 0 60%").set("max-width", "55%")
                .set("background", "#fcf8ff").set("overflow-y", "auto")
                .set("height", "100%").set("padding", "2rem").set("box-sizing", "border-box");

        artikelGridDiv.addClassName("artikel-grid");
        artikelGridDiv.getElement().setAttribute("tour-id", "artikel-grid");
        artikelGridDiv.getStyle()
                .set("width", "100%").set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)").set("gap", "1.25rem");

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

    private void ladeArtikelGrid() {
        artikelGridDiv.removeAll();
        kartenMap.clear();

        List<Artikel> alle = aktuelleSuche.isBlank()
                ? artikelService.findAllArtikel()
                : artikelService.findByName(aktuelleSuche);

        for (Artikel a : alle) {
            if (!aktiveKategorie.equals("Alle") && !a.getKategorie().getName().equals(aktiveKategorie)) continue;
            if (!a.isAktiv()) continue;

            // Aktuellen Warenkorb-Abzug berechnen für korrekten Startbestand
            int imKorb = warenkorbListe.stream()
                    .filter(e -> e.artikel.getId().equals(a.getId()))
                    .mapToInt(e -> e.menge)
                    .sum();
            int anzeigeBestand = a.getBestand() >= 999 ? 999 : Math.max(0, a.getBestand() - imKorb);
            boolean ausverkauft = a.getBestand() == 0;

            Div karte = ArtikelKarteFactory.create(a, anzeigeBestand, ausverkauft,
                    this::artikelZumKorbHinzufuegen);
            kartenMap.put(a.getId(), karte);
            artikelGridDiv.add(karte);
        }
    }

    private HorizontalLayout buildSuchfeld() {
        TextField search = new TextField();
        search.setWidthFull();
        search.setPlaceholder("Artikel suchen...");
        search.setPrefixComponent(createIcon("search"));
        search.addValueChangeListener(e -> { aktuelleSuche = e.getValue(); ladeArtikelGrid(); });

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("margin-bottom", "1.5rem");
        row.getElement().setAttribute("tour-id", "artikel-suche");
        row.add(search);
        return row;
    }

    private KategorieChipGroup buildKategorieFilter() {
        List<String> kategorien = artikelService.findAllArtikel().stream()
                .filter(Artikel::isAktiv)
                .map(a -> a.getKategorie().getName())
                .distinct()
                .collect(Collectors.toList());
        KategorieChipGroup chipGroup = new KategorieChipGroup(kategorien, kat -> {
            aktiveKategorie = kat;
            ladeArtikelGrid();
        });
        chipGroup.getElement().setAttribute("tour-id", "kategorie-chips");
        return chipGroup;
    }

    // ═══════════════════════════════════════════════════════════
    // RECHTE SPALTE
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildWarenkorbSpalte() {
        VerticalLayout spalte = new VerticalLayout();
        spalte.setPadding(false);
        spalte.setSpacing(false);
        spalte.getStyle()
                .set("flex", "1").set("background", "#f5f2ff").set("min-height", "100vh")
                .set("height", "100%").set("display", "flex").set("flex-direction", "column")
                .set("overflow", "hidden");
        spalte.getElement().setAttribute("tour-id", "warenkorb-spalte");
        spalte.add(new WarenkorbHeader(this::warenkorbLeeren), warenkorbPositionenLayout, zusammenfassung);
        return spalte;
    }

    // ═══════════════════════════════════════════════════════════
    // WARENKORB STATE
    // ═══════════════════════════════════════════════════════════

    private void artikelZumKorbHinzufuegen(Artikel artikel) {
        if (artikel.getBestand() == 0) {
            Notification.show("\"" + artikel.getName() + "\" ist nicht mehr auf Lager.",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        for (WarenkorbEintrag e : warenkorbListe) {
            if (e.artikel.getId().equals(artikel.getId())) {
                if (artikel.getBestand() < 999 && e.menge >= artikel.getBestand()) {
                    Notification.show("Nicht mehr Bestand vorhanden als bereits im Warenkorb.",
                            2500, Notification.Position.MIDDLE);
                    return;
                }
                e.menge++;
                aktualisiereWarenkorbUI();
                return;
            }
        }
        warenkorbListe.add(new WarenkorbEintrag(artikel, 1));
        aktualisiereWarenkorbUI();
    }

    private void aktualisiereWarenkorbUI() {
        warenkorbListe.removeIf(e -> e.menge <= 0);

        // Warenkorb-Positionen neu zeichnen
        warenkorbPositionenLayout.removeAll();
        boolean zebra = false;
        for (WarenkorbEintrag e : new ArrayList<>(warenkorbListe)) {
            warenkorbPositionenLayout.add(
                    WarenkorbPositionFactory.create(e, zebra, () -> {
                        warenkorbListe.removeIf(x -> x.menge <= 0);
                        aktualisiereWarenkorbUI();
                    })
            );
            zebra = !zebra;
        }
        zusammenfassung.aktualisierePreise(warenkorbListe);

        // FIX: Bestand-Badges in allen sichtbaren Karten live aktualisieren
        aktualisiereBestandBadges();
    }

    /**
     * Berechnet für jeden Artikel den aktuell anzuzeigenden Bestand
     * (DB-Bestand minus was gerade im Warenkorb liegt) und aktualisiert
     * den Badge in der Karte per JS – ohne die Karte neu zu bauen.
     */
    private void aktualisiereBestandBadges() {
        for (Map.Entry<Long, Div> entry : kartenMap.entrySet()) {
            Long artikelId = entry.getKey();
            Div  karte     = entry.getValue();

            // Wie viel liegt im Warenkorb?
            int imKorb = warenkorbListe.stream()
                    .filter(e -> e.artikel.getId().equals(artikelId))
                    .mapToInt(e -> e.menge)
                    .sum();

            // DB-Bestand des Artikels (aus dem WarenkorbEintrag oder der Karte)
            int dbBestand = warenkorbListe.stream()
                    .filter(e -> e.artikel.getId().equals(artikelId))
                    .map(e -> e.artikel.getBestand())
                    .findFirst()
                    .orElseGet(() -> {
                        // Artikel nicht im Warenkorb → Originalbestand aus Service holen
                        // (nur wenn nötig, dh. imKorb == 0 → Badge bleibt eh gleich)
                        return 0;
                    });

            // Wenn Artikel im Warenkorb: Bestand live anpassen
            if (imKorb > 0) {
                int anzeige = dbBestand >= 999 ? 999 : Math.max(0, dbBestand - imKorb);
                ArtikelKarteFactory.aktualisiereBestand(karte, artikelId, anzeige);
            } else {
                // Nicht im Warenkorb → Originalbestand wiederherstellen
                // Artikel aus Service nachladen um echten Bestand zu kennen
                artikelService.findAllArtikel().stream()
                        .filter(a -> a.getId().equals(artikelId))
                        .findFirst()
                        .ifPresent(a -> ArtikelKarteFactory.aktualisiereBestand(
                                karte, artikelId, a.getBestand()));
            }
        }
    }

    private void warenkorbLeeren() {
        warenkorbListe.clear();
        zusammenfassung.resetRabatt();
        aktualisiereWarenkorbUI();
    }

    // ═══════════════════════════════════════════════════════════
    // GESCHÄFTSLOGIK
    // ═══════════════════════════════════════════════════════════

    private void verkaufAbschliessen(Zahlungsart zahlungsart) {
        try {
            BigDecimal zwischensumme = warenkorbListe.stream()
                    .map(e -> e.artikel.getPreis().multiply(BigDecimal.valueOf(e.menge)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal rabatt = zusammenfassung.getAktuellerRabattProzent();
            BigDecimal rabattBetrag = rabatt.compareTo(BigDecimal.ZERO) > 0
                    ? zwischensumme.multiply(rabatt).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal gesamtsumme = zwischensumme.subtract(rabattBetrag);

            List<Verkaufsposition> positionen = new ArrayList<>();
            for (WarenkorbEintrag e : warenkorbListe) {
                Verkaufsposition pos = new Verkaufsposition();
                pos.setArtikel(e.artikel);
                pos.setMenge(e.menge);
                pos.setEinzelpreis(e.artikel.getPreis());
                positionen.add(pos);
            }
            verkaufService.verkaufKomplett(positionen, zahlungsart, rabatt, gesamtsumme);

            for (WarenkorbEintrag e : warenkorbListe) {
                if (e.artikel.getBestand() < 999) {
                    e.artikel.setBestand(Math.max(0, e.artikel.getBestand() - e.menge));
                    artikelService.updateArtikel(e.artikel);
                }
            }

            String msg = "Zahlung per " + zahlungsart.name() + " erfolgreich! Betrag: "
                    + WarenkorbZusammenfassung.format(gesamtsumme);
            if (rabattBetrag.compareTo(BigDecimal.ZERO) > 0)
                msg += " (Rabatt: " + WarenkorbZusammenfassung.format(rabattBetrag) + ")";
            Notification.show(msg, 4000, Notification.Position.BOTTOM_CENTER);

            final List<Verkaufsposition> bonPositionen = new ArrayList<>(positionen);
            final BigDecimal bonRabatt = rabatt;

            new QuittungsDialog(
                    () -> { druckeKassenbon(bonPositionen, bonRabatt); warenkorbLeeren(); ladeArtikelGrid(); },
                    () -> { warenkorbLeeren(); ladeArtikelGrid(); }
            ).open();

        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void druckeKassenbon(List<Verkaufsposition> positionen, BigDecimal rabatt) {
        try {
            byte[] pdfBytes = pdfExportService.exportiereKassenbon(positionen, rabatt);
            String dateiname = "Kassenbon_" + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".pdf";
            String base64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                    "const bytes=atob($0);const arr=new Uint8Array(bytes.length);" +
                            "for(let i=0;i<bytes.length;i++)arr[i]=bytes.charCodeAt(i);" +
                            "const blob=new Blob([arr],{type:'application/pdf'});" +
                            "const url=URL.createObjectURL(blob);" +
                            "const a=document.createElement('a');a.href=url;a.download=$1;" +
                            "document.body.appendChild(a);a.click();" +
                            "document.body.removeChild(a);URL.revokeObjectURL(url);",
                    base64, dateiname);
        } catch (Exception ex) {
            Notification.show("Fehler beim Bon-Druck: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            event.rerouteTo("login");
        }
    }
    // ═══════════════════════════════════════════════════════════
    // TOUR-AKTIONEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet Tour-Aktionen aus dem TourManager.
     * Wird als actionHandler an tourManager.start() übergeben.
     */
    public void tourAktion(String action) {
        switch (action) {
            case "demo-verkauf" -> {
                // Ersten verfügbaren Artikel in den Warenkorb legen (nur Demo, kein Verkauf)
                if (warenkorbListe.isEmpty()) {
                    artikelService.findAllArtikel().stream()
                            .filter(a -> a.isAktiv() && a.getBestand() > 0)
                            .findFirst()
                            .ifPresent(this::artikelZumKorbHinzufuegen);
                }
            }
            case "open-zahlungsdialog" -> {
                String betrag = zusammenfassung.getGesamtBetragText();
                String anzeige = (betrag == null || betrag.isBlank()) ? "2,99€" : betrag;
                new ZahlungsDialog(anzeige, art -> {}).open();
            }
            case "open-quittungsdialog" -> {
                new QuittungsDialog(() -> {}, () -> {}).open();
            }
        }
    }

}