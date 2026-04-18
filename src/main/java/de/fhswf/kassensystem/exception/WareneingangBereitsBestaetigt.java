package de.fhswf.kassensystem.exception;

public class WareneingangBereitsBestaetigt extends KassensystemException {
    public WareneingangBereitsBestaetigt(Long id) {
        super("Wareneingang " + id + " wurde bereits bestätigt.");
    }
}
