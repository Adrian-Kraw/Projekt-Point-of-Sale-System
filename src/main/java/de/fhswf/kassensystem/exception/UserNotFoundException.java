package de.fhswf.kassensystem.exception;

/**
 * Wird geworfen, wenn der gesuchte Nutzer nicht gefunden werden konnte.
 *
 * @author Paula Martin
 */
public class UserNotFoundException extends KassensystemException {
    public UserNotFoundException(Long id) {
        super("User mit der ID " + id + " konnte nicht gefunden werden.");
    }
}
