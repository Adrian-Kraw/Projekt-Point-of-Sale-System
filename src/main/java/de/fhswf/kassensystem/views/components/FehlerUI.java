package de.fhswf.kassensystem.views.components;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import de.fhswf.kassensystem.exception.KassensystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zentrale Hilfsklasse für einheitliche Fehler- und Erfolgsmeldungen im Frontend.
 *
 * Unterscheidet zwei Fehlertypen:
 *  - KassensystemException: fachlicher Fehler, Nachricht ist nutzerfreundlich → direkt anzeigen
 *  - Exception: technischer/unerwarteter Fehler → generische Meldung anzeigen
 *
 * Verwendung in BaseDialog.onSpeichern():
 *   return FehlerUI.versuch(() -> {
 *       service.doSomething();
 *       FehlerUI.erfolg("Gespeichert.");
 *   });
 *
 * Verwendung in View-Methoden (void):
 *   try {
 *       service.doSomething();
 *   } catch (KassensystemException ex) {
 *       FehlerUI.fehler(ex.getMessage());
 *   } catch (Exception ex) {
 *       FehlerUI.technischerFehler(ex);
 *   }
 *
 * @author Adrian Krawietz
 */
public final class FehlerUI {

    private static final Logger log = LoggerFactory.getLogger(FehlerUI.class);

    private static final int DAUER_FEHLER = 4000;
    private static final int DAUER_ERFOLG = 3000;

    private FehlerUI() {}

    /**
     * Zeigt eine rote Fehler-Notification mit der gegebenen Nachricht.
     * Für KassensystemExceptions, deren getMessage() bereits nutzerfreundlich ist.
     */
    public static void fehler(String nachricht) {
        Notification n = Notification.show(nachricht, DAUER_FEHLER, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Zeigt eine rote Fehler-Notification für unerwartete technische Fehler.
     * Zeigt dem Nutzer eine generische Meldung statt interner Details.
     *
     * <p> Der vollständige Stack-Trace wird über SLF4J geloggt, damit technische Fehler im
     * Betrieb nachvollzogen werden können.
     * </p>
     *
     * @param ex die aufgetretene Exception
     */
    public static void technischerFehler(Exception ex) {
        log.error("Unerwarteter technischer Fehler", ex);
        Notification n = Notification.show(
                "Ein unerwarteter Fehler ist aufgetreten. Bitte versuche es erneut.",
                DAUER_FEHLER, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Zeigt eine grüne Erfolgs-Notification.
     */
    public static void erfolg(String nachricht) {
        Notification n = Notification.show(nachricht, DAUER_ERFOLG, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Führt eine Aktion aus und fängt alle Exceptions zentral ab.
     * Gibt true zurück bei Erfolg, false bei Fehler.
     *
     * Ideal für BaseDialog.onSpeichern():
     *   return FehlerUI.versuch(() -> {
     *       service.speichern(entity);
     *       FehlerUI.erfolg("Gespeichert.");
     *   });
     */
    public static boolean versuch(Runnable aktion) {
        try {
            aktion.run();
            return true;
        } catch (KassensystemException ex) {
            fehler(ex.getMessage());
        } catch (Exception ex) {
            technischerFehler(ex);
        }
        return false;
    }
}