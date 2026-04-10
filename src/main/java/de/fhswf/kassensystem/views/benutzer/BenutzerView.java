package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.service.UserService;
import de.fhswf.kassensystem.views.MainLayout;
import de.fhswf.kassensystem.views.components.AbstractTabellenView;

import java.util.List;

/**
 * Benutzerverwaltungs-View. Nur für Manager sichtbar.
 */
@Route(value = "benutzer", layout = MainLayout.class)
public class BenutzerView extends AbstractTabellenView {

    private final UserService userService;

    public BenutzerView(UserService userService) {
        super(Rolle.MANAGER);
        this.userService = userService;
        add(buildHeader(), buildTabellenBereich());
        ladeDaten();
    }

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

    @Override
    public void ladeDaten() {
        tabelle.removeAll();
        tabelle.add(buildTabellenHeader());
        List<User> benutzer = userService.findAllUsers();
        boolean zebra = false;
        for (User u : benutzer) {
            tabelle.add(BenutzerZeileFactory.create(u, zebra, userService,
                    this::ladeDaten, this::oeffnePasswortDialog));
            zebra = !zebra;
        }
    }

    private void oeffnePasswortDialog(User user) {
        PasswortDialog dialog = new PasswortDialog(user, userService, this::ladeDaten);
        dialog.open();
    }

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

    private com.vaadin.flow.component.button.Button buildNeuerBenutzerButton() {
        Span plusIcon = createIcon("add");
        plusIcon.getStyle().set("font-size", "1.1rem");
        Span btnText = new Span("Neuer Benutzer");
        btnText.getStyle().set("font-weight", "700").set("font-family", "'Plus Jakarta Sans', sans-serif");

        com.vaadin.flow.component.button.Button btn = new com.vaadin.flow.component.button.Button();
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

    private VerticalLayout buildTabellenBereich() {
        VerticalLayout bereich = new VerticalLayout();
        bereich.setWidthFull();
        bereich.setPadding(false);
        bereich.setSpacing(false);
        bereich.getStyle()
                .set("background", "white").set("border-radius", "1.25rem").set("overflow", "hidden");
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
