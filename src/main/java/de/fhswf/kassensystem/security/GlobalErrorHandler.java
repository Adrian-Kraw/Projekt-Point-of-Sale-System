package de.fhswf.kassensystem.security;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GlobalErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public void error(ErrorEvent event) {
        Throwable cause = event.getThrowable();
        log.error("Nicht abgefangener Fehler", cause);
        Notification n = Notification.show(
                "Ein unerwarteter Fehler ist aufgetreten. Bitte versuche es erneut.",
                4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}