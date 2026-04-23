package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.service.UserService;
import de.fhswf.kassensystem.views.components.FehlerUI;

import java.util.function.Consumer;

/**
 * Fabrikklasse für einzelne Tabellenzeilen in der Benutzerverwaltung.
 *
 * <p>Jede Zeile enthält: ID, Benutzername, Name, Rollenbadge, Statusanzeige
 * sowie drei Aktions-Buttons (Bearbeiten, Sperren/Entsperren, Passwort-Reset).
 * Alle Buttons sind dauerhaft sichtbar.
 *
 * @author Adrian Krawietz
 */
class BenutzerZeileFactory {

    static final String BREITE_ID       = "10%";
    static final String BREITE_USERNAME = "18%";
    static final String BREITE_NAME     = "20%";
    static final String BREITE_ROLLE    = "15%";
    static final String BREITE_STATUS   = "12%";
    static final String BREITE_AKTIONEN = "25%";

    private BenutzerZeileFactory() {}

    /**
     * Erstellt eine vollständig gestylte Tabellenzeile für den übergebenen Benutzer.
     *
     * @param user             der darzustellende Benutzer
     * @param zebra            {@code true} für abwechselnden Zeilenhintergrund
     * @param userService      Service für Sperren/Entsperren und Aktualisieren
     * @param onAenderung      wird nach jeder Änderung aufgerufen
     * @param onPasswortReset  wird aufgerufen wenn der Passwort-Button geklickt wird
     * @return fertiges Zeilen-Layout
     */
    static HorizontalLayout create(User user, boolean zebra,
                                   UserService userService,
                                   Runnable onAenderung,
                                   Consumer<User> onPasswortReset) {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setPadding(false);
        zeile.setSpacing(false);
        zeile.getStyle()
                .set("background", zebra ? "rgba(245,242,255,0.4)" : "white")
                .set("padding", "1rem 2rem").set("gap", "0").set("transition", "background 0.15s");

        String rolleText = user.getRolle().name().charAt(0)
                + user.getRolle().name().substring(1).toLowerCase();

        Span idZelle = buildTextZelle("#USR-" + String.format("%03d", user.getId()),
                BREITE_ID, "0.7rem", "#82746d", false);
        idZelle.getStyle().set("font-family", "monospace").set("opacity", "0.7");

        Span usernameZelle = buildTextZelle(
                user.getBenutzername() != null ? user.getBenutzername() : "-",
                BREITE_USERNAME, "0.875rem", "#1a1a2e", true);

        Span nameZelle = buildTextZelle(user.getName(), BREITE_NAME, "0.875rem", "#50453e", false);

        Div rolleZelle = new Div(buildRolleBadge(rolleText));
        rolleZelle.getStyle().set("flex", "0 0 " + BREITE_ROLLE).set("min-width", "0");

        HorizontalLayout statusZelle = buildStatusZelle(user.isAktiv());
        statusZelle.getStyle().set("flex", "0 0 " + BREITE_STATUS).set("min-width", "0");

        HorizontalLayout aktionenZelle = buildAktionenZelle(user, userService, onAenderung, onPasswortReset);
        aktionenZelle.getStyle().set("flex", "0 0 " + BREITE_AKTIONEN).set("min-width", "0").set("justify-content", "flex-start");

        String normalBg = zebra ? "rgba(245,242,255,0.4)" : "white";
        zeile.getElement().executeJs(
                "this.addEventListener('mouseenter', () => { this.style.background = '#f0eeff'; });" +
                        "this.addEventListener('mouseleave', () => { this.style.background = '" + normalBg + "'; });");

        zeile.add(idZelle, usernameZelle, nameZelle, rolleZelle, statusZelle, aktionenZelle);
        return zeile;
    }

    /**
     * Erstellt eine einfache Text-Zelle mit definierter Breite und Farbe.
     */
    private static Span buildTextZelle(String text, String breite,
                                       String fontSize, String color, boolean bold) {
        Span span = new Span(text);
        span.getStyle()
                .set("flex", "0 0 " + breite).set("min-width", "0")
                .set("font-size", fontSize).set("color", color)
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        if (bold) span.getStyle().set("font-weight", "600");
        return span;
    }

    /**
     * Erstellt den Rollen-Badge ("Kassierer" oder "Manager") mit passender Farbe.
     *
     * @param rolle Rollenname als String
     */
    private static Span buildRolleBadge(String rolle) {
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

    /**
     * Erstellt die Status-Zelle mit farbigem Punkt und Text ("Aktiv" / "Inaktiv").
     *
     * @param aktiv {@code true} für aktiven, {@code false} für gesperrten Benutzer
     */
    private static HorizontalLayout buildStatusZelle(boolean aktiv) {
        HorizontalLayout status = new HorizontalLayout();
        status.setAlignItems(FlexComponent.Alignment.CENTER);
        status.setSpacing(false);
        status.getStyle().set("gap", "0.5rem");

        Div punkt = new Div();
        punkt.getStyle()
                .set("width", "0.5rem").set("height", "0.5rem").set("border-radius", "9999px")
                .set("background", aktiv ? "#22c55e" : "#94a3b8").set("flex-shrink", "0");

        Span text = new Span(aktiv ? "Aktiv" : "Inaktiv");
        text.getStyle()
                .set("font-size", "0.8rem").set("font-weight", "500")
                .set("color", aktiv ? "#15803d" : "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        status.add(punkt, text);
        return status;
    }

    /**
     * Erstellt die Aktionen-Zelle mit drei Buttons: Bearbeiten, Sperren/Entsperren, Passwort-Reset.
     *
     * @param user            betroffener Benutzer
     * @param userService     Service für Deaktivierung und Update
     * @param onAenderung     Callback nach jeder Änderung
     * @param onPasswortReset Callback für den Passwort-Reset-Button
     */
    private static HorizontalLayout buildAktionenZelle(User user, UserService userService,
                                                       Runnable onAenderung,
                                                       Consumer<User> onPasswortReset) {
        HorizontalLayout zelle = new HorizontalLayout();
        zelle.setAlignItems(FlexComponent.Alignment.CENTER);
        zelle.setSpacing(false);
        zelle.getStyle().set("gap", "0.25rem").set("opacity", "1");

        Button editBtn = buildAktionsButton("edit", "#553722", "#ffdcc6");
        editBtn.getElement().setAttribute("tour-id", "benutzer-bearbeiten-btn");
        editBtn.addClickListener(e -> {
            BenutzerBearbeitenDialog dialog =
                    new BenutzerBearbeitenDialog(user, userService, onAenderung);
            dialog.open();
        });

        Button sperrBtn = buildAktionsButton(
                user.isAktiv() ? "block" : "check_circle",
                user.isAktiv() ? "#ba1a1a" : "#16a34a",
                user.isAktiv() ? "#ffdad6" : "#dcfce7");
        sperrBtn.getElement().setAttribute("tour-id", "benutzer-sperren-btn");
        sperrBtn.addClickListener(e -> {
            try {
                if (user.isAktiv()) {
                    userService.deactivateUser(user.getId());
                    FehlerUI.erfolg("Benutzer gesperrt.");
                } else {
                    user.setAktiv(true);
                    userService.updateUser(user);
                    FehlerUI.erfolg("Benutzer aktiviert.");
                }
                onAenderung.run();
            } catch (KassensystemException ex) {
                FehlerUI.fehler(ex.getMessage());
            } catch (Exception ex) {
                FehlerUI.technischerFehler(ex);
            }
        });

        Button passwortBtn = buildAktionsButton("vpn_key", "#553722", "#efecff");
        passwortBtn.getElement().setAttribute("tour-id", "benutzer-passwort-btn");
        passwortBtn.addClickListener(e -> onPasswortReset.accept(user));

        zelle.add(editBtn, sperrBtn, passwortBtn);
        return zelle;
    }

    /**
     * Erstellt einen Icon-Button mit Hover-Hintergrundfarbe.
     *
     * @param iconName   Material-Symbols-Icon-Name
     * @param iconFarbe  Standardfarbe des Icons
     * @param hoverFarbe Hintergrundfarbe beim Hover
     */
    private static Button buildAktionsButton(String iconName, String iconFarbe, String hoverFarbe) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("font-size", "1.1rem").set("color", iconFarbe);

        Button btn = new Button();
        btn.getElement().appendChild(icon.getElement());
        btn.getStyle()
                .set("background", "none").set("border", "none").set("cursor", "pointer")
                .set("padding", "0.4rem").set("border-radius", "0.5rem")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                .set("min-width", "unset").set("transition", "background 0.15s");
        btn.getElement().executeJs(
                "this.addEventListener('mouseenter', () => this.style.background = '" + hoverFarbe + "');" +
                        "this.addEventListener('mouseleave', () => this.style.background = 'none');");
        return btn;
    }
}