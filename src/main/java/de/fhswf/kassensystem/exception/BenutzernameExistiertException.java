package de.fhswf.kassensystem.exception;

/**
 * Wird geworfen, wenn beim Anlegen eines neuen Benutzers der angegebene Benutzername
 * bereits vergeben ist.
 *
 * @author Paula Martin
 */
public class BenutzernameExistiertException extends KassensystemException {
    public BenutzernameExistiertException(String benutzername) {
        super("Benutzername '" + benutzername + "' ist bereits vergeben.");
    }
}
