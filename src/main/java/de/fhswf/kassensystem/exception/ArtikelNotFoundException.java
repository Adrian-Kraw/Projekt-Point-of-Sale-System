package de.fhswf.kassensystem.exception;

public class ArtikelNotFoundException extends KassensystemException {
    public ArtikelNotFoundException(Long id) {
        super("Artikel mit ID " + id + " nicht gefunden.");
    }
}
