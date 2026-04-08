package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import de.fhswf.kassensystem.service.ArtikelService;

import java.math.BigDecimal;

/**
 * NeuerArtikelDialog ermöglicht das Erstellen eines neuen Artikels.
 *
 * Felder:
 * - Name des Artikels
 * - Kategorie (Dropdown aus DB)
 * - Preis (Zahlenfeld)
 * - MwSt (Dropdown: 7% / 19% aus DB)
 * - Anfangsbestand (Zahlenfeld)
 * - Minimalbestand mit Hinweistext
 *
 * Speichert über ArtikelService.createArtikel().
 */
public class NeuerArtikelDialog extends Dialog {

    private final ArtikelService artikelService;

    // Felder als Instanzvariablen für buildBody() und speichern()
    private TextField nameFeld;
    private Select<Kategorie> kategorieSelect;
    private NumberField preisFeld;
    private Select<Mehrwertsteuer> mwstSelect;
    private NumberField bestandFeld;
    private NumberField minBestandFeld;

    /** Wenn != null, wird der Dialog im Bearbeitungsmodus geöffnet. */
    private Artikel zuBearbeitenderArtikel = null;
    private byte[] hochgeladensBild = null;  // Artikelbild als Byte-Array

    /**
     * Konstruktor baut den Dialog vollständig auf.
     * Aufruf: new NeuerArtikelDialog(artikelService).open()
     *
     * @param artikelService Service für Datenbankoperationen
     */
    public NeuerArtikelDialog(ArtikelService artikelService) {
        this.artikelService = artikelService;

        setWidth("36rem");
        setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
        layout.getStyle().set("gap", "0").set("background", "#f5f2ff");

        layout.add(
                buildHeader(),
                buildBody(),
                buildFooter()
        );

        add(layout);

        /*
         * Dialog-Overlay per JavaScript stylen (Shadow DOM).
         */
        getElement().executeJs(
                "setTimeout(() => {" +
                        "  const overlay = this.$.overlay;" +
                        "  if (overlay) {" +
                        "    overlay.style.padding = '0';" +
                        "    overlay.style.borderRadius = '1rem';" +
                        "    overlay.style.overflow = 'hidden';" +
                        "  }" +
                        "  const content = this.$.overlay.$.content;" +
                        "  if (content) {" +
                        "    content.style.padding = '0';" +
                        "    content.style.borderRadius = '1rem';" +
                        "    content.style.overflow = 'hidden';" +
                        "  }" +
                        "}, 50);"
        );
    }

    /**
     * Konstruktor für Bearbeitungsmodus: Felder werden mit vorhandenen Werten vorausgefüllt.
     */
    public NeuerArtikelDialog(ArtikelService artikelService, Artikel artikel) {
        this(artikelService);
        this.zuBearbeitenderArtikel = artikel;
        nameFeld.setValue(artikel.getName());
        preisFeld.setValue(artikel.getPreis().doubleValue());
        bestandFeld.setValue((double) artikel.getBestand());
        if (minBestandFeld != null) {
            minBestandFeld.setValue((double) artikel.getMinimalbestand());
        }
        if (artikel.getKategorie() != null) {
            kategorieSelect.getListDataView().getItems()
                    .filter(k -> k.getId().equals(artikel.getKategorie().getId()))
                    .findFirst().ifPresent(kategorieSelect::setValue);
        }
        if (artikel.getMehrwertsteuer() != null) {
            mwstSelect.getListDataView().getItems()
                    .filter(m -> m.getId().equals(artikel.getMehrwertsteuer().getId()))
                    .findFirst().ifPresent(mwstSelect::setValue);
        }
        if (artikel.getBild() != null) {
            hochgeladensBild = artikel.getBild();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    /**
     * Header mit Icon, Titel und Schließen-Button.
     */
    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "1.25rem 1.5rem");

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "0.75rem");

        Span titelIcon = createIcon("add_box");
        titelIcon.getStyle().set("color", "#553722");

        Span titel = new Span("Neuer Artikel");
        titel.getStyle()
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(titelIcon, titel);

        Button closeBtn = new Button();
        closeBtn.getElement().appendChild(createIcon("close").getElement());
        closeBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0.4rem")
                .set("border-radius", "9999px")
                .set("min-width", "unset")
                .set("color", "#553722");
        closeBtn.addClickListener(e -> close());

        header.add(titelGruppe, closeBtn);
        return header;
    }

    // ═══════════════════════════════════════════════════════════
    // BODY
    // ═══════════════════════════════════════════════════════════

    /**
     * Body mit allen Eingabefeldern.
     */
    private VerticalLayout buildBody() {
        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle()
                .set("padding", "1.5rem")
                .set("gap", "1.25rem")
                .set("background", "white")
                .set("margin", "0");

        nameFeld = buildNameFeld();
        kategorieSelect = buildKategorieSelect();
        preisFeld = buildPreisFeld();
        mwstSelect = buildMwstSelect();
        bestandFeld = buildBestandFeld();
        minBestandFeld = buildMinBestandFeld();

        body.add(buildFeld("NAME DES ARTIKELS", nameFeld));
        body.add(buildZweispaltig(
                buildFeld("KATEGORIE", kategorieSelect),
                buildFeld("PREIS (€)",  preisFeld)
        ));
        body.add(buildZweispaltig(
                buildFeld("MWST",           mwstSelect),
                buildFeld("ANFANGSBESTAND", bestandFeld)
        ));
        body.add(buildMinBestandBlock());
        body.add(buildBildUploadBlock());

        return body;
    }

    /**
     * Zwei Felder nebeneinander in einer Zeile.
     */
    private HorizontalLayout buildZweispaltig(VerticalLayout links,
                                              VerticalLayout rechts) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setSpacing(false);
        zeile.getStyle().set("gap", "1rem");

        links.getStyle().set("flex", "1");
        rechts.getStyle().set("flex", "1");

        zeile.add(links, rechts);
        return zeile;
    }

    /**
     * Label + Eingabefeld als Block.
     */
    private VerticalLayout buildFeld(String label, Component eingabefeld) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "0.4rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.6rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        block.add(labelSpan, eingabefeld);
        return block;
    }

    /**
     * Minimalbestand-Block mit Feld und Hinweistext.
     */
    private VerticalLayout buildMinBestandBlock() {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "0.25rem");

        block.add(buildFeld("MINIMALBESTAND (WARNUNG)", minBestandFeld));

        Paragraph hinweis = new Paragraph(
                "* Bei Erreichen wird eine Warnung in der Liste angezeigt.");
        hinweis.getStyle()
                .set("font-size", "0.65rem")
                .set("color", "#82746d")
                .set("font-style", "italic")
                .set("margin", "0")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        block.add(hinweis);
        return block;
    }

    // ═══════════════════════════════════════════════════════════
    // EINGABEFELDER
    // ═══════════════════════════════════════════════════════════

    private TextField buildNameFeld() {
        TextField feld = new TextField();
        feld.setWidthFull();
        feld.setPlaceholder("z.B. Cortado");
        feld.addClassName("dialog-feld");
        return feld;
    }

    /**
     * Kategorie-Dropdown mit Daten aus der Datenbank.
     * Zeigt den Kategorienamen an.
     */
    private Select<Kategorie> buildKategorieSelect() {
        Select<Kategorie> select = new Select<>();
        select.setWidthFull();
        select.addClassName("dialog-feld");

        // Kategorien über ArtikelService laden (alle aktiven Artikel → Kategorien)
        // Da kein KategorieService existiert, holen wir sie aus Artikeln
        java.util.Set<Kategorie> kategorien = new java.util.LinkedHashSet<>();
        artikelService.findAllArtikel().forEach(a -> kategorien.add(a.getKategorie()));
        select.setItems(kategorien);
        select.setItemLabelGenerator(Kategorie::getName);

        if (!kategorien.isEmpty()) {
            select.setValue(kategorien.iterator().next());
        }
        return select;
    }

    private NumberField buildPreisFeld() {
        NumberField feld = new NumberField();
        feld.setWidthFull();
        feld.setPlaceholder("0.00");
        feld.setMin(0);
        feld.addClassName("dialog-feld");
        return feld;
    }

    /**
     * MwSt-Dropdown mit Daten aus der Datenbank.
     * Zeigt Bezeichnung + Satz an (z.B. "7% (Ermäßigt)").
     */
    private Select<Mehrwertsteuer> buildMwstSelect() {
        Select<Mehrwertsteuer> select = new Select<>();
        select.setWidthFull();
        select.addClassName("dialog-feld");

        java.util.Set<Mehrwertsteuer> mwstSaetze = new java.util.LinkedHashSet<>();
        artikelService.findAllArtikel().forEach(a -> mwstSaetze.add(a.getMehrwertsteuer()));
        select.setItems(mwstSaetze);
        select.setItemLabelGenerator(m ->
                m.getSatz().stripTrailingZeros().toPlainString() + "% (" + m.getBezeichnung() + ")");

        if (!mwstSaetze.isEmpty()) {
            select.setValue(mwstSaetze.iterator().next());
        }
        return select;
    }

    private NumberField buildBestandFeld() {
        NumberField feld = new NumberField();
        feld.setWidthFull();
        feld.setPlaceholder("0");
        feld.setMin(0);
        feld.addClassName("dialog-feld");
        return feld;
    }

    private NumberField buildMinBestandFeld() {
        NumberField feld = new NumberField();
        feld.setWidthFull();
        feld.setPlaceholder(String.valueOf(Artikel.STANDARD_MINIMALBESTAND));
        feld.setMin(0);
        feld.addClassName("dialog-feld");
        return feld;
    }

    // ═══════════════════════════════════════════════════════════
    // FOOTER
    // ═══════════════════════════════════════════════════════════

    /**
     * Footer mit Abbrechen und Speichern Button.
     * Speichern liest die Feldwerte aus und ruft artikelService.createArtikel() auf.
     */
    private HorizontalLayout buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        footer.setSpacing(false);
        footer.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "1.25rem 2.0rem")
                .set("gap", "1rem");

        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("background", "transparent")
                .set("border", "2px solid rgba(85,55,34,0.2)")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 2.5rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("flex", "1")
                .set("justify-content", "center");
        abbrechenBtn.addClickListener(e -> close());

        Button speichernBtn = new Button("Speichern");
        speichernBtn.getStyle()
                .set("background", "#553722")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 2.5rem")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("box-shadow", "0 4px 15px rgba(85,55,34,0.25)")
                .set("flex", "1")
                .set("justify-content", "center");

        speichernBtn.addClickListener(e -> speichern());

        footer.add(abbrechenBtn, speichernBtn);
        return footer;
    }

    // ═══════════════════════════════════════════════════════════
    // BILD-UPLOAD
    // ═══════════════════════════════════════════════════════════

    /**
     * Upload-Block für Artikelbild (optional).
     * Lädt Bild in hochgeladensBild als byte[] vor dem Speichern.
     */
    private VerticalLayout buildBildUploadBlock() {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "0.4rem");

        Span labelSpan = new Span("ARTIKELBILD (OPTIONAL)");
        labelSpan.getStyle()
                .set("font-size", "0.6rem").set("font-weight", "800")
                .set("text-transform", "uppercase").set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setWidthFull();
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5 MB

        // Vorschaubild
        Image vorschau = new Image();
        vorschau.getStyle()
                .set("width", "100%").set("max-height", "8rem")
                .set("object-fit", "cover").set("border-radius", "0.75rem")
                .set("display", "none");

        upload.addSucceededListener(event -> {
            try {
                hochgeladensBild = buffer.getInputStream().readAllBytes();
                StreamResource sr = new StreamResource(
                        event.getFileName(),
                        () -> new java.io.ByteArrayInputStream(hochgeladensBild));
                vorschau.setSrc(sr);
                vorschau.getStyle().set("display", "block");
                Notification.show("Bild geladen: " + event.getFileName(),
                        2000, Notification.Position.BOTTOM_START);
            } catch (Exception e) {
                Notification.show("Fehler beim Laden des Bildes.",
                        3000, Notification.Position.MIDDLE);
            }
        });

        upload.addFileRemovedListener(event -> {
            hochgeladensBild = null;
            vorschau.setSrc("");
            vorschau.getStyle().set("display", "none");
        });

        block.add(labelSpan, upload, vorschau);
        return block;
    }

    // ═══════════════════════════════════════════════════════════
    // SPEICHERN-LOGIK
    // ═══════════════════════════════════════════════════════════

    /**
     * Validiert die Eingaben und speichert den neuen Artikel über den ArtikelService.
     * Bei Erfolg wird der Dialog geschlossen.
     * Bei fehlenden Pflichtfeldern wird eine Notification angezeigt.
     */
    private void speichern() {
        if (nameFeld.isEmpty()) {
            Notification.show("Bitte einen Namen eingeben.", 3000,
                    Notification.Position.MIDDLE);
            return;
        }
        if (kategorieSelect.isEmpty()) {
            Notification.show("Bitte eine Kategorie auswählen.", 3000,
                    Notification.Position.MIDDLE);
            return;
        }
        if (preisFeld.isEmpty()) {
            Notification.show("Bitte einen Preis eingeben.", 3000,
                    Notification.Position.MIDDLE);
            return;
        }
        if (mwstSelect.isEmpty()) {
            Notification.show("Bitte einen MwSt-Satz auswählen.", 3000,
                    Notification.Position.MIDDLE);
            return;
        }

        Artikel neuerArtikel = new Artikel();
        neuerArtikel.setName(nameFeld.getValue().trim());
        neuerArtikel.setKategorie(kategorieSelect.getValue());
        neuerArtikel.setPreis(BigDecimal.valueOf(preisFeld.getValue()));
        neuerArtikel.setMehrwertsteuer(mwstSelect.getValue());
        neuerArtikel.setBestand(bestandFeld.isEmpty() ? 0
                : bestandFeld.getValue().intValue());
        neuerArtikel.setMinimalbestand(minBestandFeld.isEmpty()
                ? Artikel.STANDARD_MINIMALBESTAND
                : minBestandFeld.getValue().intValue());
        neuerArtikel.setAktiv(true);
        if (hochgeladensBild != null) {
            neuerArtikel.setBild(hochgeladensBild);
        }

        if (zuBearbeitenderArtikel != null) {
            neuerArtikel.setId(zuBearbeitenderArtikel.getId());
            artikelService.updateArtikel(neuerArtikel);
            Notification.show("Artikel \"" + neuerArtikel.getName() + "\" wurde aktualisiert.",
                    3000, Notification.Position.BOTTOM_START);
        } else {
            artikelService.createArtikel(neuerArtikel);
            Notification.show("Artikel \"" + neuerArtikel.getName() + "\" wurde erstellt.",
                    3000, Notification.Position.BOTTOM_START);
        }
        close();
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt einen Material Symbols Icon-Span.
     *
     * @param iconName Name des Icons
     */
    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }
}