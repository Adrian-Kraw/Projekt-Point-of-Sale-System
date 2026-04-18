package de.fhswf.kassensystem.exception;

public class UserNotFoundException extends KassensystemException {
    public UserNotFoundException(Long id) {
        super("User mit der ID " + id + " konnte nicht gefunden werden.");
    }
}
