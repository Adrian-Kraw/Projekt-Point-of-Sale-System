package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import de.fhswf.kassensystem.model.enums.Rolle;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstrakte Basisklasse für alle geschützten Views.
 *
 * Implementiert BeforeEnterObserver – wird von Vaadin aufgerufen
 * bevor die View gerendert wird. Prüft:
 *   1. Ist der User eingeloggt?         → sonst /login
 *   2. Hat er die nötige Rolle?         → sonst /login
 *
 * Verwendung:
 *   public class MeineView extends SecuredView {
 *       public MeineView() { super(Rolle.KASSIERER); } // oder MANAGER
 *   }
 */
public abstract class SecuredView extends VerticalLayout implements BeforeEnterObserver {

    private final Rolle mindestRolle;

    /**
     * @param mindestRolle Minimale Rolle die für diese View benötigt wird.
     *                     KASSIERER = Kassierer & Manager erlaubt.
     *                     MANAGER   = nur Manager erlaubt.
     */
    protected SecuredView(Rolle mindestRolle) {
        this.mindestRolle = mindestRolle;
    }

    @Override
    public final void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Nicht eingeloggt oder anonymer User → Login
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            event.rerouteTo(LoginView.class);
            return;
        }

        // Rollenprüfung
        if (!hatRolle(auth, mindestRolle)) {
            // Eingeloggt aber falsche Rolle → zurück zum Dashboard
            event.rerouteTo(DashboardView.class);
            return;
        }

        // Alles OK – View-spezifische Initialisierung aufrufen
        onBeforeEnter(event);
    }

    /**
     * Kann von Unterklassen überschrieben werden wenn sie
     * eigene beforeEnter-Logik brauchen (optional).
     */
    protected void onBeforeEnter(BeforeEnterEvent event) {
        // Standard: nichts tun
    }

    /**
     * Prüft ob der eingeloggte User mindestens die geforderte Rolle hat.
     * MANAGER darf alles was KASSIERER darf (Hierarchie).
     */
    private boolean hatRolle(Authentication auth, Rolle mindest) {
        boolean istManager = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);
        boolean istKassierer = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_KASSIERER"::equals);

        return switch (mindest) {
            case KASSIERER -> istKassierer || istManager; // beide erlaubt
            case MANAGER   -> istManager;                 // nur Manager
        };
    }

    /**
     * Hilfsmethode für Unterklassen: aktuell eingeloggten Benutzernamen holen.
     */
    protected String getEingeloggterBenutzername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "";
    }

    /**
     * Hilfsmethode für Unterklassen: prüfen ob aktueller User Manager ist.
     */
    protected boolean istManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANAGER"::equals);
    }
}