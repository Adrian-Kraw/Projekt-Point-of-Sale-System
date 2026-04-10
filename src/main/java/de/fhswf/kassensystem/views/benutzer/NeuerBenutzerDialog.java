package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.service.UserService;
import de.fhswf.kassensystem.views.components.BaseDialog;

class NeuerBenutzerDialog extends BaseDialog {

    private final UserService userService;
    private final Runnable    onErfolg;

    private final TextField     usernameFeld  = new TextField();
    private final TextField     nameFeld      = new TextField();
    private final PasswordField passwortFeld  = new PasswordField();
    private final Select<Rolle> rolleSelect   = new Select<>();

    NeuerBenutzerDialog(UserService userService, Runnable onErfolg) {
        this.userService = userService;
        this.onErfolg    = onErfolg;
        init("Neuer Benutzer", "Erstellen Sie ein neues Profil für Ihr Team.");
    }

    @Override
    protected VerticalLayout buildBody() {
        usernameFeld.setWidthFull();
        usernameFeld.setPlaceholder("z.B. m.mustermann");
        usernameFeld.addClassName("dialog-feld");

        nameFeld.setWidthFull();
        nameFeld.setPlaceholder("Vorname Nachname");
        nameFeld.addClassName("dialog-feld");

        passwortFeld.setWidthFull();
        passwortFeld.setPlaceholder("Passwort");
        passwortFeld.addClassName("dialog-feld");

        rolleSelect.setWidthFull();
        rolleSelect.setItems(Rolle.values());
        rolleSelect.setItemLabelGenerator(r -> r.name().charAt(0) + r.name().substring(1).toLowerCase());
        rolleSelect.setValue(Rolle.KASSIERER);
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
        body.add(zeile1, feldMitLabel("VOLLSTÄNDIGER NAME", nameFeld),
                feldMitLabel("PASSWORT", passwortFeld));
        return body;
    }

    @Override
    protected boolean onSpeichern() {
        if (nameFeld.isEmpty() || passwortFeld.isEmpty()) {
            Notification.show("Bitte Name und Passwort eingeben.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        User u = new User();
        u.setBenutzername(usernameFeld.getValue().trim());
        u.setName(nameFeld.getValue().trim());
        u.setPassword(passwortFeld.getValue());
        u.setRolle(rolleSelect.getValue());
        u.setAktiv(true);
        userService.createUser(u);
        Notification.show("Benutzer \"" + u.getName() + "\" erstellt.", 3000,
                Notification.Position.BOTTOM_START);
        onErfolg.run();
        return true;
    }

    @Override protected String getSpeichernLabel() { return "Erstellen"; }
}