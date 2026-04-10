package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import de.fhswf.kassensystem.model.enums.Rolle;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstrakte Basisklasse für alle geschützten Views.
 * Stellt bereit: Security-Check, createIcon(), applyStandardBackground(), istManager().
 */
public abstract class SecuredView extends VerticalLayout implements BeforeEnterObserver {

    private final Rolle mindestRolle;

    protected SecuredView(Rolle mindestRolle) {
        this.mindestRolle = mindestRolle;
    }

    @Override
    public final void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            event.rerouteTo(LoginView.class);
            return;
        }
        if (!hatRolle(auth, mindestRolle)) {
            event.rerouteTo(DashboardView.class);
            return;
        }
        onBeforeEnter(event);
    }

    protected void onBeforeEnter(BeforeEnterEvent event) {}

    private boolean hatRolle(Authentication auth, Rolle mindest) {
        boolean istManager   = hatAuthority(auth, "ROLE_MANAGER");
        boolean istKassierer = hatAuthority(auth, "ROLE_KASSIERER");
        return switch (mindest) {
            case KASSIERER -> istKassierer || istManager;
            case MANAGER   -> istManager;
        };
    }

    private boolean hatAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    /**
     * Erstellt einen Material Symbols Icon-Span.
     * Wird von allen Unterklassen (ArtikelView, BenutzerView, LagerView, BerichteView...) genutzt.
     */
    protected Span createIcon(String iconName) {
        Span icon = new Span(iconName);
        icon.addClassName("material-symbols-outlined");
        icon.getStyle().set("line-height", "1");
        return icon;
    }

    /**
     * Einheitliches Hintergrund-Padding für Standard-Views.
     * Wird von AbstractTabellenView und BerichteView im Konstruktor aufgerufen.
     */
    protected void applyStandardBackground() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#fcf8ff")
                .set("padding", "2.5rem")
                .set("box-sizing", "border-box");
    }

    protected boolean istManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return hatAuthority(auth, "ROLE_MANAGER");
    }

    protected String getEingeloggterBenutzername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "";
    }
}
