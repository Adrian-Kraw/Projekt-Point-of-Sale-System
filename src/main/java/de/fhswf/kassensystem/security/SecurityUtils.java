package de.fhswf.kassensystem.security;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Hilfskomponente für den Zugriff auf sicherheitsrelevante Informationen der aktuellen HTTP-Session.
 *
 * <p>
 *     Kapselt den Zugriff auf den {@link SecurityContextHolder}, um in Services auf den eingeloggten Benutzer
 *     zugreifen zu können, ohne den Spring-Security-Kontext direkt ansprechen zu müssen.
 * </p>
 *
 * @author Paula Martin
 */
@Component
public class SecurityUtils {
    private final UserRepository userRepository;

    /**
     * Erstellt eine neue Instanz mit dem benötigten Repository.
     * @param userRepository Repository für den Datenbankzugriff auf Benutzer
     */
    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gibt den aktuell eingeloggten Benutzer zurück.
     *
     * <p>
     *     Liest den Benutzernamen aus dem {@link SecurityContextHolder} und lädt den zugehörigen {@link User}
     *     aus der Datenbank.
     * </p>
     *
     * #Todo: Fehlerbehandlung
     *
     * @return der aktuell authentifizierte {@link User}
     */
    public User getEingeloggterUser() {
        String benutzername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByBenutzername(benutzername);
    }
}
