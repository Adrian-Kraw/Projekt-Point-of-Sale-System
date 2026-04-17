package de.fhswf.kassensystem.tour;

/**
 * Repräsentiert einen einzelnen Schritt der Onboarding-Tour.
 *
 * @param targetSelector CSS-Selektor des Elements. Null/leer = kein Spotlight.
 * @param title          Überschrift
 * @param description    Beschreibungstext (wird zu)<br>
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
    /**
     * Kurzform ohne Aktion – für reine Erklärungs-Steps ohne Navigation oder Dialog.
     *
     * @param targetSelector CSS-Selektor des hervorzuhebenden Elements (oder {@code null})
     * @param title          Überschrift des Tooltip
     * @param description    Beschreibungstext ({@code \n} wird zu {@code <br>})
     * @param position       Tooltip-Position ("top", "bottom", "left", "right", "center", "dialog-left")
     */
    public TourStep(String targetSelector, String title, String description, String position) {
        this(targetSelector, title, description, position, null);
    }

    /**
     * Prüft ob ein Ziel-Element für den Spotlight definiert ist.
     *
     * @return {@code true} wenn {@code targetSelector} gesetzt und nicht leer ist
     */
    public boolean hasTarget() {
        return targetSelector != null && !targetSelector.isBlank();
    }

    /**
     * Prüft ob der Tooltip zentriert dargestellt werden soll.
     * Gilt für {@code position = "center"} sowie für Steps ohne Ziel-Element,
     * ausgenommen {@code "dialog-left"}-Steps.
     *
     * @return {@code true} wenn der Tooltip bildschirmmittig positioniert wird
     */
    public boolean tooltipCentered() {
        return ("center".equals(position) || !hasTarget()) && !"dialog-left".equals(position);
    }

    /**
     * Prüft, ob dieser Step eine Aktion ausführt (Navigation, Demo oder Dialog öffnen).
     *
     * @return {@code true} wenn {@code action} gesetzt und nicht leer ist
     */
    public boolean hasAction() {
        return action != null && !action.isBlank();
    }
}