package de.fhswf.kassensystem.exception;

public class WareneingangNotFoundException extends KassensystemException {
    public WareneingangNotFoundException(Long id) {
        super("Der Wareneingang " + id + " existiert nicht");
    }
}
