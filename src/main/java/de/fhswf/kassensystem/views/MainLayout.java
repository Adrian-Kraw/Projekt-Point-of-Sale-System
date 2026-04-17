package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import de.fhswf.kassensystem.tour.TourManager;
import de.fhswf.kassensystem.views.sidebar.LogoRow;
import de.fhswf.kassensystem.views.sidebar.SidebarNavigation;
import de.fhswf.kassensystem.views.sidebar.UserCard;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Haupt-Layout der Anwendung, das alle geschützten Views umrahmt.
 *
 * <p>Erweitert Vaadins {@link com.vaadin.flow.component.applayout.AppLayout} und
 * stellt die linke Sidebar bereit, die auf allen Seiten (außer Login) sichtbar ist.
 *
 * <p>Aufbau der Sidebar:
 * <ul>
 *   <li><b>Oben:</b> Logo-Zeile ({@link de.fhswf.kassensystem.views.sidebar.LogoRow}),
 *       Trennlinie, Navigationslinks ({@link de.fhswf.kassensystem.views.sidebar.SidebarNavigation})</li>
 *   <li><b>Unten:</b> "Software Einführung"-Button zum Starten der Onboarding-Tour
 *       sowie die Benutzer-Karte ({@link de.fhswf.kassensystem.views.sidebar.UserCard})
 *       mit Logout-Funktion</li>
 * </ul>
 *
 * <p>Nach jeder Navigation wird der aktive Navigationslink farblich hervorgehoben
 * ({@code afterNavigation}). Die Onboarding-Tour wird rollenabhängig gestartet –
 * Manager erhalten eine erweiterte Tour mit allen Verwaltungsbereichen.
 *
 * @author Adrian & Paula
 */
@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final SidebarNavigation sidebarNavigation;
    private final TourManager tourManager;

    /**
     * Erstellt das Haupt-Layout und baut die Sidebar auf.
     *
     * @param tourManager verwaltet den Start der rollenabhängigen Onboarding-Tour
     */
    public MainLayout(TourManager tourManager) {
        this.tourManager       = tourManager;
        this.sidebarNavigation = new SidebarNavigation(istManager());
        setPrimarySection(Section.DRAWER);
        buildSidebar();
        getElement().getStyle()
                .set("--vaadin-app-layout-drawer-overlay", "false")
                .set("min-height", "100vh");
    }

    /**
     * Erstellt die linke Sidebar und fügt sie dem Drawer hinzu.
     *
     * <p>Die Sidebar gliedert sich in zwei Bereiche:
     * oben Logo, Trennlinie und Navigation – unten Einführungs-Button und Benutzer-Karte.
     */
    private void buildSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.getStyle()
                .set("width", "240px")
                .set("height", "100vh")
                .set("background-color", "#f5f2ff")
                .set("border-radius", "0 3rem 3rem 0")
                .set("overflow", "hidden")
                .set("box-shadow", "20px 0 60px -15px rgba(85,55,34,0.08)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("justify-content", "space-between")
                .set("padding", "2rem 1rem")
                .set("box-sizing", "border-box");

        Hr divider = new Hr();
        divider.getStyle()
                .set("border", "none").set("border-top", "1px solid rgba(85,55,34,0.12)")
                .set("margin", "0.25rem 0.5rem").set("width", "calc(100% - 1rem)");

        VerticalLayout topSection = new VerticalLayout();
        topSection.setPadding(false);
        topSection.setSpacing(false);
        topSection.getStyle().set("gap", "1rem");
        topSection.add(new LogoRow(), divider, sidebarNavigation);

        sidebar.add(topSection, buildBottomSection());
        addToDrawer(sidebar);
        setDrawerOpened(true);
    }

    /**
     * Erstellt den unteren Bereich der Sidebar mit Einführungs-Button und Benutzer-Karte.
     *
     * <p>Name und Rolle des eingeloggten Benutzers werden aus dem {@code SecurityContext}
     * gelesen und an die {@link UserCard} übergeben.
     *
     * @return fertiges Layout mit Einführungs-Button und Benutzer-Karte
     */
    private VerticalLayout buildBottomSection() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name  = (auth != null && auth.isAuthenticated()) ? auth.getName() : "Unbekannt";
        String rolle = istManager() ? "Manager" : "Kassierer";

        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "0.75rem");
        section.add(buildIntroButton(), new UserCard(name, rolle, this::logout));
        return section;
    }

    /**
     * Erstellt den "Software Einführung"-Button in der Sidebar.
     *
     * <p>Ein Klick startet die rollenabhängige Onboarding-Tour über den {@link TourManager}.
     * Tour-Aktionen (z.B. Demo-Verkauf, Dialog öffnen) werden an die aktuell angezeigte
     * View delegiert, sofern diese {@code tourAktion(String)} implementiert.
     *
     * @return der fertig konfigurierte Einführungs-Button
     */
    private Button buildIntroButton() {
        Span bookIcon = new Span("menu_book");
        bookIcon.addClassName("material-symbols-outlined");
        bookIcon.getStyle()
                .set("font-size", "1.1rem").set("flex-shrink", "0")
                .set("line-height", "1").set("vertical-align", "middle").set("margin-right", "0.6rem");

        Span btnText = new Span("Software Einführung");
        btnText.getStyle()
                .set("font-size", "0.8rem").set("font-weight", "600")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("vertical-align", "middle");

        Button btn = new Button();
        btn.setWidthFull();
        btn.getElement().appendChild(bookIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("background", "#ddd8f5").set("border", "none").set("border-radius", "1rem")
                .set("padding", "0.75rem 1rem").set("cursor", "pointer")
                .set("display", "flex").set("align-items", "center").set("justify-content", "flex-start")
                .set("color", "#553722").set("width", "100%")
                .set("font-family", "'Plus Jakarta Sans', sans-serif").set("line-height", "1");

        // Rollenabhängige Tour starten
        String tourId = istManager() ? "manager" : "kassierer";
        btn.addClickListener(e -> tourManager.start(tourId, action -> {
            Component currentView = getContent();
            if (currentView instanceof de.fhswf.kassensystem.views.verkauf.VerkaufView vv) {
                vv.tourAktion(action);
            } else if (currentView instanceof de.fhswf.kassensystem.views.lager.LagerView lv) {
                lv.tourAktion(action);
            } else if (currentView instanceof de.fhswf.kassensystem.views.artikel.ArtikelView av) {
                av.tourAktion(action);
            } else if (currentView instanceof de.fhswf.kassensystem.views.berichte.BerichteView bv) {
                bv.tourAktion(action);
            } else if (currentView instanceof de.fhswf.kassensystem.views.benutzer.BenutzerView buv) {
                buv.tourAktion(action);
            }
        }));

        return btn;
    }

    /**
     * Hebt nach jeder Navigation den aktuell aktiven Navigationslink farblich hervor.
     *
     * <p>Der Pfad der aktuellen Route wird mit den {@code href}-Attributen aller
     * Navigationslinks verglichen. Der passende Link erhält eine Hintergrundfarbe
     * und fette Schrift, alle anderen werden zurückgesetzt.
     *
     * @param event enthält die aktuelle Route nach abgeschlossener Navigation
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String aktuellerPfad = event.getLocation().getPath();
        for (RouterLink link : sidebarNavigation.getNavLinks()) {
            if (aktuellerPfad.equals(link.getHref())) {
                link.getStyle()
                        .set("background-color", "#e8d5c4")
                        .set("color", "#553722")
                        .set("font-weight", "700");
            } else {
                link.getStyle()
                        .remove("background-color")
                        .remove("color")
                        .remove("font-weight");
            }
        }
    }

    /**
     * Leitet den Browser zur Spring Security Logout-URL weiter.
     *
     * <p>Spring Security invalidiert dabei die Session und löscht das
     * {@code JSESSIONID}-Cookie, bevor zur Login-Seite weitergeleitet wird.
     */
    private void logout() {
        UI.getCurrent().getPage().setLocation("/logout");
    }

    /**
     * Prüft ob der aktuell eingeloggte Benutzer die Rolle Manager besitzt.
     *
     * @return {@code true} wenn der Benutzer die Rolle {@code ROLE_MANAGER} hat,
     *         sonst {@code false}
     */
    private boolean istManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);
    }
}