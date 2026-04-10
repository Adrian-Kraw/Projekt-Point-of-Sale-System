package de.fhswf.kassensystem.views.sidebar;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import de.fhswf.kassensystem.views.DashboardView;

/**
 * Logo-Zeile der Sidebar mit Café-Name.
 * Dark-Mode-Toggle entfernt.
 */
public class LogoRow extends HorizontalLayout {

    public LogoRow() {
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("padding", "0 0.5rem");
        add(buildLogoLink());
    }

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