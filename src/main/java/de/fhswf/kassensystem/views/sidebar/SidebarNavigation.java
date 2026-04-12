package de.fhswf.kassensystem.views.sidebar;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import de.fhswf.kassensystem.views.artikel.ArtikelView;
import de.fhswf.kassensystem.views.benutzer.BenutzerView;
import de.fhswf.kassensystem.views.berichte.BerichteView;
import de.fhswf.kassensystem.views.lager.LagerView;
import de.fhswf.kassensystem.views.verkauf.VerkaufView;

import java.util.ArrayList;
import java.util.List;

/**
 * Navigationsbereich der Sidebar.
 * Zeigt rollenabhängig die korrekten Links an.
 */
public class SidebarNavigation extends VerticalLayout {

    private final List<RouterLink> navLinks = new ArrayList<>();

    public SidebarNavigation(boolean istManager) {
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "0.25rem");

        add(buildNavLink("receipt_long", "Kassieren", VerkaufView.class,  "kassieren-nav"));
        add(buildNavLink("inventory_2",  "Lager",     LagerView.class,    "lager-nav"));

        if (istManager) {
            add(buildNavLink("label",     "Artikel",  ArtikelView.class,  "artikel-nav"));
            add(buildNavLink("bar_chart", "Berichte", BerichteView.class, "berichte-nav"));
            add(buildNavLink("person",    "Benutzer", BenutzerView.class, "benutzer-nav"));
        }
    }

    public List<RouterLink> getNavLinks() {
        return navLinks;
    }

    private RouterLink buildNavLink(String icon, String label,
                                    Class<? extends Component> view,
                                    String tourId) {
        RouterLink link = new RouterLink();
        link.setRoute(view);
        link.addClassName("nav-link");
        link.getElement().setAttribute("tour-id", tourId);

        Span iconSpan = new Span(icon);
        iconSpan.addClassName("material-symbols-outlined");

        Span labelSpan = new Span(label);
        link.add(iconSpan, labelSpan);

        navLinks.add(link);
        return link;
    }
}