package de.fhswf.kassensystem.exception;

/**
 * Wird geworfen, wenn ein Wareneingang bereits bestätigt wurde.
 *
 * @author Paula Martin
 */
public class WareneingangBereitsBestaetigt extends KassensystemException {
    public WareneingangBereitsBestaetigt(Long id) {
        super("Wareneingang " + id + " wurde bereits bestätigt.");
    }
}
