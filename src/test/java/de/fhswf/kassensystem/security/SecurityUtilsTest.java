package de.fhswf.kassensystem.security;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests für {@link SecurityUtils}.
 *
 * <p>Das {@link UserRepository} wird per Mockito gemockt damit keine
 * Datenbankverbindung benötigt wird. Der {@link SecurityContextHolder}
 * wird vor jedem Test geleert um Zustandsübertragung zwischen Tests zu verhindern.</p>
 */
class SecurityUtilsTest {

    private UserRepository userRepository;
    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        securityUtils = new SecurityUtils(userRepository);
        SecurityContextHolder.clearContext();
    }

    /**
     * Kein Authentication-Objekt im SecurityContext — leeres Optional wird erwartet.
     */
    @Test
    @DisplayName("Gibt leeres Optional zurück wenn kein Sicherheitskontext vorhanden")
    void getEingeloggterUser_nichtEingeloggt() {
        Optional<User> result = securityUtils.getEingeloggterUser();

        assertThat(result).isEmpty();
    }

    /**
     * Gültige Authentifizierung und Benutzer in DB — das User-Objekt wird erwartet.
     */
    @Test
    @DisplayName("Gibt User zurück wenn Benutzer eingeloggt und in DB gefunden")
    void getEingeloggterUser_eingeloggtUndGefunden() {
        User user = new User();
        user.setBenutzername("max");
        when(userRepository.findByBenutzername("max")).thenReturn(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("max", null, List.of()));

        Optional<User> result = securityUtils.getEingeloggterUser();

        assertThat(result).isPresent();
        assertThat(result.get().getBenutzername()).isEqualTo("max");
    }

    /**
     * Authentifizierung vorhanden aber Benutzer nicht in DB — leeres Optional wird erwartet.
     */
    @Test
    @DisplayName("Gibt leeres Optional zurück wenn Benutzer nicht in DB gefunden")
    void getEingeloggterUser_benutzerNichtInDb() {
        when(userRepository.findByBenutzername("ghost")).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ghost", null, List.of()));

        Optional<User> result = securityUtils.getEingeloggterUser();

        assertThat(result).isEmpty();
    }

    /**
     * Anonyme Authentifizierung — leeres Optional wird erwartet.
     */
    @Test
    @DisplayName("Gibt leeres Optional zurück wenn anonymer Benutzer")
    void getEingeloggterUser_anonyerBenutzer() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        Optional<User> result = securityUtils.getEingeloggterUser();

        assertThat(result).isEmpty();
    }

    /**
     * Benutzer mit leerem Benutzernamen — leeres Optional wird erwartet.
     */
    @Test
    @DisplayName("Gibt leeres Optional zurück wenn Benutzername leer ist")
    void getEingeloggterUser_leererBenutzername() {
        when(userRepository.findByBenutzername("")).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("", null, List.of()));

        Optional<User> result = securityUtils.getEingeloggterUser();

        assertThat(result).isEmpty();
    }
}