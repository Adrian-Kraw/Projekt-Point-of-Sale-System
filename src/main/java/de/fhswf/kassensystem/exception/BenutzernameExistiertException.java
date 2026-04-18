package de.fhswf.kassensystem.exception;

public class BenutzernameExistiertException extends KassensystemException {
    public BenutzernameExistiertException(String benutzername) {
        super("Benutzername '" + benutzername + "' ist bereits vergeben.");
    }
}
