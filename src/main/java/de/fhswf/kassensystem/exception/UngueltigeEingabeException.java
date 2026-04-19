package de.fhswf.kassensystem.exception;

/**
 * Wird geworfen, wenn ein Nutzer ungültige Eingaben gemacht hat, die fachlich
 * nicht akzeptiert werden können.
 *
 * @author Paula Martin
 */
public class UngueltigeEingabeException extends KassensystemException {
    public UngueltigeEingabeException(String message) {
        super(message);
    }
}
