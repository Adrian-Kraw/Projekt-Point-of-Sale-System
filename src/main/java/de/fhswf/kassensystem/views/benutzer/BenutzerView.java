package de.fhswf.kassensystem.views.benutzer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

@Route(value = "benutzer", layout = MainLayout.class)
public class BenutzerView extends Div {
    public BenutzerView() {
        add("Benutzer – coming soon");
    }
}