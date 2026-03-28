package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("")
public class LoginView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        /*
         * Startseite leitet direkt zum Dashboard weiter.
         * echte Login-Seite mit Formular kommt noch.
         */
        event.forwardTo(DashboardView.class);
    }
}
