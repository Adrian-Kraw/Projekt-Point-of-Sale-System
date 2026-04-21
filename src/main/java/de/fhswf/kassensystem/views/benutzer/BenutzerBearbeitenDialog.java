package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.service.UserService;
import de.fhswf.kassensystem.views.components.FehlerUI;
import de.fhswf.kassensystem.views.components.BaseDialog;

/**
 * Dialog zum Bearbeiten eines bestehenden Benutzers (Benutzername, Name, Rolle).
 *
 * <p>Nach erfolgreichem Speichern wird der übergebene
 * {@code onErfolg}-Callback aufgerufen, um die Tabelle neu zu laden.
 *
 * @author Adrian Krawietz
 */
class BenutzerBearbeitenDialog extends BaseDialog {

    private final UserService   userService;
    private final User          user;
    private final Runnable      onErfolg;

    private final TextField     usernameFeld  = new TextField();
    private final TextField     nameFeld      = new TextField();
    private final Select<Rolle> rolleSelect   = new Select<>();

    /**
     * Erstellt den Dialog und befüllt alle Felder mit den aktuellen Benutzerdaten.
     *
     * @param user        der zu bearbeitende Benutzer
     * @param userService Service für das Speichern der Änderungen
     * @param onErfolg    wird nach erfolgreichem Speichern aufgerufen
     */
    BenutzerBearbeitenDialog(User user, UserService userService, Runnable onErfolg) {
        this.user        = user;
        this.userService = userService;
        this.onErfolg    = onErfolg;
        init("Benutzer bearbeiten", user.getBenutzername());
    }

    /**
     * Erstellt den Dialog-Body mit Benutzername, Rolle (zweispaltig) und vollständigem Namen.
     */
    @Override
    protected VerticalLayout buildBody() {
        usernameFeld.setWidthFull();
        usernameFeld.setPlaceholder("z.B. m.mustermann");
        usernameFeld.setValue(user.getBenutzername() != null ? user.getBenutzername() : "");
        usernameFeld.addClassName("dialog-feld");

        nameFeld.setWidthFull();
        nameFeld.setPlaceholder("Vorname Nachname");
        nameFeld.setValue(user.getName() != null ? user.getName() : "");
        nameFeld.addClassName("dialog-feld");

        rolleSelect.setWidthFull();
        rolleSelect.setItems(Rolle.values());
        rolleSelect.setItemLabelGenerator(r -> r.name().charAt(0) + r.name().substring(1).toLowerCase());
        rolleSelect.setValue(user.getRolle());
        rolleSelect.addClassName("dialog-feld");

        HorizontalLayout zeile1 = new HorizontalLayout();
        zeile1.setWidthFull();
        zeile1.setSpacing(false);
        zeile1.getStyle().set("gap", "1rem");
        VerticalLayout ub = feldMitLabel("BENUTZERNAME", usernameFeld);
        ub.getStyle().set("flex", "1");
        VerticalLayout rb = feldMitLabel("ROLLE", rolleSelect);
        rb.getStyle().set("flex", "1");
        zeile1.add(ub, rb);

        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("background", "white").set("padding", "1.5rem").set("gap", "1.25rem");
        body.add(zeile1, feldMitLabel("VOLLSTÄNDIGER NAME", nameFeld));
        return body;
    }

    /**
     * Validiert die Eingaben und aktualisiert den Benutzer in der Datenbank.
     *
     * @return {@code true} bei Erfolg, {@code false} wenn der Name fehlt
     */
    @Override
    protected boolean onSpeichern() {
        if (nameFeld.isEmpty()) {
            FehlerUI.fehler("Bitte einen Namen eingeben.");
            return false;
        }
        user.setBenutzername(usernameFeld.getValue().trim());
        user.setName(nameFeld.getValue().trim());
        user.setRolle(rolleSelect.getValue());
        return FehlerUI.versuch(() -> {
            userService.updateUser(user);
            FehlerUI.erfolg("Benutzer \"" + user.getName() + "\" aktualisiert.");
            onErfolg.run();
        });
    }

    @Override protected String getSpeichernLabel() { return "Speichern"; }
    @Override protected String getDialogBreite()   { return "34rem"; }
}