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
 * Navigationsbereich der Sidebar mit rollenabhängigen Links.
 *
 * <p>Kassierer sehen: Kassieren, Lager.
 * Manager sehen zusätzlich: Artikel, Berichte, Benutzer.
 *
 * <p>Alle Links werden mit einer {@code tour-id} versehen und in der
 * {@link #getNavLinks()}-Liste gespeichert, damit {@link de.fhswf.kassensystem.views.MainLayout}
 * den aktiven Link nach jeder Navigation hervorheben kann.
 *
 * @author Adrian Krawietz
 */
public class SidebarNavigation extends VerticalLayout {

    private final List<RouterLink> navLinks = new ArrayList<>();

    /**
     * Erstellt die Navigation mit rollenabhängigen Links.
     *
     * @param istManager {@code true} wenn der eingeloggte Benutzer Manager ist
     */
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

    /**
     * Gibt alle gerenderten Navigations-Links zurück.
     *
     * @return unveränderliche Liste der Links
     */
    public List<RouterLink> getNavLinks() {
        return navLinks;
    }

    /**
     * Erstellt einen einzelnen Navigationslink mit Icon, Label, Ziel-View und Tour-ID.
     *
     * @param icon   Material-Symbols-Icon-Name
     * @param label  Anzeigetext des Links
     * @param view   Ziel-View-Klasse für den RouterLink
     * @param tourId Tour-ID für den Onboarding-Guide
     * @return fertig konfigurierter RouterLink
     */
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