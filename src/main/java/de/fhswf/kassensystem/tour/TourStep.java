package de.fhswf.kassensystem.tour;

/**
 * Repräsentiert einen einzelnen Schritt der Onboarding-Tour.
 *
 * @param targetSelector CSS-Selektor des Elements. Null/leer = kein Spotlight.
 * @param title          Überschrift
 * @param description    Beschreibungstext (\n wird zu <br>)
 * @param position       "top"|"bottom"|"left"|"right"|"center"|"dialog-left"
 *                       "center"      → Tooltip immer mittig, Spotlight zeigt wenn targetSelector gesetzt
 *                       "dialog-left" → Tooltip links neben dem offenen Vaadin-Dialog
 * @param action         Optionale Aktion vor dem Step:
 *                       "navigate:dashboard|kassieren|lager",
 *                       "demo-verkauf", "open-zahlungsdialog", "open-quittungsdialog"
 */
public record TourStep(
        String targetSelector,
        String title,
        String description,
        String position,
        String action
) {
    public TourStep(String targetSelector, String title, String description, String position) {
        this(targetSelector, title, description, position, null);
    }

    /** Kein Ziel-Element → kein Spotlight. */
    public boolean hasTarget() {
        return targetSelector != null && !targetSelector.isBlank();
    }

    /** Tooltip soll mittig erscheinen (unabhängig ob Spotlight vorhanden). */
    public boolean tooltipCentered() {
        return ("center".equals(position) || !hasTarget()) && !"dialog-left".equals(position);
    }

    public boolean hasAction() {
        return action != null && !action.isBlank();
    }
}