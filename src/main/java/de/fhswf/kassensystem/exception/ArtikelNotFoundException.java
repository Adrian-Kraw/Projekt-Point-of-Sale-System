package de.fhswf.kassensystem.exception;

/**
 * Wird geworfen, wenn ein Artikel mit der angegebenen ID nicht in der Datenbank
 * gefunden wurde.
 *
 * @author Paula Martin
 */
public class ArtikelNotFoundException extends KassensystemException {
    public ArtikelNotFoundException(Long id) {
        super("Artikel mit ID " + id + " nicht gefunden.");
    }
}
