package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.service.LagerService;
import de.fhswf.kassensystem.views.components.BaseDialog;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

/**
 * Dialog zum Buchen eines Wareneingangs.
 *
 * FIX: Felder werden vor init() gesetzt, da BaseDialog.init() buildBody() aufruft.
 */
class WareneingangDialog extends BaseDialog {

    private final LagerService  lagerService;
    private final Runnable      onErfolg;
    private final List<Artikel> alleArtikel;
    private final Artikel       vorausgewaehlt;

    private Select<Artikel> artikelSelect;
    private IntegerField    mengeFeld;
    private TextField       lieferantFeld;
    private TextField       kommentarFeld;

    WareneingangDialog(List<Artikel> alleArtikel, LagerService lagerService, Runnable onErfolg) {
        this(alleArtikel, null, lagerService, onErfolg);
    }

    WareneingangDialog(List<Artikel> alleArtikel, Artikel vorausgewaehlt,
                       LagerService lagerService, Runnable onErfolg) {
        // Felder ZUERST setzen, dann init() – buildBody() liest sie
        this.alleArtikel    = alleArtikel;
        this.vorausgewaehlt = vorausgewaehlt;
        this.lagerService   = lagerService;
        this.onErfolg       = onErfolg;
        init("Wareneingang buchen", null);
    }

    @Override
    protected VerticalLayout buildBody() {
        artikelSelect = new Select<>();
        artikelSelect.setWidthFull();
        artikelSelect.addClassName("dialog-feld");
        artikelSelect.setItems(alleArtikel != null ? alleArtikel : List.of());
        artikelSelect.setItemLabelGenerator(Artikel::getName);
        if (vorausgewaehlt != null && alleArtikel != null) {
            final Long vorId = vorausgewaehlt.getId();
            alleArtikel.stream()
                    .filter(a -> vorId.equals(a.getId()))
                    .findFirst()
                    .ifPresent(artikelSelect::setValue);
        }

        mengeFeld = new IntegerField();
        mengeFeld.setWidthFull();
        mengeFeld.setPlaceholder("Menge");
        mengeFeld.setMin(1);
        mengeFeld.addClassName("dialog-feld");

        lieferantFeld = new TextField();
        lieferantFeld.setWidthFull();
        lieferantFeld.setPlaceholder("z.B. Bäckerei Meier (optional)");
        lieferantFeld.addClassName("dialog-feld");

        kommentarFeld = new TextField();
        kommentarFeld.setWidthFull();
        kommentarFeld.setPlaceholder("Kommentar (optional)");
        kommentarFeld.addClassName("dialog-feld");

        VerticalLayout body = new VerticalLayout();
        body.setWidthFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("padding", "1.5rem").set("gap", "1.25rem").set("background", "white");
        body.add(
                feldMitLabel("ARTIKEL",   artikelSelect),
                feldMitLabel("MENGE",     mengeFeld),
                feldMitLabel("LIEFERANT", lieferantFeld),
                feldMitLabel("KOMMENTAR", kommentarFeld)
        );
        return body;
    }

    @Override
    protected boolean onSpeichern() {
        if (artikelSelect.isEmpty() || mengeFeld.isEmpty()) {
            Notification.show("Bitte Artikel und Menge angeben.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        Wareneingang eingang = new Wareneingang();
        eingang.setArtikel(artikelSelect.getValue());
        eingang.setMenge(mengeFeld.getValue());
        // eingang.setDatum(LocalDate.now());
        if (!lieferantFeld.isEmpty()) eingang.setLieferant(lieferantFeld.getValue());
        if (!kommentarFeld.isEmpty()) eingang.setKommentar(kommentarFeld.getValue());


        lagerService.bestellungAufgeben(eingang);

        Notification.show("Bestellung aufgegeben. Warte auf Lieferbestätigung.",
                4000, Notification.Position.BOTTOM_START);

        onErfolg.run();
        return true;
        // lagerService.wareneingangBuchen(eingang);
        // Notification.show("Wareneingang für \"" + artikelSelect.getValue().getName() + "\" gebucht.",
        //        3000, Notification.Position.BOTTOM_START);
        // onErfolg.run();
        // return true;
    }

    @Override protected String getSpeichernLabel() { return "Buchen"; }
    @Override protected String getDialogBreite()   { return "32rem"; }
}
