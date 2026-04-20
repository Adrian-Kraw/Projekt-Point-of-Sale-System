package de.fhswf.kassensystem.security;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Hilfsklasse für sicherheitsbezogene Hilfsmethoden.
 *
 * <p>Stellt eine zentrale Methode bereit, um den aktuell eingeloggten
 * {@link de.fhswf.kassensystem.model.User} aus dem {@code SecurityContext}
 * zu ermitteln. Dadurch müssen Views und Services den Benutzernamen nicht
 * selbst aus dem {@code SecurityContextHolder} auslesen.
 *
 * @author Paula & Adrian
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
     * <p>Liest den Benutzernamen aus dem {@code SecurityContext} von Spring Security
     * und lädt den zugehörigen {@link User} aus der Datenbank.
     *
     * @return der aktuell authentifizierte {@link User}, oder {@code null}
     *         wenn keine aktive Authentifizierung vorliegt oder
     *         wenn kein Benutzer mit diesem Namen gefunden wurde
     */
    public Optional<User> getEingeloggterUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return Optional.ofNullable(userRepository.findByBenutzername(auth.getName()));
    }
}
