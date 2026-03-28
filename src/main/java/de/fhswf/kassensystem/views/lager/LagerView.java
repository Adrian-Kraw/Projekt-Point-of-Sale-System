package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

@Route(value = "lager", layout = MainLayout.class)
public class LagerView extends Div {
    public LagerView() {
        add("Lager – coming soon");
    }
}