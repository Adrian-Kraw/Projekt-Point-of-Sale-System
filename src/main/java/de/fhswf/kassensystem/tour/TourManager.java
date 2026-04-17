package de.fhswf.kassensystem.tour;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * Spring-Service zum Starten von Onboarding-Touren.
 *
 * <p>Lädt die Steps einer Tour aus dem {@link TourService}, erstellt eine
 * {@link TourComponent} und fügt sie der aktuellen Vaadin-UI hinzu.
 *
 * <p>View-spezifische Aktionen (Demo-Verkauf, Dialoge öffnen etc.) werden
 * über den {@code actionHandler}-Callback an die aufrufende View delegiert.
 * Das ermöglicht eine saubere Trennung: {@code TourManager} kennt keine Views,
 * und die Views kennen keine Tour-Logik.
 *
 * @author Adrian
 */
@Service
public class TourManager {

    private final TourService tourService;

    /**
     * Erstellt den TourManager.
     *
     * @param tourService Service der die Tour-Definitionen liefert
     */
    public TourManager(TourService tourService) {
        this.tourService = tourService;
    }

    /**
     * Startet die Tour. actionHandler wird aufgerufen wenn ein Step eine Aktion hat.
     * Die View implementiert die Aktionen (Demo-Verkauf, Dialoge öffnen etc.).
     *
     * @param tourId        ID der Tour
     * @param actionHandler Callback der den Action-String verarbeitet
     */
    public void start(String tourId, Consumer<String> actionHandler) {
        List<TourStep> steps = tourService.getSteps(tourId);
        if (steps.isEmpty()) return;

        UI ui = UI.getCurrent();
        if (ui == null) return;

        TourComponent tour = new TourComponent(steps, actionHandler);
        tour.addTourFinishedListener(e -> ui.remove(tour));

        ui.add(tour);
        ui.access(tour::start);
    }

    /**
     * Startet eine Tour ohne View-spezifische Aktionen.
     * Kurzform für Touren die nur navigieren und Texte anzeigen.
     *
     * @param tourId ID der Tour (z.B. "kassierer", "manager")
     */
    public void start(String tourId) {
        start(tourId, action -> {});
    }
}