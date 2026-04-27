package de.fhswf.kassensystem.broadcast;

import com.vaadin.flow.shared.Registration;
import de.fhswf.kassensystem.exception.BroadcastListenerException;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.exception.UngueltigeEingabeException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zentraler Event-Bus für Live-Updates zwischen Browser-Tabs.
 *
 * Wenn sich Daten ändern (z.B. Verkauf abgeschlossen, Lieferung bestätigt),
 * wird über broadcast() ein Event-String gesendet. Alle registrierten Listener
 * (offene Views) werden benachrichtigt und können ihre Daten neu laden.
 *
 * Verfügbare Events:
 *   "bestand-geaendert"  – nach Verkauf oder Lieferungsbestätigung
 *   "lager-geaendert"    – nach Wareneingang, Bestätigung oder Stornierung
 *
 * @author Adrian Krawietz, Paula Martin
 */
public class Broadcaster {

    private static final Logger logger = Logger.getLogger(Broadcaster.class.getName());
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final LinkedList<Consumer<String>> listeners = new LinkedList<>();

    private Broadcaster() {}

    /**
     * Registriert einen Listener der bei jedem broadcast() aufgerufen wird.
     * Gibt eine Registration zurück die beim Detach der View entfernt werden muss.
     *
     * @param listener Callback der den Event-String empfängt; darf nicht null sein
     * @return Registration zum Entfernen in onDetach()
     * @throws IllegalArgumentException wenn listener null ist
     * @throws KassensystemException    wenn das Registrieren intern fehlschlägt
     */
    public static synchronized Registration register(Consumer<String> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener darf nicht null sein");
        }
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            throw new KassensystemException(
                    "Broadcaster-Listener konnte nicht registriert werden: " + ex.getMessage());
        }
        return () -> {
            synchronized (Broadcaster.class) {
                try {
                    listeners.remove(listener);
                } catch (Exception ex) {
                    throw new KassensystemException(
                            "Broadcaster-Listener konnte nicht entfernt werden: " + ex.getMessage());
                }
            }
        };
    }

    /**
     * Sendet ein Event an alle registrierten Listener.
     * Wird in einem eigenen Thread ausgeführt damit der aufrufende Thread nicht blockiert.
     *
     * <p>Die Listener-Liste wird vor dem Ausführen kopiert, damit der Lock
     * nicht während des Aufrufs gehalten wird. Exceptions einzelner Listener
     * werden als {@link BroadcastListenerException} weitergeworfen und unterbrechen
     * nicht die Benachrichtigung der übrigen.
     *
     * @param event der Event-String (z.B. "bestand-geaendert")
     * @throws UngueltigeEingabeException wenn event null ist
     */
    public static void broadcast(String event) {
        if (event == null) {
            throw new UngueltigeEingabeException("broadcast() darf nicht mit null aufgerufen werden.");
        }

        List<Consumer<String>> snapshot;
        synchronized (Broadcaster.class) {
            snapshot = new ArrayList<>(listeners);
        }

        for (Consumer<String> listener : snapshot) {
            executor.execute(() -> {
                try {
                    listener.accept(event);
                } catch (Exception ex) {
                    BroadcastListenerException ble = new BroadcastListenerException(event, ex);
                    logger.log(Level.WARNING, ble.getMessage(), ble);
                }
            });
        }
    }
}