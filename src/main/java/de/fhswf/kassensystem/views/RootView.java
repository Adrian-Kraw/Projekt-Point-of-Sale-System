package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Root-Route "/" – leitet eingeloggte User zum Dashboard,
 * nicht eingeloggte User zur Login-Seite.
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