package de.fhswf.kassensystem.security;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    private final GlobalErrorHandler errorHandler;

    public ApplicationServiceInitListener(GlobalErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(sessionEvent ->
                sessionEvent.getSession().setErrorHandler(errorHandler)
        );
    }
}