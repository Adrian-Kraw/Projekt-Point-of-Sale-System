package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.views.SecuredView;
import de.fhswf.kassensystem.service.UserService;
import de.fhswf.kassensystem.views.MainLayout;

import java.util.List;

/**
 * BenutzerView zeigt alle Benutzer des Cafés in einer Tabelle.
 *
 * Aufbau:
 * - Header: Titel + "Neuer Benutzer" Button
 * - Tabelle: ID, Benutzername, Name, Rolle, Status, Aktionen
 *
 * Daten kommen aus UserService.
 */
@Route(value = "benutzer", layout = MainLayout.class)
public class BenutzerView extends SecuredView {

    private static final String BREITE_ID       = "10%";
    private static final String BREITE_USERNAME = "18%";
    private static final String BREITE_NAME     = "20%";
    private static final String BREITE_ROLLE    = "15%";
    private static final String BREITE_STATUS   = "12%";
    private static final String BREITE_AKTIONEN = "25%";

    private final UserService userService;
    private final VerticalLayout tabelle = new VerticalLayout();

    public BenutzerView(UserService userService) {
        super(Rolle.MANAGER);
        this.userService = userService;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box");

        tabelle.setWidthFull();
        tabelle.setPadding(false);
        tabelle.setSpacing(false);
        tabelle.getStyle().set("gap", "0");

        add(buildHeader(), buildTabellenBereich());
        ladeBenutzer();
    }

    // ═══════════════════════════════════════════════════════════
    // DATEN LADEN
    // ═══════════════════════════════════════════════════════════

    private void ladeBenutzer() {
        tabelle.removeAll();
        tabelle.add(buildTabellenHeader());

        List<User> benutzer = userService.findAllUsers();
        boolean zebra = false;
        for (User u : benutzer) {
            String rolleText = u.getRolle().name().charAt(0) +
                    u.getRolle().name().substring(1).toLowerCase();
            tabelle.add(buildBenutzerZeile(
                    "#USR-" + String.format("%03d", u.getId()),
                    u.getBenutzername() != null ? u.getBenutzername() : "-",
                    u.getName(), rolleText, u.isAktiv(), zebra, u));
            zebra = !zebra;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

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

    private HorizontalLayout buildTitel() {
        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "1rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "#e2e0fc").set("border-radius", "1rem")
                .set("padding", "0.75rem").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");

        Span icon = createIcon("group");
        icon.getStyle().set("color", "#553722").set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Benutzerverwaltung");
        titel.getStyle()
                .set("margin", "0").set("font-size", "1.75rem")
                .set("font-weight", "800").set("color", "#1a1a2e")
                .set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);
        return titelGruppe;
    }

    private Button buildNeuerBenutzerButton() {
        Span plusIcon = createIcon("add");
        plusIcon.getStyle().set("font-size", "1.1rem");

        Span btnText = new Span("Neuer Benutzer");
        btnText.getStyle().set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button btn = new Button();
        btn.getElement().appendChild(plusIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("background", "linear-gradient(135deg, #553722, #6f4e37)")
                .set("color", "white").set("border", "none")
                .set("border-radius", "1rem").set("padding", "0.75rem 1.5rem")
                .set("cursor", "pointer").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("gap", "0.5rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("white-space", "nowrap");

        btn.addClickListener(e -> {
            Dialog dialog = buildNeuerBenutzerDialog();
            dialog.addOpenedChangeListener(ev -> { if (!ev.isOpened()) ladeBenutzer(); });
            dialog.open();
        });
        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // TABELLE
    // ═══════════════════════════════════════════════════════════

    private VerticalLayout buildTabellenBereich() {
        VerticalLayout bereich = new VerticalLayout();
        bereich.setWidthFull();
        bereich.setPadding(false);
        bereich.setSpacing(false);
        bereich.getStyle()
                .set("background", "white").set("border-radius", "1.25rem")
                .set("overflow", "hidden");
        bereich.add(tabelle);
        return bereich;
    }

    private HorizontalLayout buildTabellenHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle()
                .set("background", "#f5f2ff").set("padding", "0.75rem 2rem").set("gap", "0");
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

    private Span buildHeaderZelle(String text, String breite) {
        Span zelle = new Span(text);
        zelle.getStyle()
                .set("width", breite).set("font-size", "0.65rem")
                .set("font-weight", "800").set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        return zelle;
    }

    private HorizontalLayout buildBenutzerZeile(String id, String benutzername,
                                                String name, String rolle,
                                                boolean aktiv, boolean zebra, User user) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("background", zebra ? "rgba(245,242,255,0.4)" : "white")
                .set("padding", "1rem 2rem").set("gap", "0")
                .set("transition", "background 0.15s");

        Span idZelle = new Span(id);
        idZelle.getStyle()
                .set("width", BREITE_ID).set("font-size", "0.7rem")
                .set("font-family", "monospace").set("color", "#82746d").set("opacity", "0.7");

        Span usernameZelle = new Span(benutzername);
        usernameZelle.getStyle()
                .set("width", BREITE_USERNAME).set("font-weight", "600")
                .set("font-size", "0.875rem").set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span nameZelle = new Span(name);
        nameZelle.getStyle()
                .set("width", BREITE_NAME).set("font-size", "0.875rem")
                .set("color", "#50453e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Div rolleZelle = new Div(buildRolleBadge(rolle));
        rolleZelle.getStyle().set("width", BREITE_ROLLE);

        HorizontalLayout statusZelle = buildStatusZelle(aktiv);
        statusZelle.getStyle().set("width", BREITE_STATUS);

        HorizontalLayout aktionenZelle = buildAktionenZelle(aktiv, user);
        aktionenZelle.getStyle()
                .set("width", BREITE_AKTIONEN).set("justify-content", "flex-end");

        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.background = '#f0eeff';" +
                        "  const b = this.querySelector('.aktionen-gruppe');" +
                        "  if (b) b.style.opacity = '1';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.background = '" + (zebra ? "rgba(245,242,255,0.4)" : "white") + "';" +
                        "  const b = this.querySelector('.aktionen-gruppe');" +
                        "  if (b) b.style.opacity = '0';" +
                        "});"
        );

        zeile.add(idZelle, usernameZelle, nameZelle, rolleZelle, statusZelle, aktionenZelle);
        return zeile;
    }

    private Span buildRolleBadge(String rolle) {
        boolean istManager = "Manager".equals(rolle);
        Span badge = new Span(rolle);
        badge.getStyle()
                .set("padding", "0.2rem 0.75rem").set("border-radius", "9999px")
                .set("font-size", "0.7rem").set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("background", istManager ? "#ffdcc6" : "#e1e1c9")
                .set("color", istManager ? "#553722" : "#474836");
        return badge;
    }

    private HorizontalLayout buildStatusZelle(boolean aktiv) {
        HorizontalLayout status = new HorizontalLayout();
        status.setAlignItems(FlexComponent.Alignment.CENTER);
        status.setSpacing(false);
        status.getStyle().set("gap", "0.5rem");

        Div punkt = new Div();
        punkt.getStyle()
                .set("width", "0.5rem").set("height", "0.5rem")
                .set("border-radius", "9999px")
                .set("background", aktiv ? "#22c55e" : "#94a3b8").set("flex-shrink", "0");

        Span text = new Span(aktiv ? "Aktiv" : "Inaktiv");
        text.getStyle()
                .set("font-size", "0.8rem").set("font-weight", "500")
                .set("color", aktiv ? "#15803d" : "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        status.add(punkt, text);
        return status;
    }

    private HorizontalLayout buildAktionenZelle(boolean aktiv, User user) {
        HorizontalLayout zelle = new HorizontalLayout();
        zelle.setAlignItems(FlexComponent.Alignment.CENTER);
        zelle.addClassName("aktionen-gruppe");
        zelle.setSpacing(false);
        zelle.getStyle()
                .set("gap", "0.25rem").set("opacity", "0")
                .set("transition", "opacity 0.15s");

        Button editBtn = buildAktionsButton("edit", "#553722", "#ffdcc6");
        editBtn.addClickListener(e ->
                Notification.show("Bearbeiten kommt bald.", 2000,
                        Notification.Position.BOTTOM_START));

        Button sperrBtn = buildAktionsButton(
                aktiv ? "block" : "check_circle",
                aktiv ? "#ba1a1a" : "#16a34a",
                aktiv ? "#ffdad6" : "#dcfce7");
        sperrBtn.addClickListener(e -> {
            if (aktiv) {
                userService.deactivateUser(user.getId());
                Notification.show("Benutzer gesperrt.", 2000,
                        Notification.Position.BOTTOM_START);
            } else {
                user.setAktiv(true);
                userService.updateUser(user);
                Notification.show("Benutzer aktiviert.", 2000,
                        Notification.Position.BOTTOM_START);
            }
            ladeBenutzer();
        });

        Button passwortBtn = buildAktionsButton("vpn_key", "#553722", "#efecff");
        passwortBtn.addClickListener(e -> {
            Dialog d = buildPasswortDialog(user);
            d.addOpenedChangeListener(ev -> { if (!ev.isOpened()) ladeBenutzer(); });
            d.open();
        });

        zelle.add(editBtn, sperrBtn, passwortBtn);
        return zelle;
    }

    private Button buildAktionsButton(String iconName, String iconFarbe, String hoverFarbe) {
        Span icon = createIcon(iconName);
        icon.getStyle().set("font-size", "1.1rem").set("color", iconFarbe);

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("padding", "0.4rem")
                .set("border-radius", "0.5rem").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("min-width", "unset").set("transition", "background 0.15s");

        btn.getElement().executeJs(
                "this.addEventListener('mouseenter', () => this.style.background = '" + hoverFarbe + "');" +
                        "this.addEventListener('mouseleave', () => this.style.background = 'none');");
        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // DIALOGE
    // ═══════════════════════════════════════════════════════════

    private Dialog buildNeuerBenutzerDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("32rem");
        dialog.setCloseOnOutsideClick(true);

        TextField usernameFeld = new TextField();
        usernameFeld.setWidthFull();
        usernameFeld.setPlaceholder("z.B. m.mustermann");
        usernameFeld.addClassName("dialog-feld");

        Select<Rolle> rolleSelect = new Select<>();
        rolleSelect.setWidthFull();
        rolleSelect.setItems(Rolle.values());
        rolleSelect.setItemLabelGenerator(r ->
                r.name().charAt(0) + r.name().substring(1).toLowerCase());
        rolleSelect.setValue(Rolle.KASSIERER);
        rolleSelect.addClassName("dialog-feld");

        TextField nameFeld = new TextField();
        nameFeld.setWidthFull();
        nameFeld.setPlaceholder("Vorname Nachname");
        nameFeld.addClassName("dialog-feld");

        PasswordField passwortFeld = new PasswordField();
        passwortFeld.setWidthFull();
        passwortFeld.setPlaceholder("Passwort");
        passwortFeld.addClassName("dialog-feld");

        HorizontalLayout zeile1 = new HorizontalLayout();
        zeile1.setWidthFull();
        zeile1.setSpacing(false);
        zeile1.getStyle().set("gap", "1rem");
        VerticalLayout ub = buildDialogFeld("BENUTZERNAME", usernameFeld);
        ub.getStyle().set("flex", "1");
        VerticalLayout rb = buildDialogFeld("ROLLE", rolleSelect);
        rb.getStyle().set("flex", "1");
        zeile1.add(ub, rb);

        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle()
                .set("background", "white").set("padding", "1.5rem").set("gap", "1.25rem");
        body.add(zeile1, buildDialogFeld("VOLLSTÄNDIGER NAME", nameFeld),
                buildDialogFeld("PASSWORT", passwortFeld));

        dialog.add(buildDialogLayout(dialog, "Neuer Benutzer",
                "Erstellen Sie ein neues Profil für Ihr Team.", body,
                () -> {
                    if (nameFeld.isEmpty() || passwortFeld.isEmpty()) {
                        Notification.show("Bitte Name und Passwort eingeben.",
                                3000, Notification.Position.MIDDLE);
                        return false;
                    }
                    User u = new User();
                    u.setBenutzername(usernameFeld.getValue().trim());
                    u.setName(nameFeld.getValue().trim());
                    u.setPassword(passwortFeld.getValue());
                    u.setRolle(rolleSelect.getValue());
                    u.setAktiv(true);
                    userService.createUser(u);
                    Notification.show("Benutzer \"" + u.getName() + "\" erstellt.",
                            3000, Notification.Position.BOTTOM_START);
                    return true;
                }));
        styleDialogOverlay(dialog);
        return dialog;
    }

    private Dialog buildPasswortDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setWidth("28rem");
        dialog.setCloseOnOutsideClick(true);

        PasswordField neuesPasswort = new PasswordField();
        neuesPasswort.setWidthFull();
        neuesPasswort.setPlaceholder("Neues Passwort");
        neuesPasswort.addClassName("dialog-feld");

        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle()
                .set("background", "white").set("padding", "1.5rem").set("gap", "1.25rem");
        body.add(buildDialogFeld("NEUES PASSWORT", neuesPasswort));

        dialog.add(buildDialogLayout(dialog, "Passwort zurücksetzen – " + user.getName(),
                null, body,
                () -> {
                    if (neuesPasswort.isEmpty()) {
                        Notification.show("Bitte ein neues Passwort eingeben.",
                                3000, Notification.Position.MIDDLE);
                        return false;
                    }
                    userService.resetPasswort(user.getId(), neuesPasswort.getValue());
                    Notification.show("Passwort wurde zurückgesetzt.",
                            3000, Notification.Position.BOTTOM_START);
                    return true;
                }));
        styleDialogOverlay(dialog);
        return dialog;
    }

    /**
     * Generisches Dialog-Layout: Header + Body + Footer.
     * Spart Duplikation zwischen den Dialogen.
     *
     * @param dialog     der Dialog selbst (für close())
     * @param titelText  Titel im Header
     * @param subtitelText Untertitel (oder null)
     * @param body       Body-Layout
     * @param speichern  Lambda das beim Speichern ausgeführt wird; gibt true bei Erfolg zurück
     */
    private VerticalLayout buildDialogLayout(Dialog dialog, String titelText,
                                             String subtitelText,
                                             VerticalLayout body,
                                             java.util.function.Supplier<Boolean> speichern) {
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

        VerticalLayout titelBlock = new VerticalLayout();
        titelBlock.setPadding(false);
        titelBlock.setSpacing(false);

        Span titel = new Span(titelText);
        titel.getStyle()
                .set("font-size", "1rem").set("font-weight", "700").set("color", "#553722")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        titelBlock.add(titel);

        if (subtitelText != null) {
            Span sub = new Span(subtitelText);
            sub.getStyle()
                    .set("font-size", "0.75rem").set("color", "#82746d")
                    .set("font-family", "'Plus Jakarta Sans', sans-serif");
            titelBlock.add(sub);
        }

        Button closeBtn = new Button();
        closeBtn.getElement().appendChild(createIcon("close").getElement());
        closeBtn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("padding", "0.4rem").set("border-radius", "9999px")
                .set("min-width", "unset").set("color", "#553722");
        closeBtn.addClickListener(e -> dialog.close());
        header.add(titelBlock, closeBtn);

        // Footer
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setSpacing(false);
        footer.getStyle()
                .set("background", "#f5f2ff").set("padding", "1.25rem 1.5rem").set("gap", "1rem");

        Button abbrechenBtn = new Button("Abbrechen");
        abbrechenBtn.getStyle()
                .set("background", "transparent")
                .set("border", "2px solid rgba(85,55,34,0.2)")
                .set("border-radius", "1rem").set("padding", "0.75rem 2rem")
                .set("font-weight", "700").set("color", "#553722").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("flex", "1");
        abbrechenBtn.addClickListener(e -> dialog.close());

        Button speichernBtn = new Button("Speichern");
        speichernBtn.getStyle()
                .set("background", "#553722").set("color", "white").set("border", "none")
                .set("border-radius", "1rem").set("padding", "0.75rem 2rem")
                .set("font-weight", "700").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("box-shadow", "0 4px 15px rgba(85,55,34,0.25)").set("flex", "1");
        speichernBtn.addClickListener(e -> {
            if (Boolean.TRUE.equals(speichern.get())) dialog.close();
        });

        footer.add(abbrechenBtn, speichernBtn);
        layout.add(header, body, footer);
        return layout;
    }

    /** Stylefix für Dialog Shadow DOM */
    private void styleDialogOverlay(Dialog dialog) {
        dialog.getElement().executeJs(
                "setTimeout(() => {" +
                        "  const o = this.$.overlay;" +
                        "  if (o) { o.style.padding='0'; o.style.borderRadius='1rem'; o.style.overflow='hidden'; }" +
                        "  const c = this.$.overlay.$.content;" +
                        "  if (c) { c.style.padding='0'; c.style.borderRadius='1rem'; c.style.overflow='hidden'; }" +
                        "}, 50);"
        );
    }

    private VerticalLayout buildDialogFeld(String label,
                                           com.vaadin.flow.component.Component feld) {
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

    private Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }
}