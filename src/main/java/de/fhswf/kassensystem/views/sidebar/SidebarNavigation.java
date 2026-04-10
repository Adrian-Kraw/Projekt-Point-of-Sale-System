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

        add(buildNavLink("receipt_long", "Kassieren", VerkaufView.class));
        add(buildNavLink("inventory_2",  "Lager",     LagerView.class));

        if (istManager) {
            add(buildNavLink("label",     "Artikel",  ArtikelView.class));
            add(buildNavLink("bar_chart", "Berichte", BerichteView.class));
            add(buildNavLink("person",    "Benutzer", BenutzerView.class));
        }
    }

    public List<RouterLink> getNavLinks() {
        return navLinks;
    }

    private RouterLink buildNavLink(String icon, String label,
                                    Class<? extends Component> view) {
        RouterLink link = new RouterLink();
        link.setRoute(view);
        link.addClassName("nav-link");

        Span iconSpan = new Span(icon);
        iconSpan.addClassName("material-symbols-outlined");

        Span labelSpan = new Span(label);
        link.add(iconSpan, labelSpan);

        navLinks.add(link);
        return link;
    }
}