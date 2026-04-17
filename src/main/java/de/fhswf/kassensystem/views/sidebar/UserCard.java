package de.fhswf.kassensystem.views.sidebar;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Benutzerkarte am unteren Rand der Sidebar mit Avatar, Name, Rolle und Logout-Popup.
 *
 * <p>Ein Klick auf die Karte toggelt ein kleines Popup-Menü mit dem "Ausloggen"-Link.
 * Der Logout-Callback wird von {@link de.fhswf.kassensystem.views.MainLayout} übergeben.
 *
 * @author Adrian
 */
public class UserCard extends HorizontalLayout {

    /**
     * Erstellt die Benutzerkarte.
     *
     * @param name     Benutzername des eingeloggten Benutzers
     * @param rolle    Rollenbezeichnung (z.B. "Manager", "Kassierer")
     * @param onLogout Callback der den Logout-Vorgang einleitet
     */
    public UserCard(String name, String rolle, Runnable onLogout) {
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(false);
        getStyle()
                .set("background", "white").set("border-radius", "1.5rem")
                .set("padding", "0.75rem 1rem").set("gap", "0.75rem")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.06)")
                .set("cursor", "pointer").set("position", "relative");
        getElement().setAttribute("tour-id", "user-card");

        Div popup = buildPopup(onLogout);
        addClickListener(e -> togglePopup(popup));
        add(buildAvatar(), buildUserInfo(name, rolle), popup);
    }

    /**
     * Erstellt den runden Avatar-Platzhalter mit Person-Icon.
     */
    private Div buildAvatar() {
        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "2.5rem").set("height", "2.5rem").set("border-radius", "9999px")
                .set("background-color", "#ffdcc6").set("flex-shrink", "0")
                .set("border", "2px solid white").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");

        Span icon = new Span("person");
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("color", "#553722").set("font-size", "1.25rem").set("line-height", "1");
        avatar.add(icon);
        return avatar;
    }

    /**
     * Erstellt den Infoblock mit Name und Rolle.
     *
     * @param name  Benutzername
     * @param rolle Rollenbezeichnung
     */
    private VerticalLayout buildUserInfo(String name, String rolle) {
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("min-width", "0").set("flex", "1");

        Span nameSpan = new Span(name);
        nameSpan.getStyle()
                .set("font-size", "0.8rem").set("font-weight", "700").set("color", "#1a1a2e")
                .set("white-space", "nowrap").set("overflow", "hidden").set("text-overflow", "ellipsis")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span rolleSpan = new Span(rolle);
        rolleSpan.getStyle()
                .set("font-size", "0.6rem").set("text-transform", "uppercase")
                .set("letter-spacing", "0.08em").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        info.add(nameSpan, rolleSpan);
        return info;
    }

    /**
     * Erstellt das Logout-Popup-Menü, das beim Klick auf die Karte ein-/ausgeblendet wird.
     *
     * @param onLogout Callback für den Logout-Klick
     */
    private Div buildPopup(Runnable onLogout) {
        Div popup = new Div();
        popup.getStyle()
                .set("position", "absolute").set("bottom", "calc(100% + 8px)")
                .set("left", "0").set("right", "0").set("background", "white")
                .set("border-radius", "1rem").set("box-shadow", "0 4px 20px rgba(0,0,0,0.12)")
                .set("padding", "0.5rem").set("display", "none").set("z-index", "1000");

        Span logoutItem = new Span("Ausloggen");
        logoutItem.getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("padding", "0.6rem 1rem").set("border-radius", "0.5rem")
                .set("cursor", "pointer").set("color", "#ba1a1a")
                .set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("font-weight", "600");
        logoutItem.addClickListener(e -> onLogout.run());

        popup.add(logoutItem);
        return popup;
    }

    /**
     * Blendet das Popup ein oder aus (toggle).
     *
     * @param popup der umzuschaltende Popup-Container
     */
    private void togglePopup(Div popup) {
        String current = popup.getStyle().get("display");
        popup.getStyle().set("display", "none".equals(current) ? "block" : "none");
    }
}