package de.fhswf.kassensystem.exception;

/**
 * Basisklasse für alle fachlichen Exceptions im Kassensystem.
 *
 * @author Paula Martin
 */
public class KassensystemException extends RuntimeException {
    public KassensystemException(String message) {
        super(message);
    }
}
