package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.service.UserService;
import de.fhswf.kassensystem.views.components.FehlerUI;
import de.fhswf.kassensystem.views.MainLayout;
import de.fhswf.kassensystem.views.components.AbstractTabellenView;
import java.util.List;

/**
 * Benutzerverwaltungs-View – zeigt alle Benutzer-Accounts in einer Tabelle
 * und erlaubt Anlegen, Bearbeiten, Passwort-Reset und Sperren/Entsperren.
 *
 * <p>Nur für Benutzer mit der Rolle {@code MANAGER} zugänglich.
 *
 * @author Adrian Krawietz
 */
@Route(value = "benutzer", layout = MainLayout.class)
public class BenutzerView extends AbstractTabellenView {

    private final UserService userService;

    /**
     * Erstellt die View und lädt alle Benutzer.
     *
     * @param userService Service für alle Benutzer-Operationen
     */
    public BenutzerView(UserService userService) {
        super(Rolle.MANAGER);
        this.userService = userService;
        add(buildHeader(), buildTabellenBereich());
        ladeDaten();
    }

    /**
     * Baut den Seitenkopf mit Titel und "Neuer Benutzer"-Button.
     */
    @Override
    protected HorizontalLayout buildHeader() {
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
     * Lädt alle Benutzer aus der Datenbank und befüllt die Tabelle mit Zebra-Streifen.
     */
    @Override
    public void ladeDaten() {
        tabelle.removeAll();
        tabelle.add(buildTabellenHeader());
        try {
            List<User> benutzer = userService.findAllUsers();
            boolean zebra = false;
            for (User u : benutzer) {
                tabelle.add(BenutzerZeileFactory.create(u, zebra, userService,
                        this::ladeDaten, this::oeffnePasswortDialog));
                zebra = !zebra;
            }
        } catch (KassensystemException ex) {
            FehlerUI.fehler(ex.getMessage());
        } catch (Exception ex) {
            FehlerUI.technischerFehler(ex);
        }
    }

    /**
     * Öffnet den {@link PasswortDialog} für den angegebenen Benutzer.
     *
     * @param user der Benutzer dessen Passwort zurückgesetzt werden soll
     */
    private void oeffnePasswortDialog(User user) {
        PasswortDialog dialog = new PasswortDialog(user, userService, this::ladeDaten);
        dialog.open();
    }

    /**
     * Erstellt die Titelgruppe (Icon-Box + Überschrift "Benutzerverwaltung").
     */
    private HorizontalLayout buildTitel() {
        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "1rem");

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("background", "#e2e0fc").set("border-radius", "1rem").set("padding", "0.75rem")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        Span icon = createIcon("group");
        icon.getStyle().set("color", "#553722").set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Benutzerverwaltung");
        titel.getStyle()
                .set("margin", "0").set("font-size", "1.75rem").set("font-weight", "800")
                .set("color", "#1a1a2e").set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        titelGruppe.add(iconBox, titel);
        return titelGruppe;
    }

    /**
     * Erstellt den "Neuer Benutzer"-Button, der den {@link NeuerBenutzerDialog} öffnet.
     */
    private Button buildNeuerBenutzerButton() {
        Span plusIcon = createIcon("add");
        plusIcon.getStyle().set("font-size", "1.1rem");
        Span btnText = new Span("Neuer Benutzer");
        btnText.getStyle().set("font-weight", "700").set("font-family", "'Plus Jakarta Sans', sans-serif");

        Button btn = new com.vaadin.flow.component.button.Button();
        btn.getElement().setAttribute("tour-id", "neuer-benutzer-btn");
        btn.getElement().appendChild(plusIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("background", "linear-gradient(135deg, #553722, #6f4e37)").set("color", "white")
                .set("border", "none").set("border-radius", "1rem").set("padding", "0.75rem 1.5rem")
                .set("cursor", "pointer").set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("gap", "0.5rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("white-space", "nowrap");
        btn.addClickListener(e -> new NeuerBenutzerDialog(userService, this::ladeDaten).open());
        return btn;
    }

    /**
     * Verarbeitet Tour-Aktionen aus dem {@link de.fhswf.kassensystem.tour.TourManager}.
     * Aktuell unterstützt: {@code "open-neuer-benutzer-dialog"}.
     *
     * @param action Aktions-String aus dem Tour-Step
     */
    public void tourAktion(String action) {
        switch (action) {
            case "open-neuer-benutzer-dialog" -> new NeuerBenutzerDialog(userService, this::ladeDaten).open();
            default -> {}
        }
    }

    /**
     * Umhüllt die Tabelle in einem styled Container.
     */
    private VerticalLayout buildTabellenBereich() {
        VerticalLayout bereich = new VerticalLayout();
        bereich.setWidthFull();
        bereich.setPadding(false);
        bereich.setSpacing(false);
        bereich.getElement().setAttribute("tour-id", "benutzer-tabelle");
        bereich.getStyle()
                .set("background", "white").set("border-radius", "1.25rem").set("overflow", "hidden");
        bereich.add(tabelle);
        return bereich;
    }

    /**
     * Erstellt die Kopfzeile der Tabelle mit allen Spaltenüberschriften.
     */
    private HorizontalLayout buildTabellenHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle()
                .set("background", "#f5f2ff").set("padding", "0.75rem 2rem").set("gap", "0");
        header.add(
                headerZelle("ID",           BenutzerZeileFactory.BREITE_ID),
                headerZelle("Benutzername", BenutzerZeileFactory.BREITE_USERNAME),
                headerZelle("Name",         BenutzerZeileFactory.BREITE_NAME),
                headerZelle("Rolle",        BenutzerZeileFactory.BREITE_ROLLE),
                headerZelle("Status",       BenutzerZeileFactory.BREITE_STATUS),
                headerZelle("Aktionen",     BenutzerZeileFactory.BREITE_AKTIONEN)
        );
        return header;
    }
}