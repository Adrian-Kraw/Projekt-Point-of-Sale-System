package de.fhswf.kassensystem.exception;

public class BestandUnterschrittenException extends KassensystemException {
    public BestandUnterschrittenException(String artikelName, int verfuegbar) {
        super("Nicht genug Bestand für '" + artikelName +
                "'. Verfügbar: " + verfuegbar);
    }
}
