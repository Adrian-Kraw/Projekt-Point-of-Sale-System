package de.fhswf.kassensystem.exception;

/**
 * Wird geworfen, wenn ein Broadcaster-Listener bei der Verarbeitung
 * eines Events eine unkontrollierte Exception wirft.
 *
 * @author Adrian Krawietz,
 */
public class BroadcastListenerException extends KassensystemException {
    public BroadcastListenerException(String event, Throwable cause) {
        super("Fehler in Broadcaster-Listener beim Event '" + event + "': " + cause.getMessage());
    }
}