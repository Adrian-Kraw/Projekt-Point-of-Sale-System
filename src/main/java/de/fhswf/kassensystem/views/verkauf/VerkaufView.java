package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

@Route(value = "kassieren", layout = MainLayout.class)
public class VerkaufView extends Div {
    public VerkaufView() {
        add("Kassieren – coming soon");
    }
}