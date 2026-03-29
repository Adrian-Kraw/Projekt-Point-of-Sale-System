package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

/**
 * NeuerArtikelDialog ist ein modaler Dialog zum Erstellen eines neuen Artikels.
 *
 * Felder:
 * - Name des Artikels
 * - Kategorie (Dropdown)
 * - Preis (Zahlenfeld)
 * - MwSt (Dropdown: 7% / 19%)
 * - Anfangsbestand (Zahlenfeld)
 * - Minimalbestand mit Hinweistext
 *
 * Im Prototyp ohne Speicher-Logik.
 * TODO: ArtikelService.save() einbinden wenn Backend bereit ist.
 */
public class NeuerArtikelDialog extends Dialog {

    /**
     * Konstruktor baut den Dialog vollständig auf.
     * Aufruf: new NeuerArtikelDialog().open()
     */
    public NeuerArtikelDialog() {
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
         * Das Vaadin Dialog Overlay hat intern einen weißen Hintergrund
         * der sich per CSS nicht überschreiben lässt.
         * Wir setzen ihn direkt per JavaScript auf die Sidebar-Farbe.
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
                        "    content.style.borderRadius = '1rem';" +  // ← neu
                        "    content.style.overflow = 'hidden';" +     // ← neu
                        "  }" +
                        "}, 50);"
        );
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
     *
     * Layout:
     * - Name (volle Breite)
     * - Kategorie + Preis (zwei Spalten)
     * - MwSt + Anfangsbestand (zwei Spalten)
     * - Minimalbestand + Hinweis (volle Breite)
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

        body.add(buildFeld("NAME DES ARTIKELS", buildNameFeld()));
        body.add(buildZweispaltig(
                buildFeld("KATEGORIE",  buildKategorieSelect()),
                buildFeld("PREIS (€)",  buildPreisFeld())
        ));
        body.add(buildZweispaltig(
                buildFeld("MWST",           buildMwstSelect()),
                buildFeld("ANFANGSBESTAND", buildBestandFeld())
        ));
        body.add(buildMinBestandBlock());

        return body;
    }

    /**
     * Zwei Felder nebeneinander in einer Zeile.
     *
     * @param links  linkes Feld
     * @param rechts rechtes Feld
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
     *
     * @param label       Feldbezeichnung (wird uppercase dargestellt)
     * @param eingabefeld das Vaadin-Eingabefeld
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

        block.add(buildFeld("MINIMALBESTAND (WARNUNG)", buildMinBestandFeld()));

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

    private Select<String> buildKategorieSelect() {
        Select<String> select = new Select<>();
        select.setWidthFull();
        select.setItems("Heißgetränke", "Gebäck", "Kaltgetränke");
        select.setValue("Heißgetränke");
        select.addClassName("dialog-feld");
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

    private Select<String> buildMwstSelect() {
        Select<String> select = new Select<>();
        select.setWidthFull();
        select.setItems("7% (Ermäßigt)", "19% (Standard)");
        select.setValue("7% (Ermäßigt)");
        select.addClassName("dialog-feld");
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
        feld.setPlaceholder("5");
        feld.setMin(0);
        feld.addClassName("dialog-feld");
        return feld;
    }

    // ═══════════════════════════════════════════════════════════
    // FOOTER
    // ═══════════════════════════════════════════════════════════

    /**
     * Footer mit Abbrechen und Speichern Button.
     * TODO: Speichern mit ArtikelService verbinden.
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
                .set("flex", "1")        // ← neu
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
                .set("flex", "1")        // ← neu
                .set("justify-content", "center");

        speichernBtn.addClickListener(e -> close());

        footer.add(abbrechenBtn, speichernBtn);
        return footer;
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