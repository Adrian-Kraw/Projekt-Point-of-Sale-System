package de.fhswf.kassensystem.model.enums;

/**
 * Definiert die verfügbaren Benutzerrollen im Kassensystem.
 *
 * <p>
 *     Die Rolle eines Benutzers bestimmt seine Zugriffsrechte im Kassensystem und wird bei der Authentifizierung
 *     über Spring Security ausgewertet.
 * </p>
 *
 * @author Paula Martin
 */
public enum Rolle {
    KASSIERER,
    MANAGER
}
