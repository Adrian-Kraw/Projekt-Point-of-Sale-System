package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Kategorie;
import de.fhswf.kassensystem.model.Mehrwertsteuer;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.views.components.FehlerUI;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Kapselt alle Eingabefelder des Artikel-Dialogs (Name, Kategorie, Preis, MwSt,
 * Bestand, Minimalbestand) inklusive Validierung und Datenbefüllung.
 *
 * <p>Die Klasse stellt drei öffentliche Methoden bereit:
 * <ul>
 *   <li>{@link #befuelleFelder(Artikel)} – füllt Felder beim Bearbeiten</li>
 *   <li>{@link #valide()} – prüft Pflichtfelder und zeigt Fehlermeldungen</li>
 *   <li>{@link #toArtikel()} – liest Feldwerte in ein neues {@code Artikel}-Objekt</li>
 * </ul>
 *
 * @author Adrian Krawietz
 */
class ArtikelFormularFelder extends VerticalLayout {

    private final TextField              nameFeld       = new TextField();
    private final Select<Kategorie>      kategorieSelect;
    private final NumberField            preisFeld      = new NumberField();
    private final Select<Mehrwertsteuer> mwstSelect;
    private final NumberField            bestandFeld    = new NumberField();
    private final NumberField            minBestandFeld = new NumberField();

    /**
     * Erstellt alle Eingabefelder und baut das zweispaltige Formularlayout auf.
     *
     * @param service wird für das Laden von Kategorien und MwSt-Sätzen benötigt
     */
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

    /**
     * Befüllt alle Formularfelder mit den Werten des übergebenen Artikels.
     * Wird beim Öffnen des Bearbeiten-Dialogs aufgerufen.
     *
     * @param artikel der zu bearbeitende Artikel
     */
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

    /**
     * Prüft ob alle Pflichtfelder ausgefüllt sind.
     * Zeigt bei fehlenden Eingaben eine Fehlermeldung über {@link FehlerUI}.
     *
     * @return {@code true} wenn alle Pflichtfelder valide sind, sonst {@code false}
     */
    boolean valide() {
        if (nameFeld.isEmpty()) {
            FehlerUI.fehler("Bitte einen Namen eingeben.");
            return false;
        }
        if (kategorieSelect.isEmpty()) {
            FehlerUI.fehler("Bitte eine Kategorie auswählen.");
            return false;
        }
        if (preisFeld.isEmpty()) {
            FehlerUI.fehler("Bitte einen Preis eingeben.");
            return false;
        }
        if (mwstSelect.isEmpty()) {
            FehlerUI.fehler("Bitte einen MwSt-Satz auswählen.");
            return false;
        }
        if (preisFeld.getValue() < 0) {
            FehlerUI.fehler("Preis darf nicht negativ sein.");
            return false;
        }
        if (!bestandFeld.isEmpty() && bestandFeld.getValue() < 0) {
            FehlerUI.fehler("Bestand darf nicht negativ sein.");
            return false;
        }
        if (!minBestandFeld.isEmpty() && minBestandFeld.getValue() < 0) {
            FehlerUI.fehler("Minimalbestand darf nicht negativ sein.");
            return false;
        }
        return true;
    }

    /**
     * Liest die aktuellen Feldwerte aus und erstellt daraus ein neues {@link Artikel}-Objekt.
     * Fehlende optionale Felder (Bestand, Minimalbestand) werden mit Standardwerten befüllt.
     *
     * @return neues {@code Artikel}-Objekt mit den eingegebenen Werten
     */
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

    /**
     * Erstellt das Kategorie-Auswahlfeld und befüllt es mit allen im System vorhandenen Kategorien.
     *
     * @param service Quelle der Kategorien
     * @return konfiguriertes {@code Select}-Element
     */
    private Select<Kategorie> buildKategorieSelect(ArtikelService service) {
        Select<Kategorie> select = new Select<>();
        select.setWidthFull();
        select.addClassName("dialog-feld");
        try {
            Set<Kategorie> kategorien = new LinkedHashSet<>();
            service.findAllArtikel().forEach(a -> kategorien.add(a.getKategorie()));
            select.setItems(kategorien);
            select.setItemLabelGenerator(Kategorie::getName);
            if (!kategorien.isEmpty()) select.setValue(kategorien.iterator().next());
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
        return select;
    }

    /**
     * Erstellt das MwSt-Auswahlfeld mit allen im System vorhandenen Steuersätzen.
     * Das Label zeigt Satz und Bezeichnung, z.B. "7% (Ermäßigt)".
     *
     * @param service Quelle der Steuersätze
     * @return konfiguriertes {@code Select}-Element
     */
    private Select<Mehrwertsteuer> buildMwstSelect(ArtikelService service) {
        Select<Mehrwertsteuer> select = new Select<>();
        select.setWidthFull();
        select.addClassName("dialog-feld");
        try {
            Set<Mehrwertsteuer> saetze = new LinkedHashSet<>();
            service.findAllArtikel().forEach(a -> saetze.add(a.getMehrwertsteuer()));
            select.setItems(saetze);
            select.setItemLabelGenerator(m ->
                    m.getSatz().stripTrailingZeros().toPlainString() + "% (" + m.getBezeichnung() + ")");
            if (!saetze.isEmpty()) select.setValue(saetze.iterator().next());
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
        return select;
    }

    /**
     * Erstellt das Minimalbestand-Feld mit einem erklärenden Hinweistext darunter.
     *
     * @return Layout mit Eingabefeld und Hinweis
     */
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

    /**
     * Erstellt einen einheitlichen Formularblock aus Label und Eingabefeld.
     *
     * @param label       der Beschriftungstext (wird in Großbuchstaben dargestellt)
     * @param eingabefeld das zugehörige Vaadin-Eingabefeld
     * @return Layout mit Label oben und Feld unten
     */
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

    /**
     * Ordnet zwei Formularblöcke nebeneinander in einem zweispaltigen Layout an.
     *
     * @param links  linker Formularblock
     * @param rechts rechter Formularblock
     * @return horizontales Layout mit gleichmäßiger Aufteilung
     */
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