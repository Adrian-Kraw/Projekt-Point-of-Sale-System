package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.service.UserService;
import de.fhswf.kassensystem.views.components.BaseDialog;

class PasswortDialog extends BaseDialog {

    private final UserService userService;
    private final User        user;
    private final Runnable    onErfolg;
    private final PasswordField neuesPasswort = new PasswordField();

    PasswortDialog(User user, UserService userService, Runnable onErfolg) {
        this.user        = user;
        this.userService = userService;
        this.onErfolg    = onErfolg;
        init("Passwort zurücksetzen – " + user.getName(), null);
    }

    @Override
    protected VerticalLayout buildBody() {
        neuesPasswort.setWidthFull();
        neuesPasswort.setPlaceholder("Neues Passwort");
        neuesPasswort.addClassName("dialog-feld");

        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("background", "white").set("padding", "1.5rem").set("gap", "1.25rem");
        body.add(feldMitLabel("NEUES PASSWORT", neuesPasswort));
        return body;
    }

    @Override
    protected boolean onSpeichern() {
        if (neuesPasswort.isEmpty()) {
            Notification.show("Bitte ein neues Passwort eingeben.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        userService.resetPasswort(user.getId(), neuesPasswort.getValue());
        Notification.show("Passwort wurde zurückgesetzt.", 3000, Notification.Position.BOTTOM_START);
        onErfolg.run();
        return true;
    }

    @Override protected String getSpeichernLabel() { return "Zurücksetzen"; }
}