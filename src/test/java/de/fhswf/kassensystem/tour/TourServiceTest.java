package de.fhswf.kassensystem.tour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für {@link TourService}.
 *
 * <p>Prüft ob die vordefinierten Touren korrekt geladen werden
 * und ob Grenzfälle (unbekannte ID, leere ID, null) sicher behandelt werden.
 */
class TourServiceTest {

    private TourService tourService;

    @BeforeEach
    void setUp() {
        tourService = new TourService();
    }

    /**
     * Kassierer-Tour existiert und hat mindestens einen Step.
     */
    @Test
    void getSteps_KassiererTour_GibtStepsZurueck() {
        List<TourStep> steps = tourService.getSteps("kassierer");
        assertFalse(steps.isEmpty());
    }

    /**
     * Manager-Tour existiert und hat mindestens einen Step.
     */
    @Test
    void getSteps_ManagerTour_GibtStepsZurueck() {
        List<TourStep> steps = tourService.getSteps("manager");
        assertFalse(steps.isEmpty());
    }

    /**
     * Unbekannte Tour-ID: eine leere Liste wird erwartet, keine Exception.
     */
    @Test
    void getSteps_UnbekannteId_GibtLeereListeZurueck() {
        List<TourStep> steps = tourService.getSteps("unbekannt");
        assertTrue(steps.isEmpty());
    }

    /**
     * Leere Tour-ID: eine leere Liste wird erwartet, keine Exception.
     */
    @Test
    void getSteps_LeereId_GibtLeereListeZurueck() {
        List<TourStep> steps = tourService.getSteps("");
        assertTrue(steps.isEmpty());
    }

    /**
     * null als Tour-ID: eine leere Liste wird erwartet, keine NullPointerException.
     * Map.of() erlaubt keine null-Keys, daher ist ein expliziter null-Check nötig.
     */
    @Test
    void getSteps_NullId_GibtLeereListeZurueck() {
        List<TourStep> steps = tourService.getSteps(null);
        assertTrue(steps.isEmpty());
    }

    /**
     * Erster Step der Kassierer-Tour muss der Willkommens-Step sein.
     */
    @Test
    void getSteps_KassiererTour_ErsterStepIstWillkommen() {
        TourStep ersterStep = tourService.getSteps("kassierer").getFirst();
        assertEquals("Willkommen im Kassensystem!", ersterStep.title());
    }

    /**
     * Die Manager-Tour deckt mehr Bereiche ab und muss
     * mehr Steps haben als die Kassierer-Tour.
     */
    @Test
    void getSteps_ManagerTour_HatMehrStepsAlsKassierer() {
        int kassierer = tourService.getSteps("kassierer").size();
        int manager   = tourService.getSteps("manager").size();
        assertTrue(manager > kassierer);
    }
}