package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import de.fhswf.kassensystem.views.artikel.ArtikelView;
import de.fhswf.kassensystem.views.berichte.BerichteView;
import de.fhswf.kassensystem.views.benutzer.BenutzerView;
import de.fhswf.kassensystem.views.lager.LagerView;
import de.fhswf.kassensystem.views.verkauf.VerkaufView;

import java.util.ArrayList;
import java.util.List;

/**
 * MainLayout ist das zentrale Grundgerüst der Anwendung.
 *
 * Es erweitert AppLayout von Vaadin, welches eine feste Sidebar (Drawer)
 * und einen Content-Bereich bereitstellt. Alle Views die
 * "layout = MainLayout.class" in ihrer @Route-Annotation haben,
 * werden automatisch in den Content-Bereich gerendert.
 *
 * Aufbau der Sidebar (von oben nach unten):
 *   - Logo "Canapé Café" (klickbar → Dashboard) + Dark-Mode-Toggle
 *   - Trennlinie
 *   - Navigationslinks (Kassieren, Artikel, Lager, Berichte, Benutzer)
 *   - Software Einführung Button
 *   - User-Card mit Popup-Menü (Ausloggen)
 */
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    /**
     * Speichert ob der Dark Mode aktuell aktiv ist.
     * Wird beim Klick auf den Dark-Mode-Toggle geändert.
     */
    private boolean darkMode = false;

    /**
     * Referenz auf das Dark-Mode-Icon damit wir das Symbol
     * (Mond/Sonne) im Event-Handler wechseln können.
     */
    private Span darkModeIcon;


    private final List<RouterLink> navLinks = new ArrayList<>();

    /**
     * Konstruktor – baut die Sidebar auf und konfiguriert das AppLayout.
     *
     * setPrimarySection(DRAWER) sorgt dafür dass der Drawer (Sidebar)
     * als primärer Bereich gilt und links angezeigt wird.
     */
    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        buildSidebar();
        getElement().getStyle()
                .set("--vaadin-app-layout-drawer-overlay", "false")
                .set("min-height", "100vh");

        /*
         * Die Navbar-Leiste von AppLayout wird ausgeblendet da wir
         * sie nicht benötigen. Dies geschieht über das Shadow DOM
         * des AppLayout-Elements, das per JavaScript erreichbar ist.
         */
        getElement().executeJs(
                "setTimeout(() => {" +
                        "  document.querySelectorAll('a.nav-link').forEach(el => {" +
                        "    el.style.textDecoration = 'none';" +
                        "  });" +
                        "}, 100);"
        );
    }

    // ═══════════════════════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════════════════════

    /**
     * Baut die gesamte Sidebar auf und fügt sie dem Drawer hinzu.
     *
     * Die Sidebar besteht aus zwei Bereichen:
     * - topSection: Logo + Trennlinie + Navigation
     * - bottomSection: Intro-Button + User-Card
     *
     * justify-content: space-between sorgt dafür, dass topSection
     * oben und bottomSection unten klebt.
     */
    private void buildSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.getStyle()
                .set("width", "240px")
                .set("min-height", "100vh")
                .set("background-color", "#f5f2ff")
                .set("border-radius", "0 3rem 3rem 0")
                .set("box-shadow", "20px 0 60px -15px rgba(85,55,34,0.08)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("justify-content", "space-between")
                .set("padding", "2rem 1rem")
                .set("box-sizing", "border-box");

        VerticalLayout topSection = new VerticalLayout();
        topSection.setPadding(false);
        topSection.setSpacing(false);
        topSection.getStyle().set("gap", "1rem");

        Hr divider = new Hr();
        divider.getStyle()
                .set("border", "none")
                .set("border-top", "1px solid rgba(85,55,34,0.12)")
                .set("margin", "0.25rem 0.5rem")
                .set("width", "calc(100% - 1rem)");

        topSection.add(buildLogoRow(), divider, buildNavigation());
        sidebar.add(topSection, buildBottomSection());
        addToDrawer(sidebar);
        setDrawerOpened(true);
    }

    // ═══════════════════════════════════════════════════════════
    // LOGO + DARK MODE TOGGLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt die Logo-Zeile mit dem Café-Namen und dem Dark-Mode-Toggle.
     *
     * Das Logo ist ein RouterLink – Vaadin's typsicherer Navigations-Link.
     * Er navigiert ohne vollen Seitenreload direkt zum Dashboard.
     */
    private HorizontalLayout buildLogoRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("padding", "0 0.5rem");

        RouterLink logoLink = new RouterLink();
        logoLink.setRoute(DashboardView.class);
        logoLink.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.5rem")
                .set("text-decoration", "none")
                .set("flex", "1");

        Span coffeeIcon = new Span("coffee");
        coffeeIcon.addClassName("material-symbols-outlined");
        coffeeIcon.getStyle()
                .set("color", "#553722")
                .set("font-size", "1.4rem")
                .set("font-variation-settings", "'FILL' 1")
                .set("line-height", "1");

        Span appName = new Span("Canapé Café");
        appName.getStyle()
                .set("font-size", "1.2rem")
                .set("font-weight", "800")
                .set("color", "#553722")
                .set("letter-spacing", "-0.03em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("line-height", "1.2");

        logoLink.add(coffeeIcon, appName);

        darkModeIcon = new Span("light_mode");
        darkModeIcon.addClassName("material-symbols-outlined");
        darkModeIcon.getStyle()
                .set("color", "#64748b")
                .set("font-size", "1.2rem")
                .set("line-height", "1");

        Button darkToggle = new Button();
        darkToggle.getElement().appendChild(darkModeIcon.getElement());
        darkToggle.getStyle()
                .set("width", "2.25rem")
                .set("height", "2.25rem")
                .set("min-width", "2.25rem")
                .set("border-radius", "9999px")
                .set("background", "white")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("margin-left", "auto")
                .set("padding", "0")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,0.1)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("flex-shrink", "0");

        darkToggle.addClickListener(event -> toggleDarkMode());

        row.add(logoLink, darkToggle);
        return row;
    }

    /**
     * Schaltet den Dark Mode um.
     *
     * Das Icon wechselt zwischen Mond (dark_mode) und Sonne (light_mode).
     * Die CSS-Klasse 'dark' wird am body-Element gesetzt, was alle
     * dark-Mode-Styles in der styles.css aktiviert.
     */
    private void toggleDarkMode() {
        darkMode = !darkMode;
        darkModeIcon.setText(darkMode ? "dark_mode" : "light_mode");

        if (darkMode) {
            UI.getCurrent().getElement()
                    .executeJs("document.body.classList.add('dark')");
        } else {
            UI.getCurrent().getElement()
                    .executeJs("document.body.classList.remove('dark')");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt die vertikale Navigationsleiste mit allen Haupt-Views.
     */
    private VerticalLayout buildNavigation() {
        VerticalLayout nav = new VerticalLayout();
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.getStyle().set("gap", "0.25rem");

        nav.add(
                createNavLink("receipt_long", "Kassieren", VerkaufView.class),
                createNavLink("label", "Artikel", ArtikelView.class),
                createNavLink("inventory_2", "Lager", LagerView.class),
                createNavLink("bar_chart", "Berichte", BerichteView.class),
                createNavLink("person", "Benutzer", BenutzerView.class)
        );
        return nav;
    }

    /**
     * Erstellt einen einzelnen Navigationslink als RouterLink.
     *
     * RouterLink ist Vaadin's typsicherer Link – er navigiert ohne
     * Seitenreload und setzt automatisch die CSS-Klasse "highlight"
     * wenn die verlinkte View gerade aktiv ist.
     *
     * @param icon   Name des Material Symbols Icons (z.B. "receipt_long")
     * @param label  Anzeigetext des Links (z.B. "Kassieren")
     * @param view   Ziel-View Klasse (typsicher, kein String-URL)
     */
    private RouterLink createNavLink(String icon, String label,
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


    // ═══════════════════════════════════════════════════════════
    // UNTERER BEREICH
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt den unteren Sidebar-Bereich mit Intro-Button und User-Card.
     */
    private VerticalLayout buildBottomSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "0.75rem");
        section.add(buildIntroButton(), buildUserCard());
        return section;
    }

    /**
     * Erstellt den "Software Einführung" Button mit Buch-Icon.
     *
     * Dieser Button soll später einen Onboarding-Dialog öffnen,
     * der dem Nutzer die Navigation Schritt für Schritt erklärt.
     */
    private Button buildIntroButton() {
        Span bookIcon = new Span("menu_book");
        bookIcon.addClassName("material-symbols-outlined");
        bookIcon.getStyle()
                .set("font-size", "1.1rem")
                .set("flex-shrink", "0")
                .set("line-height", "1")
                .set("vertical-align", "middle")
                .set("margin-right", "0.6rem");

        Span btnText = new Span("Software Einführung");
        btnText.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("vertical-align", "middle");

        Button btn = new Button();
        btn.setWidthFull();
        btn.getElement().appendChild(bookIcon.getElement());
        btn.getElement().appendChild(btnText.getElement());
        btn.getStyle()
                .set("background", "#ddd8f5")
                .set("border", "none")
                .set("border-radius", "1rem")
                .set("padding", "0.75rem 1rem")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "flex-start")
                .set("gap", "0")
                .set("color", "#553722")
                .set("width", "100%")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("transition", "background 0.2s")
                .set("line-height", "1");

        btn.addClickListener(event -> openIntroDialog());
        return btn;
    }

    /**
     * Öffnet den Onboarding-Dialog.
     * TODO: Echten Dialog mit Schritt-für-Schritt-Anleitung implementieren.
     */
    private void openIntroDialog() {
        UI.getCurrent().getElement()
                .executeJs("alert('Software Einführung folgt...')");
    }

    /**
     * Erstellt die User-Card am unteren Rand der Sidebar.
     *
     * Die Card zeigt Avatar, Name und Rolle des eingeloggten Nutzers.
     * Ein Klick öffnet ein Popup-Menü direkt oberhalb der Card
     * mit der Option "Ausloggen".
     *
     * Das Popup nutzt position:absolute relativ zur Card (position:relative)
     * und erscheint über der Card durch bottom: calc(100% + 8px).
     */
    private HorizontalLayout buildUserCard() {
        HorizontalLayout userCard = new HorizontalLayout();
        userCard.setWidthFull();
        userCard.setAlignItems(FlexComponent.Alignment.CENTER);
        userCard.setSpacing(false);
        userCard.getStyle()
                .set("background", "white")
                .set("border-radius", "1.5rem")
                .set("padding", "0.75rem 1rem")
                .set("gap", "0.75rem")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.06)")
                .set("cursor", "pointer")
                .set("position", "relative");

        Div avatarWrapper = buildAvatar();

        VerticalLayout userInfo = new VerticalLayout();
        userInfo.setPadding(false);
        userInfo.setSpacing(false);
        userInfo.getStyle()
                .set("min-width", "0")
                .set("flex", "1");

        Span userName = new Span("Max Mustermann");
        userName.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "700")
                .set("color", "#1a1a2e")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span userRole = new Span("MANAGER");
        userRole.getStyle()
                .set("font-size", "0.6rem")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.08em")
                .set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        userInfo.add(userName, userRole);

        Div popup = buildUserPopup();

        userCard.addClickListener(event -> toggleUserPopup(popup));
        userCard.add(avatarWrapper, userInfo, popup);
        return userCard;
    }

    /**
     * Erstellt den runden Avatar-Kreis mit Person-Icon.
     * Wird später durch ein echtes Profilbild ersetzt.
     */
    private Div buildAvatar() {
        Div avatarWrapper = new Div();
        avatarWrapper.getStyle()
                .set("width", "2.5rem")
                .set("height", "2.5rem")
                .set("border-radius", "9999px")
                .set("background-color", "#ffdcc6")
                .set("flex-shrink", "0")
                .set("border", "2px solid white")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span avatarIcon = new Span("person");
        avatarIcon.addClassName("material-symbols-outlined");
        avatarIcon.getStyle()
                .set("color", "#553722")
                .set("font-size", "1.25rem")
                .set("line-height", "1");

        avatarWrapper.add(avatarIcon);
        return avatarWrapper;
    }

    /**
     * Erstellt das Popup-Menü das oberhalb der User-Card erscheint.
     *
     * Das Popup ist standardmäßig versteckt (display:none) und wird
     * durch toggleUserPopup() ein- und ausgeblendet.
     */
    private Div buildUserPopup() {
        Div popup = new Div();
        popup.getStyle()
                .set("position", "absolute")
                .set("bottom", "calc(100% + 8px)")
                .set("left", "0")
                .set("right", "0")
                .set("background", "white")
                .set("border-radius", "1rem")
                .set("box-shadow", "0 4px 20px rgba(0,0,0,0.12)")
                .set("padding", "0.5rem")
                .set("display", "none")
                .set("z-index", "1000");

        Span logoutItem = new Span("Ausloggen");
        logoutItem.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("padding", "0.6rem 1rem")
                .set("border-radius", "0.5rem")
                .set("cursor", "pointer")
                .set("color", "#ba1a1a")
                .set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("font-weight", "600");

        logoutItem.addClickListener(event -> logout());
        popup.add(logoutItem);
        return popup;
    }

    /**
     * Toggelt die Sichtbarkeit des User-Popups.
     * Ist es versteckt, wird es angezeigt – und umgekehrt.
     *
     * @param popup Das Popup-Div das ein- oder ausgeblendet wird.
     */
    private void toggleUserPopup(Div popup) {
        String current = popup.getStyle().get("display");
        if ("none".equals(current)) {
            popup.getStyle().set("display", "block");
        } else {
            popup.getStyle().set("display", "none");
        }
    }

    /**
     * Wird nach jeder Navigation aufgerufen.
     * Setzt den aktiven Link manuell mit Inline-Style –
     * da CSS-Klassen im Vaadin Shadow DOM nicht greifen.
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String aktuellerPfad = event.getLocation().getPath();

        for (RouterLink link : navLinks) {
            String linkPfad = link.getHref();
            if (aktuellerPfad.equals(linkPfad)) {
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
     * Meldet den aktuellen Nutzer ab und leitet zur Login-Seite weiter.
     * TODO: Spring Security Session beenden wenn Backend integriert wird.
     */
    private void logout() {
        UI.getCurrent().navigate(LoginView.class);
    }



}