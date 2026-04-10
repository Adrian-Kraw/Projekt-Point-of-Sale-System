package de.fhswf.kassensystem.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Abstrakte Basisklasse für alle Dialoge im Kassensystem.
 *
 * FIX: buildBody() wird NICHT mehr im Konstruktor von BaseDialog aufgerufen.
 * Stattdessen rufen alle Unterklassen am Ende ihres eigenen Konstruktors
 * super.init(titel, untertitel) auf – zu diesem Zeitpunkt sind alle
 * Instanzvariablen der Unterklasse bereits gesetzt.
 */
public abstract class BaseDialog extends Dialog {

    /**
     * Unterklassen rufen diese Methode am ENDE ihres eigenen Konstruktors auf,
     * NACHDEM alle eigenen Felder initialisiert wurden.
     *
     * Beispiel:
     *   MeinDialog(List<X> items, ...) {
     *       super();                 // nur Dialog-Grundkonstruktor
     *       this.items = items;      // eigene Felder setzen
     *       init("Titel", null);     // DANACH UI aufbauen
     *   }
     */
    protected final void init(String titel, String untertitel) {
        setWidth(getDialogBreite());
        setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "0");

        HorizontalLayout footer = buildCustomFooter();
        if (footer == null) footer = buildStandardFooter();

        layout.add(buildHeader(titel, untertitel), buildBody(), footer);
        add(layout);
        styleOverlay();
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRAKTE METHODEN
    // ═══════════════════════════════════════════════════════════

    protected abstract VerticalLayout buildBody();

    /** true = Erfolg → Dialog schließt. false = Fehler → Dialog bleibt offen. */
    protected abstract boolean onSpeichern();

    // ═══════════════════════════════════════════════════════════
    // HOOKS (optional überschreibbar)
    // ═══════════════════════════════════════════════════════════

    protected String getSpeichernLabel()  { return "Speichern"; }
    protected String getDialogBreite()    { return "28rem"; }

    /**
     * Eigenen Footer definieren (z.B. Ja/Nein statt Speichern/Abbrechen).
     * null → Standard-Footer wird verwendet.
     */
    protected HorizontalLayout buildCustomFooter() { return null; }

    // ═══════════════════════════════════════════════════════════
    // GEMEINSAME UI-BAUSTEINE
    // ═══════════════════════════════════════════════════════════

    private HorizontalLayout buildHeader(String titel, String untertitel) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("background", "#f5f2ff").set("padding", "1.25rem 1.5rem");

        VerticalLayout titelBlock = new VerticalLayout();
        titelBlock.setPadding(false);
        titelBlock.setSpacing(false);

        Span titelSpan = new Span(titel);
        titelSpan.getStyle()
                .set("font-size", "1rem").set("font-weight", "700").set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        titelBlock.add(titelSpan);

        if (untertitel != null) {
            Span sub = new Span(untertitel);
            sub.getStyle()
                    .set("font-size", "0.75rem").set("color", "#82746d")
                    .set("font-family", "'Plus Jakarta Sans', sans-serif");
            titelBlock.add(sub);
        }

        Button closeBtn = new Button();
        closeBtn.getElement().appendChild(icon("close").getElement());
        closeBtn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("padding", "0.4rem").set("border-radius", "9999px")
                .set("min-width", "unset").set("color", "#553722");
        closeBtn.addClickListener(e -> close());

        header.add(titelBlock, closeBtn);
        return header;
    }

    private HorizontalLayout buildStandardFooter() {
        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("flex", "1").set("background", "transparent")
                .set("border", "2px solid rgba(85,55,34,0.2)").set("border-radius", "1rem")
                .set("padding", "0.75rem 2rem").set("font-weight", "700")
                .set("color", "#553722").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        abbrechenBtn.addClickListener(e -> close());

        Button speichernBtn = new Button(getSpeichernLabel());
        speichernBtn.getStyle()
                .set("flex", "1").set("background", "#553722").set("color", "white")
                .set("border", "none").set("border-radius", "1rem").set("padding", "0.75rem 2rem")
                .set("font-weight", "700").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("box-shadow", "0 4px 15px rgba(85,55,34,0.25)");
        speichernBtn.addClickListener(e -> { if (onSpeichern()) close(); });

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setSpacing(false);
        footer.getStyle()
                .set("background", "#f5f2ff").set("padding", "1.25rem 1.5rem").set("gap", "1rem");
        footer.add(abbrechenBtn, speichernBtn);
        return footer;
    }

    private void styleOverlay() {
        getElement().executeJs(
                "setTimeout(() => {" +
                        "  const o = this.$.overlay;" +
                        "  if (o) { o.style.padding='0'; o.style.borderRadius='1rem'; o.style.overflow='hidden'; }" +
                        "  const c = this.$.overlay.$.content;" +
                        "  if (c) { c.style.padding='0'; c.style.borderRadius='1rem'; c.style.overflow='hidden'; }" +
                        "}, 50);"
        );
    }

    protected static Span icon(String name) {
        Span s = new Span(name);
        s.addClassName("material-symbols-outlined");
        s.getStyle().set("line-height", "1");
        return s;
    }

    protected static VerticalLayout feldMitLabel(String label, Component feld) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "0.4rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.6rem").set("font-weight", "800")
                .set("text-transform", "uppercase").set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        block.add(labelSpan, feld);
        return block;
    }
}