package de.fhswf.kassensystem.broadcast;

import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final LinkedList<Consumer<String>> listeners = new LinkedList<>();

    private Broadcaster() {}

    /**
     * Registriert einen Listener der bei jedem broadcast() aufgerufen wird.
     * Gibt eine Registration zurück die beim Detach der View entfernt werden muss.
     *
     * @param listener Callback der den Event-String empfängt
     * @return Registration zum Entfernen in onDetach()
     */
    public static synchronized Registration register(Consumer<String> listener) {
        listeners.add(listener);
        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    /**
     * Sendet ein Event an alle registrierten Listener.
     * Wird in einem eigenen Thread ausgeführt damit der aufrufende Thread nicht blockiert.
     *
     * @param event der Event-String (z.B. "bestand-geaendert")
     */
    public static synchronized void broadcast(String event) {
        for (Consumer<String> listener : listeners) {
            executor.execute(() -> listener.accept(event));
        }
    }
}