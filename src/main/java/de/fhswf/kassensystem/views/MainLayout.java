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

import java.util.List;

@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final SidebarNavigation sidebarNavigation;
    private final TourManager tourManager;

    public MainLayout(TourManager tourManager) {
        this.tourManager       = tourManager;
        this.sidebarNavigation = new SidebarNavigation(istManager());
        setPrimarySection(Section.DRAWER);
        buildSidebar();
        getElement().getStyle()
                .set("--vaadin-app-layout-drawer-overlay", "false")
                .set("min-height", "100vh");
    }

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

    private void logout() {
        UI.getCurrent().getPage().setLocation("/logout");
    }

    private boolean istManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);
    }
}