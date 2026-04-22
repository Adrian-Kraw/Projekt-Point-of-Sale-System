package de.fhswf.kassensystem.tour;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für {@link TourStep}.
 *
 * <p>Prüft die drei Hilfsmethoden des Records:
 * {@link TourStep#hasTarget()}, {@link TourStep#hasAction()}
 * und {@link TourStep#tooltipCentered()} – inklusive Grenzfälle
 * wie leere Strings und nur Leerzeichen.
 *
 * @author Adrian Krawietz
 */
class TourStepTest {

    /**
     * CSS-Selektor ist gesetzt: hasTarget() gibt true zurück.
     */
    @Test
    void hasTarget_MitSelektor_GibtTrueZurueck() {
        TourStep step = new TourStep("[tour-id='test']", "Titel", "Beschreibung", "bottom");
        assertTrue(step.hasTarget());
    }

    /**
     * Kein Selektor (null): hasTarget() gibt false zurück.
     */
    @Test
    void hasTarget_OhneSelektor_GibtFalseZurueck() {
        TourStep step = new TourStep(null, "Titel", "Beschreibung", "center");
        assertFalse(step.hasTarget());
    }

    /**
     * Selektor enthält nur Leerzeichen: hasTarget() gibt false zurück.
     */
    @Test
    void hasTarget_LeereSelektor_GibtFalseZurueck() {
        TourStep step = new TourStep("  ", "Titel", "Beschreibung", "center");
        assertFalse(step.hasTarget());
    }

    /**
     * Selektor ist ein leerer String: hasTarget() gibt false zurück.
     */
    @Test
    void hasTarget_LeererString_GibtFalseZurueck() {
        TourStep step = new TourStep("", "Titel", "Beschreibung", "center");
        assertFalse(step.hasTarget());
    }

    /**
     * Aktion ist gesetzt: hasAction() gibt true zurück.
     */
    @Test
    void hasAction_MitAktion_GibtTrueZurueck() {
        TourStep step = new TourStep(null, "Titel", "Beschreibung", "center", "demo-verkauf");
        assertTrue(step.hasAction());
    }

    /**
     * Kurzform-Konstruktor ohne Aktion: hasAction() gibt false zurück.
     */
    @Test
    void hasAction_OhneAktion_GibtFalseZurueck() {
        TourStep step = new TourStep(null, "Titel", "Beschreibung", "center");
        assertFalse(step.hasAction());
    }

    /**
     * Action ist ein leerer String: hasAction() gibt false zurück.
     */
    @Test
    void hasAction_LeereAction_GibtFalseZurueck() {
        TourStep step = new TourStep(null, "Titel", "Beschreibung", "center", "");
        assertFalse(step.hasAction());
    }

    /**
     * Action enthält nur Leerzeichen: hasAction() gibt false zurück.
     */
    @Test
    void hasAction_ActionNurLeerzeichen_GibtFalseZurueck() {
        TourStep step = new TourStep(null, "Titel", "Beschreibung", "center", "   ");
        assertFalse(step.hasAction());
    }

    /**
     * Position ist "center": tooltipCentered() gibt true zurück.
     */
    @Test
    void tooltipCentered_MitCenterPosition_GibtTrueZurueck() {
        TourStep step = new TourStep("[tour-id='test']", "Titel", "Beschreibung", "center");
        assertTrue(step.tooltipCentered());
    }

    /**
     * Kein Ziel-Element und Position ist nicht "dialog-left":
     * tooltipCentered() gibt true zurück.
     */
    @Test
    void tooltipCentered_OhneTargetUndNichtDialogLeft_GibtTrueZurueck() {
        TourStep step = new TourStep(null, "Titel", "Beschreibung", "bottom");
        assertTrue(step.tooltipCentered());
    }

    /**
     * Position ist "dialog-left": tooltipCentered() gibt false zurück,
     * da der Tooltip neben dem Dialog positioniert wird.
     */
    @Test
    void tooltipCentered_DialogLeft_GibtFalseZurueck() {
        TourStep step = new TourStep(null, "Titel", "Beschreibung", "dialog-left");
        assertFalse(step.tooltipCentered());
    }
}