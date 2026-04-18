package de.fhswf.kassensystem.security;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.ErrorEvent;
import de.fhswf.kassensystem.exception.KassensystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;


@Component
public class GlobalErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public void error(ErrorEvent event) {
        Throwable cause = event.getThrowable();

        if (cause instanceof KassensystemException) {
            Notification.show(cause.getMessage(), 4000, Notification.Position.MIDDLE);
        } else {
            log.error("Unerwarteter Fehler", cause);
            Notification.show("Ein unerwarteter Fehler ist aufgetreten.",
                    4000, Notification.Position.MIDDLE);
        }
    }
}

