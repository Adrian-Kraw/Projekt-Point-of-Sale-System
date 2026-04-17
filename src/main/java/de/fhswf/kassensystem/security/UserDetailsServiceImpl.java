package de.fhswf.kassensystem.security;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.jspecify.annotations.NonNull;
import java.util.List;

/**
 * Implementierung des Spring Security {@link org.springframework.security.core.userdetails.UserDetailsService}.
 *
 * <p>Lädt einen Benutzer anhand seines Benutzernamens aus der Datenbank
 * und wandelt ihn in ein Spring-Security-konformes {@code UserDetails}-Objekt um.
 * Die Rolle wird als {@code GrantedAuthority} in der Form {@code ROLE_KASSIERER}
 * bzw. {@code ROLE_MANAGER} übergeben.
 *
 * <p>Ist der Benutzer nicht vorhanden oder deaktiviert, wird eine
 * {@link org.springframework.security.core.userdetails.UsernameNotFoundException}
 * geworfen, die Spring Security als fehlgeschlagenen Login-Versuch wertet.
 *
 * @author Adrian
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Lädt einen Benutzer anhand seines Benutzernamens für Spring Security.
     *
     * <p>Wird automatisch von Spring Security während des Login-Vorgangs aufgerufen.
     * Der zurückgegebene {@code UserDetails}-Objekt wird anschließend von Spring Security
     * zur Passwortprüfung und Rollen-Zuweisung verwendet.
     *
     * <p>Die Rolle wird als {@code GrantedAuthority} im Format {@code ROLE_KASSIERER}
     * bzw. {@code ROLE_MANAGER} übergeben, damit Spring Security sie per
     * {@code @RolesAllowed} auf den Views auswerten kann.
     *
     * @param benutzername der eingegebene Benutzername aus dem Login-Formular
     * @return ein {@code UserDetails}-Objekt mit Benutzername, Passwort-Hash und Rolle
     * @throws UsernameNotFoundException wenn kein Benutzer mit dem Namen existiert oder der Account deaktiviert ist
     */
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String benutzername)
            throws UsernameNotFoundException {
        User user = userRepository.findByBenutzername(benutzername);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "Benutzer '" + benutzername + "' nicht gefunden.");
        }
        if (!user.isAktiv()) {
            throw new UsernameNotFoundException(
                    "Benutzer '" + benutzername + "' ist deaktiviert.");
        }

        // Rolle als Spring Security GrantedAuthority: "ROLE_KASSIERER" / "ROLE_MANAGER"
        String roleName = "ROLE_" + user.getRolle().name();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getBenutzername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(roleName)))
                .build();
    }
}