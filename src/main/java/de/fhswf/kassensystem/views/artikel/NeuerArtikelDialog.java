package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.service.ArtikelService;
import de.fhswf.kassensystem.views.components.BaseDialog;

public class NeuerArtikelDialog extends BaseDialog {

    private final ArtikelService        artikelService;
    private final ArtikelFormularFelder felder;
    private final ArtikelBildUpload     bildUpload;
    private       Artikel               zuBearbeitenderArtikel = null;

    public NeuerArtikelDialog(ArtikelService artikelService) {
        this.artikelService = artikelService;
        this.felder         = new ArtikelFormularFelder(artikelService);
        this.bildUpload     = new ArtikelBildUpload();
        init("Neuer Artikel", null);
    }

    public NeuerArtikelDialog(ArtikelService artikelService, Artikel artikel) {
        this.artikelService         = artikelService;
        this.felder                 = new ArtikelFormularFelder(artikelService);
        this.bildUpload             = new ArtikelBildUpload();
        this.zuBearbeitenderArtikel = artikel;
        felder.befuelleFelder(artikel);
        if (artikel.getBild() != null) bildUpload.setBild(artikel.getBild());
        init("Artikel bearbeiten", null);
    }

    @Override
    protected VerticalLayout buildBody() {
        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("padding", "1.5rem").set("gap", "1.25rem").set("background", "white");
        body.add(felder, bildUpload);
        return body;
    }

    @Override
    protected boolean onSpeichern() {
        if (!felder.valide()) return false;
        Artikel artikel = felder.toArtikel();
        if (bildUpload.getBildBytes() != null) artikel.setBild(bildUpload.getBildBytes());

        if (zuBearbeitenderArtikel != null) {
            artikel.setId(zuBearbeitenderArtikel.getId());
            artikelService.updateArtikel(artikel);
            Notification.show("Artikel \"" + artikel.getName() + "\" wurde aktualisiert.",
                    3000, Notification.Position.BOTTOM_START);
        } else {
            artikelService.createArtikel(artikel);
            Notification.show("Artikel \"" + artikel.getName() + "\" wurde erstellt.",
                    3000, Notification.Position.BOTTOM_START);
        }
        return true;
    }

    @Override protected String getDialogBreite() { return "36rem"; }
}