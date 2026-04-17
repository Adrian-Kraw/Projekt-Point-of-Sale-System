package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Einstiegs-Route {@code "/"} der Anwendung.
 *
 * <p>Leitet Besucher vor dem Rendern der Seite weiter:
 * <ul>
 *   <li>Eingeloggte Benutzer → {@link DashboardView}</li>
 *   <li>Nicht eingeloggte Benutzer → {@link LoginView}</li>
 * </ul>
 *
 * <p>Da die View selbst nie angezeigt wird, ist sie ein leeres {@code Div}.
 * {@code @AnonymousAllowed} stellt sicher, dass auch nicht authentifizierte
 * Benutzer die Root-URL aufrufen können, ohne von Spring Security blockiert zu werden.
 *
 * @author Adrian
 */
@Route("")
@AnonymousAllowed
public class RootView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()
                && !auth.getPrincipal().equals("anonymousUser")) {
            event.forwardTo(DashboardView.class);
        } else {
            event.forwardTo(LoginView.class);
        }
    }
}