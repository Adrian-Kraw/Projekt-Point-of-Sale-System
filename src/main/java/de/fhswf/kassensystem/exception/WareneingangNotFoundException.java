package de.fhswf.kassensystem.exception;

/**
 * Wird geworfen, wenn ein Wareneingang nicht gefunden werden konnte.
 *
 * @author Paula Martin
 */
public class WareneingangNotFoundException extends KassensystemException {
    public WareneingangNotFoundException(Long id) {
        super("Der Wareneingang " + id + " existiert nicht");
    }
}
