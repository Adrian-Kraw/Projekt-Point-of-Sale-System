package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.views.MainLayout;

@Route(value = "artikel", layout = MainLayout.class)
public class ArtikelView extends Div {
    public ArtikelView() {
        add("Artikel – coming soon");
    }
}