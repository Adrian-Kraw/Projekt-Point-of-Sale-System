package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

/**
 * BenutzerView zeigt alle Benutzer des Cafés in einer Tabelle.
 *
 * Aufbau:
 * - Header: Titel + "Neuer Benutzer" Button
 * - Tabelle: ID, Benutzername, Name, Rolle, Status, Aktionen
 *
 * Im Prototyp mit Dummy-Daten – später mit BenutzerService verbunden.
 * TODO: Spring Security Integration für echte Benutzerverwaltung.
 */
@Route(value = "benutzer", layout = MainLayout.class)
public class BenutzerView extends VerticalLayout {

    /*
     * Spaltenbreiten der Tabelle – zentral definiert.
     * Summe muss 100% ergeben.
     */
    private static final String BREITE_ID         = "10%";
    private static final String BREITE_USERNAME   = "18%";
    private static final String BREITE_NAME       = "20%";
    private static final String BREITE_ROLLE      = "15%";
    private static final String BREITE_STATUS     = "12%";
    private static final String BREITE_AKTIONEN   = "25%";

    public BenutzerView() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box");

        add(
                buildHeader(),
                buildTabellenBereich()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    /**
     * Header mit Titel links und "Neuer Benutzer" Button rechts.
     */
    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(false);
        header.getStyle().set("margin-bottom", "2rem");

        header.add(buildTitel(), buildNeuerBenutzerButton());
        return header;
    }

    /**
     * Titel-Gruppe: Icon-Box + Überschrift "Benutzerverwaltung".
     */
    private HorizontalLayout buildTitel() {
        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "1rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "#e2e0fc")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span icon = createIcon("group");
        icon.getStyle()
                .set("color", "#553722")
                .set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Benutzerverwaltung");
        titel.getStyle()
                .set("margin", "0")
                .set("font-size", "1.75rem")
                .set("font-weight", "800")
                .set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);
        return titelGruppe;
    }

    /**
     * "Neuer Benutzer" Button mit Gradient und Plus-Icon.
     * Öffnet den NeuerBenutzerDialog.
     */
    private Button buildNeuerBenutzerButton() {
        Span plusIcon = createIcon("add");
        plusIcon.getStyle().set("font-size", "1.1rem");

        Span btnText = new Span("Neuer Benutzer");
        btnText.getStyle()
                .set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button btn = new Button();
        btn.getElement().appendChild(plusIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("background", "linear-gradient(135deg, #553722, #6f4e37)")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 1.5rem")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("gap", "0.5rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("white-space", "nowrap");

        btn.addClickListener(e -> buildNeuerBenutzerDialog().open());
        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // TABELLEN-BEREICH
    // ═══════════════════════════════════════════════════════════

    /**
     * Container für die Benutzertabelle.
     */
    private VerticalLayout buildTabellenBereich() {
        VerticalLayout bereich = new VerticalLayout();
        bereich.setWidthFull();
        bereich.setPadding(false);
        bereich.setSpacing(false);
        bereich.getStyle()
                .set("background", "white")
                .set("border-radius", "1.25rem")
                .set("overflow", "hidden");

        bereich.add(buildTabelle());
        return bereich;
    }

    /**
     * Tabelle mit Header und allen Benutzerzeilen.
     */
    private VerticalLayout buildTabelle() {
        VerticalLayout tabelle = new VerticalLayout();
        tabelle.setWidthFull();
        tabelle.setPadding(false);
        tabelle.setSpacing(false);
        tabelle.getStyle().set("gap", "0");

        tabelle.add(buildTabellenHeader());

        /*
         * Dummy-Daten – später durch BenutzerService.findAll() ersetzen.
         * Parameter: id, benutzername, name, rolle, aktiv, zebra
         */
        tabelle.add(buildBenutzerZeile("#USR-001", "max_manager",   "Max Mustermann",  "Manager",   true,  false));
        tabelle.add(buildBenutzerZeile("#USR-002", "sara_sales",    "Sarah Schmidt",   "Kassierer", true,  true));
        tabelle.add(buildBenutzerZeile("#USR-003", "tom_service",   "Thomas Weber",    "Kassierer", false, false));
        tabelle.add(buildBenutzerZeile("#USR-004", "lisa_barista",  "Lisa Meyer",      "Kassierer", true,  true));
        tabelle.add(buildBenutzerZeile("#USR-005", "anna_admin",    "Anna Braun",      "Manager",   true,  false));

        return tabelle;
    }

    /**
     * Header-Zeile der Tabelle mit Spaltenbezeichnungen.
     */
    private HorizontalLayout buildTabellenHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "0.75rem 2rem")
                .set("gap", "0");

        header.add(
                buildHeaderZelle("ID",           BREITE_ID),
                buildHeaderZelle("Benutzername", BREITE_USERNAME),
                buildHeaderZelle("Name",         BREITE_NAME),
                buildHeaderZelle("Rolle",        BREITE_ROLLE),
                buildHeaderZelle("Status",       BREITE_STATUS),
                buildHeaderZelle("Aktionen",     BREITE_AKTIONEN)
        );
        return header;
    }

    /**
     * Einzelne Spaltenüberschrift.
     *
     * @param text   Spaltenbezeichnung
     * @param breite CSS-Breite aus den BREITE_*-Konstanten
     */
    private Span buildHeaderZelle(String text, String breite) {
        Span zelle = new Span(text);
        zelle.getStyle()
                .set("width", breite)
                .set("font-size", "0.65rem")
                .set("font-weight", "800")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return zelle;
    }

    /**
     * Eine Benutzer-Datenzeile in der Tabelle.
     *
     * Hover-Effekt: Hintergrund aufhellen + Aktionsbuttons hervorheben.
     *
     * @param id           Benutzer-ID (z.B. "#USR-001")
     * @param benutzername Login-Name
     * @param name         Vollständiger Name
     * @param rolle        Rolle (z.B. "Manager", "Kassierer")
     * @param aktiv        ob der Benutzer aktiv ist
     * @param zebra        ob diese Zeile den alternativen Hintergrund bekommt
     */
    private HorizontalLayout buildBenutzerZeile(String id, String benutzername,
                                                String name, String rolle,
                                                boolean aktiv, boolean zebra) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("background", zebra ? "rgba(245,242,255,0.4)" : "white")
                .set("padding", "1rem 2rem")
                .set("gap", "0")
                .set("transition", "background 0.15s");

        // ID
        Span idZelle = new Span(id);
        idZelle.getStyle()
                .set("width", BREITE_ID)
                .set("font-size", "0.7rem")
                .set("font-family", "monospace")
                .set("color", "#82746d")
                .set("opacity", "0.7");

        // Benutzername
        Span usernameZelle = new Span(benutzername);
        usernameZelle.getStyle()
                .set("width", BREITE_USERNAME)
                .set("font-weight", "600")
                .set("font-size", "0.875rem")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Name
        Span nameZelle = new Span(name);
        nameZelle.getStyle()
                .set("width", BREITE_NAME)
                .set("font-size", "0.875rem")
                .set("color", "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Rollen-Badge
        Div rolleZelle = new Div(buildRolleBadge(rolle));
        rolleZelle.getStyle().set("width", BREITE_ROLLE);

        // Status mit Punkt
        HorizontalLayout statusZelle = buildStatusZelle(aktiv);
        statusZelle.getStyle().set("width", BREITE_STATUS);

        // Aktionsbuttons
        HorizontalLayout aktionenZelle = buildAktionenZelle();
        aktionenZelle.getStyle()
                .set("width", BREITE_AKTIONEN)
                .set("justify-content", "flex-end");

        /*
         * Hover per JavaScript: Hintergrund + Aktionen-Sichtbarkeit.
         */
        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.background = '#f0eeff';" +
                        "  const btns = this.querySelector('.aktionen-gruppe');" +
                        "  if (btns) btns.style.opacity = '1';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.background = '" + (zebra ? "rgba(245,242,255,0.4)" : "white") + "';" +
                        "  const btns = this.querySelector('.aktionen-gruppe');" +
                        "  if (btns) btns.style.opacity = '0';" +
                        "});"
        );

        zeile.add(idZelle, usernameZelle, nameZelle, rolleZelle, statusZelle, aktionenZelle);
        return zeile;
    }

    /**
     * Rollen-Badge: Manager = braun, Kassierer = grün-beige.
     *
     * @param rolle Rollenbezeichnung
     */
    private Span buildRolleBadge(String rolle) {
        boolean istManager = "Manager".equals(rolle);
        Span badge = new Span(rolle);
        badge.getStyle()
                .set("padding", "0.2rem 0.75rem")
                .set("border-radius", "9999px")
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("letter-spacing", "0.025em")
                .set("background", istManager ? "#ffdcc6" : "#e1e1c9")
                .set("color", istManager ? "#553722" : "#474836");
        return badge;
    }

    /**
     * Status-Anzeige mit farbigem Punkt und Text.
     *
     * @param aktiv ob der Benutzer aktiv ist
     */
    private HorizontalLayout buildStatusZelle(boolean aktiv) {
        HorizontalLayout status = new HorizontalLayout();
        status.setAlignItems(FlexComponent.Alignment.CENTER);
        status.setSpacing(false);
        status.getStyle().set("gap", "0.5rem");

        Div punkt = new Div();
        punkt.getStyle()
                .set("width", "0.5rem")
                .set("height", "0.5rem")
                .set("border-radius", "9999px")
                .set("background", aktiv ? "#22c55e" : "#94a3b8")
                .set("flex-shrink", "0");

        Span text = new Span(aktiv ? "Aktiv" : "Inaktiv");
        text.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "500")
                .set("color", aktiv ? "#15803d" : "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        status.add(punkt, text);
        return status;
    }

    /**
     * Aktionsbuttons: Bearbeiten, Sperren, Passwort.
     * Standardmäßig unsichtbar – erscheinen beim Hover der Zeile.
     */
    private HorizontalLayout buildAktionenZelle() {
        HorizontalLayout zelle = new HorizontalLayout();
        zelle.setAlignItems(FlexComponent.Alignment.CENTER);
        zelle.addClassName("aktionen-gruppe");
        zelle.setSpacing(false);
        zelle.getStyle()
                .set("gap", "0.25rem")
                .set("opacity", "0")
                .set("transition", "opacity 0.15s");

        zelle.add(
                buildAktionsButton("edit",    "#553722", "#ffdcc6"),
                buildAktionsButton("block",   "#ba1a1a", "#ffdad6"),
                buildAktionsButton("vpn_key", "#553722", "#efecff")
        );
        return zelle;
    }

    /**
     * Einzelner Icon-only Aktionsbutton.
     *
     * @param iconName   Material Symbol Icon-Name
     * @param iconFarbe  Farbe des Icons
     * @param hoverFarbe Hintergrundfarbe beim Hover
     */
    private Button buildAktionsButton(String iconName, String iconFarbe,
                                      String hoverFarbe) {
        Span icon = createIcon(iconName);
        icon.getStyle()
                .set("font-size", "1.1rem")
                .set("color", iconFarbe);

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0.4rem")
                .set("border-radius", "0.5rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("min-width", "unset")
                .set("transition", "background 0.15s");

        btn.getElement().executeJs(
                "this.addEventListener('mouseenter', () => this.style.background = '" + hoverFarbe + "');" +
                        "this.addEventListener('mouseleave', () => this.style.background = 'none');"
        );

        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // NEUER BENUTZER DIALOG
    // ═══════════════════════════════════════════════════════════

    /**
     * Dialog zum Erstellen eines neuen Benutzers.
     *
     * Felder: Benutzername, Rolle, Vollständiger Name, Passwort.
     * Im Prototyp ohne Speicher-Logik.
     * TODO: BenutzerService.save() + Spring Security einbinden.
     */
    private Dialog buildNeuerBenutzerDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("32rem");
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "0");

        layout.add(
                buildDialogHeader(dialog),
                buildDialogBody(dialog)
        );

        dialog.add(layout);

        /*
         * Dialog-Overlay per JavaScript stylen:
         * Vaadin rendert das Overlay im Shadow DOM –
         * padding:0 und overflow:hidden sorgen für bündige Ränder.
         */
        dialog.getElement().executeJs(
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

        return dialog;
    }

    /**
     * Dialog-Header mit Titel und Schließen-Button.
     *
     * @param dialog Referenz zum Schließen bei Klick auf X
     */
    private HorizontalLayout buildDialogHeader(Dialog dialog) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "1.25rem 1.5rem");

        VerticalLayout titelBlock = new VerticalLayout();
        titelBlock.setPadding(false);
        titelBlock.setSpacing(false);

        Span titel = new Span("Neuer Benutzer");
        titel.getStyle()
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span untertitel = new Span("Erstellen Sie ein neues Profil für Ihr Team.");
        untertitel.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelBlock.add(titel, untertitel);

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
        closeBtn.addClickListener(e -> dialog.close());

        header.add(titelBlock, closeBtn);
        return header;
    }

    /**
     * Dialog-Body mit allen Eingabefeldern und Footer-Buttons.
     *
     * Felder:
     * - Benutzername + Rolle (zwei Spalten)
     * - Vollständiger Name (volle Breite)
     * - Passwort (volle Breite)
     *
     * @param dialog Referenz zum Schließen bei Abbrechen
     */
    private VerticalLayout buildDialogBody(Dialog dialog) {
        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle()
                .set("background", "white")
                .set("gap", "0");

        VerticalLayout felder = new VerticalLayout();
        felder.setWidthFull();
        felder.setPadding(false);
        felder.setSpacing(false);
        felder.getStyle()
                .set("padding", "1.5rem")
                .set("gap", "1.25rem");

        // Benutzername + Rolle nebeneinander
        HorizontalLayout zeile1 = new HorizontalLayout();
        zeile1.setWidthFull();
        zeile1.setSpacing(false);
        zeile1.getStyle().set("gap", "1rem");

        VerticalLayout usernameBlock = buildDialogFeld("BENUTZERNAME", buildUsernameTextField());
        usernameBlock.getStyle().set("flex", "1");

        VerticalLayout rolleBlock = buildDialogFeld("ROLLE", buildRolleSelect());
        rolleBlock.getStyle().set("flex", "1");

        zeile1.add(usernameBlock, rolleBlock);

        // Name (volle Breite)
        VerticalLayout nameBlock = buildDialogFeld("VOLLSTÄNDIGER NAME", buildNameTextField());

        // Passwort (volle Breite)
        VerticalLayout passwortBlock = buildDialogFeld("PASSWORT", buildPasswortTextField());

        felder.add(zeile1, nameBlock, passwortBlock);

        // Footer
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        footer.setSpacing(false);
        footer.getStyle()
                .set("background", "#f5f2ff")
                .set("padding", "1.25rem 1.5rem")
                .set("gap", "1rem");

        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("background", "transparent")
                .set("border", "2px solid rgba(85,55,34,0.2)")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 2rem")
                .set("font-weight", "700")
                .set("color", "#553722")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("flex", "1");
        abbrechenBtn.addClickListener(e -> dialog.close());

        Button speichernBtn = new Button("Erstellen");
        speichernBtn.getStyle()
                .set("background", "#553722")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 2rem")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("box-shadow", "0 4px 15px rgba(85,55,34,0.25)")
                .set("flex", "1");
        speichernBtn.addClickListener(e -> dialog.close());

        footer.add(abbrechenBtn, speichernBtn);
        body.add(felder, footer);
        return body;
    }

    /**
     * Label + Eingabefeld als Block.
     *
     * @param label       Feldbezeichnung (wird uppercase dargestellt)
     * @param eingabefeld das Vaadin-Eingabefeld
     */
    private VerticalLayout buildDialogFeld(String label,
                                           com.vaadin.flow.component.Component eingabefeld) {
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

    private TextField buildUsernameTextField() {
        TextField feld = new TextField();
        feld.setWidthFull();
        feld.setPlaceholder("z.B. m.mustermann");
        feld.addClassName("dialog-feld");
        return feld;
    }

    private Select<String> buildRolleSelect() {
        Select<String> select = new Select<>();
        select.setWidthFull();
        select.setItems("Manager", "Kassierer");
        select.setValue("Kassierer");
        select.addClassName("dialog-feld");
        return select;
    }

    private TextField buildNameTextField() {
        TextField feld = new TextField();
        feld.setWidthFull();
        feld.setPlaceholder("Vorname Nachname");
        feld.addClassName("dialog-feld");
        return feld;
    }

    private TextField buildPasswortTextField() {
        TextField feld = new TextField();
        feld.setWidthFull();
        feld.setPlaceholder("••••••••");
        feld.addClassName("dialog-feld");
        return feld;
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