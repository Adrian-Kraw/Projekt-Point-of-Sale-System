package de.fhswf.kassensystem.views.lager;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.service.LagerService;
import de.fhswf.kassensystem.views.components.BaseDialog;
import java.util.List;

/**
 * Dialog zum Aufgeben einer Warenbestellung (Wareneingang buchen).
 *
 * <p>Alle Instanzvariablen werden vor dem Aufruf von {@link #init} gesetzt,
 * da {@code BaseDialog.init()} intern {@link #buildBody()} aufruft und
 * die Felder zu diesem Zeitpunkt bereits befüllt sein müssen.
 *
 * <p>Nach dem Speichern wird die Bestellung über
 * {@link de.fhswf.kassensystem.service.LagerService#bestellungAufgeben} persistiert
 * und der {@code onErfolg}-Callback aufgerufen.
 *
 * @author Adrian
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

    /**
     * Erstellt den Dialog ohne vorausgewählten Artikel.
     *
     * @param alleArtikel  alle verfügbaren Artikel für das Auswahlfeld
     * @param lagerService Service zum Aufgeben der Bestellung
     * @param onErfolg     wird nach dem Buchen aufgerufen
     */
    WareneingangDialog(List<Artikel> alleArtikel, LagerService lagerService, Runnable onErfolg) {
        this(alleArtikel, null, lagerService, onErfolg);
    }

    /**
     * Erstellt den Dialog mit einem vorausgewählten Artikel (aus Nachbestellhinweis).
     *
     * @param alleArtikel    alle verfügbaren Artikel für das Auswahlfeld
     * @param vorausgewaehlt der im Dialog vorausgewählte Artikel
     * @param lagerService   Service zum Aufgeben der Bestellung
     * @param onErfolg       wird nach dem Buchen aufgerufen
     */
    WareneingangDialog(List<Artikel> alleArtikel, Artikel vorausgewaehlt,
                       LagerService lagerService, Runnable onErfolg) {
        this.alleArtikel    = alleArtikel;
        this.vorausgewaehlt = vorausgewaehlt;
        this.lagerService   = lagerService;
        this.onErfolg       = onErfolg;
        init("Wareneingang buchen", null);
    }

    /**
     * Erstellt den Dialog-Body mit Artikel-Select, Mengeneingabe, Lieferant und Kommentar.
     * Der vorausgewählte Artikel (falls vorhanden) wird automatisch selektiert.
     */
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

    /**
     * Validiert Artikel und Menge und gibt die Bestellung auf.
     *
     * @return {@code true} bei Erfolg, {@code false} wenn Artikel oder Menge fehlt
     */
    @Override
    protected boolean onSpeichern() {
        if (artikelSelect.isEmpty() || mengeFeld.isEmpty()) {
            Notification.show("Bitte Artikel und Menge angeben.", 3000, Notification.Position.MIDDLE);
            return false;
        }
        Wareneingang eingang = new Wareneingang();
        eingang.setArtikel(artikelSelect.getValue());
        eingang.setMenge(mengeFeld.getValue());
        if (!lieferantFeld.isEmpty()) eingang.setLieferant(lieferantFeld.getValue());
        if (!kommentarFeld.isEmpty()) eingang.setKommentar(kommentarFeld.getValue());


        lagerService.bestellungAufgeben(eingang);

        Notification.show("Bestellung aufgegeben. Warte auf Lieferbestätigung.",
                4000, Notification.Position.BOTTOM_START);

        onErfolg.run();
        return true;
    }

    @Override protected String getSpeichernLabel() { return "Buchen"; }
    @Override protected String getDialogBreite()   { return "32rem"; }
}
