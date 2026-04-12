package de.fhswf.kassensystem.tour;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * Startet Onboarding-Touren.
 * Aktionen wie Demo-Verkauf oder Dialog-Öffnen werden über einen
 * ActionHandler-Callback an die jeweilige View delegiert.
 */
@Service
public class TourManager {

    private final TourService tourService;

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

    /** Kurzform ohne ActionHandler (für einfache Touren ohne Dialoge). */
    public void start(String tourId) {
        start(tourId, action -> {});
    }
}