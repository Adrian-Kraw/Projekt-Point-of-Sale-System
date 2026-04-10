package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Kapselt alle Eingabefelder des Artikel-Dialogs mit Validierung und Befüllung.
 */
class ArtikelFormularFelder extends VerticalLayout {

    private final TextField          nameFeld       = new TextField();
    private final Select<Kategorie>  kategorieSelect;
    private final NumberField        preisFeld      = new NumberField();
    private final Select<Mehrwertsteuer> mwstSelect;
    private final NumberField        bestandFeld    = new NumberField();
    private final NumberField        minBestandFeld = new NumberField();

    ArtikelFormularFelder(ArtikelService service) {
        this.kategorieSelect = buildKategorieSelect(service);
        this.mwstSelect      = buildMwstSelect(service);

        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle().set("gap", "1.25rem");

        nameFeld.setWidthFull();
        nameFeld.setPlaceholder("z.B. Cortado");
        nameFeld.addClassName("dialog-feld");

        preisFeld.setWidthFull();
        preisFeld.setPlaceholder("0.00");
        preisFeld.setMin(0);
        preisFeld.addClassName("dialog-feld");

        bestandFeld.setWidthFull();
        bestandFeld.setPlaceholder("0");
        bestandFeld.setMin(0);
        bestandFeld.addClassName("dialog-feld");

        minBestandFeld.setWidthFull();
        minBestandFeld.setPlaceholder(String.valueOf(Artikel.STANDARD_MINIMALBESTAND));
        minBestandFeld.setMin(0);
        minBestandFeld.addClassName("dialog-feld");

        add(
                feld("NAME DES ARTIKELS", nameFeld),
                zweispaltig(feld("KATEGORIE", kategorieSelect), feld("PREIS (€)", preisFeld)),
                zweispaltig(feld("MWST", mwstSelect), feld("ANFANGSBESTAND", bestandFeld)),
                buildMinBestandBlock()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // ÖFFENTLICHE API
    // ═══════════════════════════════════════════════════════════

    void befuelleFelder(Artikel artikel) {
        nameFeld.setValue(artikel.getName());
        preisFeld.setValue(artikel.getPreis().doubleValue());
        bestandFeld.setValue((double) artikel.getBestand());
        minBestandFeld.setValue((double) artikel.getMinimalbestand());
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
    }

    boolean valide() {
        if (nameFeld.isEmpty()) {
            Notification.show("Bitte einen Namen eingeben.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        if (kategorieSelect.isEmpty()) {
            Notification.show("Bitte eine Kategorie auswählen.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        if (preisFeld.isEmpty()) {
            Notification.show("Bitte einen Preis eingeben.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        if (mwstSelect.isEmpty()) {
            Notification.show("Bitte einen MwSt-Satz auswählen.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        return true;
    }

    Artikel toArtikel() {
        Artikel a = new Artikel();
        a.setName(nameFeld.getValue().trim());
        a.setKategorie(kategorieSelect.getValue());
        a.setPreis(BigDecimal.valueOf(preisFeld.getValue()));
        a.setMehrwertsteuer(mwstSelect.getValue());
        a.setBestand(bestandFeld.isEmpty() ? 0 : bestandFeld.getValue().intValue());
        a.setMinimalbestand(minBestandFeld.isEmpty()
                ? Artikel.STANDARD_MINIMALBESTAND
                : minBestandFeld.getValue().intValue());
        a.setAktiv(true);
        return a;
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE BUILDER
    // ═══════════════════════════════════════════════════════════

    private Select<Kategorie> buildKategorieSelect(ArtikelService service) {
        Set<Kategorie> kategorien = new LinkedHashSet<>();
        service.findAllArtikel().forEach(a -> kategorien.add(a.getKategorie()));
        Select<Kategorie> select = new Select<>();
        select.setWidthFull();
        select.addClassName("dialog-feld");
        select.setItems(kategorien);
        select.setItemLabelGenerator(Kategorie::getName);
        if (!kategorien.isEmpty()) select.setValue(kategorien.iterator().next());
        return select;
    }

    private Select<Mehrwertsteuer> buildMwstSelect(ArtikelService service) {
        Set<Mehrwertsteuer> saetze = new LinkedHashSet<>();
        service.findAllArtikel().forEach(a -> saetze.add(a.getMehrwertsteuer()));
        Select<Mehrwertsteuer> select = new Select<>();
        select.setWidthFull();
        select.addClassName("dialog-feld");
        select.setItems(saetze);
        select.setItemLabelGenerator(m ->
                m.getSatz().stripTrailingZeros().toPlainString() + "% (" + m.getBezeichnung() + ")");
        if (!saetze.isEmpty()) select.setValue(saetze.iterator().next());
        return select;
    }

    private VerticalLayout buildMinBestandBlock() {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "0.25rem");
        block.add(feld("MINIMALBESTAND (WARNUNG)", minBestandFeld));
        Paragraph hinweis = new Paragraph("* Bei Erreichen wird eine Warnung in der Liste angezeigt.");
        hinweis.getStyle()
                .set("font-size", "0.65rem").set("color", "#82746d")
                .set("font-style", "italic").set("margin", "0")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        block.add(hinweis);
        return block;
    }

    private static VerticalLayout feld(String label, Component eingabefeld) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);
        block.getStyle().set("gap", "0.4rem");
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.6rem").set("font-weight", "800")
                .set("text-transform", "uppercase").set("letter-spacing", "0.1em")
                .set("color", "#82746d").set("font-family", "'Plus Jakarta Sans', sans-serif");
        block.add(labelSpan, eingabefeld);
        return block;
    }

    private static HorizontalLayout zweispaltig(VerticalLayout links, VerticalLayout rechts) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setSpacing(false);
        zeile.getStyle().set("gap", "1rem");
        links.getStyle().set("flex", "1");
        rechts.getStyle().set("flex", "1");
        zeile.add(links, rechts);
        return zeile;
    }
}
