package de.fhswf.kassensystem.security;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für {@link SecurityUtils}.
 *
 * <p>Das {@link UserRepository} wird per Mockito gemockt damit keine
 * Datenbankverbindung benötigt wird. Der {@link SecurityContextHolder}
 * wird vor jedem Test geleert um Zustandsübertragung zwischen Tests zu verhindern.
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
     * Kein Authentication-Objekt im SecurityContext: null wird erwartet.
     */
    @Test
    void getEingeloggterUser_NichtEingeloggt_GibtNullZurueck() {
        assertNull(securityUtils.getEingeloggterUser());
    }

    /**
     * Gültige Authentifizierung und Benutzer in DB: das User-Objekt wird erwartet.
     *
     * <p>Der dritte Parameter (leere Authority-Liste) ist notwendig damit
     * isAuthenticated() true zurückgibt.
     */
    @Test
    void getEingeloggterUser_EingeloggtUndGefunden_GibtUserZurueck() {
        User user = new User();
        user.setBenutzername("max");
        when(userRepository.findByBenutzername("max")).thenReturn(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("max", null, List.of()));

        User result = securityUtils.getEingeloggterUser();

        assertNotNull(result);
        assertEquals("max", result.getBenutzername());
    }

    /**
     * Authentifizierung vorhanden, aber Benutzer nicht in DB: null wird erwartet.
     */
    @Test
    void getEingeloggterUser_BenutzerNichtInDB_GibtNullZurueck() {
        when(userRepository.findByBenutzername("ghost")).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ghost", null, List.of()));

        assertNull(securityUtils.getEingeloggterUser());
    }

    /**
     * Anonyme Authentifizierung (nicht eingeloggt): null wird erwartet.
     * AnonymousAuthenticationToken gilt als nicht authentifiziert.
     */
    @Test
    void getEingeloggterUser_AnonymerBenutzer_GibtNullZurueck() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertNull(securityUtils.getEingeloggterUser());
    }

    /**
     * Benutzer mit leerem Benutzernamen: kein Treffer in DB, null wird erwartet.
     */
    @Test
    void getEingeloggterUser_LeererBenutzername_GibtNullZurueck() {
        when(userRepository.findByBenutzername("")).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("", null, List.of()));

        assertNull(securityUtils.getEingeloggterUser());
    }
}