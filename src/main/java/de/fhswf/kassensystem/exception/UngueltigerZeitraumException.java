package de.fhswf.kassensystem.exception;

public class UngueltigerZeitraumException extends KassensystemException {
    public UngueltigerZeitraumException(int tage) {
        super("Der angegebene Zeitraum " + tage + " ist nicht gültig.");
    }
}
