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
 * Abstrakte Basisklasse für alle zugriffsgeschützten Views der Anwendung.
 *
 * <p>Jede konkrete View (z.B. {@code ArtikelView}, {@code LagerView}) erbt von dieser
 * Klasse und gibt im Konstruktor die erforderliche Mindestrolle an. Die Zugriffsprüfung
 * erfolgt automatisch in {@code beforeEnter} – vor dem Rendern der View:
 * <ul>
 *   <li>Nicht eingeloggte Benutzer werden zur {@link LoginView} weitergeleitet</li>
 *   <li>Benutzer ohne ausreichende Rolle werden zum {@link DashboardView} weitergeleitet</li>
 * </ul>
 *
 * <p>Zusätzlich stellt die Klasse folgende Hilfsmethoden für alle Unterklassen bereit:
 * <ul>
 *   <li>{@link #createIcon(String)} – erzeugt einen Material-Symbols-Icon-Span</li>
 *   <li>{@link #applyStandardBackground()} – setzt einheitliches Hintergrund-Styling</li>
 *   <li>{@link #istManager()} – prüft ob der aktuelle Benutzer die Rolle MANAGER hat</li>
 *   <li>{@link #getEingeloggterBenutzername()} – gibt den Benutzernamen aus dem SecurityContext zurück</li>
 * </ul>
 *
 * @author Adrian
 */
public abstract class SecuredView extends VerticalLayout implements BeforeEnterObserver {

    private final Rolle mindestRolle;

    protected SecuredView(Rolle mindestRolle) {
        this.mindestRolle = mindestRolle;
    }

    /**
     * Wird vor dem Rendern der View aufgerufen und prüft Authentifizierung und Rollenberechtigung.
     * Nicht eingeloggte Benutzer werden zu {@link LoginView}, unberechtigte zu {@link DashboardView} weitergeleitet.
     */
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

    /**
     * Prüft ob der Benutzer mindestens die angegebene Rolle besitzt.
     * MANAGER gilt dabei als Obermenge von KASSIERER.
     *
     * @param auth    aktuelle Authentifizierung
     * @param mindest erforderliche Mindestrolle
     * @return {@code true} wenn die Rolle ausreicht
     */
    private boolean hatRolle(Authentication auth, Rolle mindest) {
        boolean istManager   = hatAuthority(auth, "ROLE_MANAGER");
        boolean istKassierer = hatAuthority(auth, "ROLE_KASSIERER");
        return switch (mindest) {
            case KASSIERER -> istKassierer || istManager;
            case MANAGER   -> istManager;
        };
    }

    /**
     * Prüft ob die Authentifizierung eine bestimmte Spring-Security-Authority enthält.
     *
     * @param auth      aktuelle Authentifizierung
     * @param authority zu prüfende Authority (z.B. {@code "ROLE_MANAGER"})
     * @return {@code true} wenn die Authority vorhanden ist
     */
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

    /**
     * Prüft ob der aktuell eingeloggte Benutzer die Rolle MANAGER besitzt.
     *
     * @return {@code true} wenn ROLE_MANAGER vorhanden, sonst {@code false}
     */
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