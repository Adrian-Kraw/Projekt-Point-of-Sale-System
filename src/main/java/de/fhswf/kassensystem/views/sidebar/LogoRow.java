package de.fhswf.kassensystem.views.sidebar;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import de.fhswf.kassensystem.views.DashboardView;

/**
 * Logo-Zeile am oberen Rand der Sidebar mit klickbarem Link zur Dashboard-View.
 *
 * <p>Enthält ein Kaffeetassen-Icon und den App-Namen "Canapé Café".
 * Ein Klick auf die Zeile navigiert zur {@link de.fhswf.kassensystem.views.DashboardView}.
 *
 * @author Adrian Krawietz
 */
public class LogoRow extends HorizontalLayout {

    /**
     * Erstellt die Logo-Zeile mit Link, Icon und App-Namen.
     */
    public LogoRow() {
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("padding", "0 0.5rem");
        getElement().setAttribute("tour-id", "logo-row");
        add(buildLogoLink());
    }

    /**
     * Erstellt den anklickbaren RouterLink mit Kaffeetassen-Icon und App-Namen.
     *
     * @return konfigurierter RouterLink zur DashboardView
     */
    private RouterLink buildLogoLink() {
        RouterLink link = new RouterLink();
        link.setRoute(DashboardView.class);
        link.getStyle()
                .set("display", "flex").set("align-items", "center").set("gap", "0.5rem")
                .set("text-decoration", "none").set("flex", "1");

        Span coffeeIcon = new Span("coffee");
        coffeeIcon.addClassName("material-symbols-outlined");
        coffeeIcon.getStyle()
                .set("color", "#553722").set("font-size", "1.4rem")
                .set("font-variation-settings", "'FILL' 1").set("line-height", "1");

        Span appName = new Span("Canapé Café");
        appName.getStyle()
                .set("font-size", "1.2rem").set("font-weight", "800").set("color", "#553722")
                .set("letter-spacing", "-0.03em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("line-height", "1.2");

        link.add(coffeeIcon, appName);
        return link;
    }
}