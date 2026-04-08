package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Login-Seite des Kassensystems.
 *
 * Verwendet Vaadins eingebautes LoginForm-Komponente, die
 * direkt mit Spring Security zusammenarbeitet (POST /login).
 *
 * @AnonymousAllowed damit nicht eingeloggte User die Seite sehen können.
 */
@Route("login")
@PageTitle("Login – Kassensystem")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "#fcf8ff");

        // Logo / Icon Box
        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "linear-gradient(135deg, #553722, #6f4e37)")
                .set("border-radius", "1.5rem")
                .set("padding", "1.25rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "1rem");

        Span icon = new Span("point_of_sale");
        icon.addClassName("material-symbols-outlined");
        icon.getStyle()
                .set("color", "white")
                .set("font-size", "2.5rem")
                .set("line-height", "1");
        iconBox.add(icon);

        H2 titel = new H2("Kassensystem");
        titel.getStyle()
                .set("margin", "0 0 0.25rem 0")
                .set("font-size", "1.75rem")
                .set("font-weight", "800")
                .set("color", "#1a1a2e")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Paragraph untertitel = new Paragraph("Bitte melden Sie sich an");
        untertitel.getStyle()
                .set("margin", "0 0 2rem 0")
                .set("color", "#82746d")
                .set("font-size", "0.9rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Deutsches LoginForm
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("");
        i18n.getForm().setUsername("Benutzername");
        i18n.getForm().setPassword("Passwort");
        i18n.getForm().setSubmit("Anmelden");
        i18n.getErrorMessage().setTitle("Anmeldung fehlgeschlagen");
        i18n.getErrorMessage().setMessage(
                "Benutzername oder Passwort ist falsch. Bitte erneut versuchen.");

        loginForm.setI18n(i18n);
        loginForm.setAction("login");  // POST /login → Spring Security
        loginForm.getStyle()
                .set("border-radius", "1.25rem")
                .set("box-shadow", "0 8px 32px rgba(85,55,34,0.12)");

        add(iconBox, titel, untertitel, loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // ?error Parameter setzen wenn Login fehlschlägt
        if (event.getLocation().getQueryParameters()
                .getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}