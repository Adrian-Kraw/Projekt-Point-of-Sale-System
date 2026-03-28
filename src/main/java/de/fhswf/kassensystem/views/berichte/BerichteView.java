package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

@Route(value = "berichte", layout = MainLayout.class)
public class BerichteView extends Div {
    public BerichteView() {
        add("Berichte – coming soon");
    }
}