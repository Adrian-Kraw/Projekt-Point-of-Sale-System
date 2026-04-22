package de.fhswf.kassensystem.security;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für {@link UserDetailsServiceImpl}.
 *
 * <p>Prüft das korrekte Laden von Benutzern für Spring Security:
 * erfolgreiche Fälle (Kassierer, Manager), Fehlerfälle
 * (Benutzer nicht gefunden, deaktiviert) sowie Grenzfälle
 * (leerer Benutzername, Leerzeichen).
 *
 * <p>Das {@link UserRepository} wird per Mockito gemockt.
 *
 * @author Adrian Krawietz
 */
class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new UserDetailsServiceImpl(userRepository);
    }

    /**
     * Aktiver Kassierer in DB: UserDetails mit korrektem Benutzernamen
     * und Authority ROLE_KASSIERER wird erwartet.
     */
    @Test
    void loadUserByUsername_BenutzerGefunden_GibtUserDetailsZurueck() {
        User user = new User();
        user.setBenutzername("max");
        user.setPassword("hashed");
        user.setRolle(Rolle.KASSIERER);
        user.setAktiv(true);
        when(userRepository.findByBenutzername("max")).thenReturn(user);

        UserDetails result = service.loadUserByUsername("max");

        assertEquals("max", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_KASSIERER")));
    }

    /**
     * Aktiver Manager in DB: Authority ROLE_MANAGER wird erwartet.
     */
    @Test
    void loadUserByUsername_ManagerRolle_GibtROLE_MANAGERZurueck() {
        User user = new User();
        user.setBenutzername("chef");
        user.setPassword("hashed");
        user.setRolle(Rolle.MANAGER);
        user.setAktiv(true);
        when(userRepository.findByBenutzername("chef")).thenReturn(user);

        UserDetails result = service.loadUserByUsername("chef");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }

    /**
     * Benutzer nicht in DB: eine {@link UsernameNotFoundException} wird erwartet.
     */
    @Test
    void loadUserByUsername_BenutzerNichtGefunden_WirftException() {
        when(userRepository.findByBenutzername("unbekannt")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unbekannt"));
    }

    /**
     * Benutzer existiert aber ist deaktiviert: eine {@link UsernameNotFoundException}
     * wird erwartet. Spring Security wertet dies als fehlgeschlagenen Login-Versuch.
     */
    @Test
    void loadUserByUsername_BenutzerDeaktiviert_WirftException() {
        User user = new User();
        user.setBenutzername("inaktiv");
        user.setPassword("hashed");
        user.setRolle(Rolle.KASSIERER);
        user.setAktiv(false);
        when(userRepository.findByBenutzername("inaktiv")).thenReturn(user);

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("inaktiv"));
    }

    /**
     * Leerer Benutzername: kein Benutzer in DB, eine {@link UsernameNotFoundException}
     * wird erwartet.
     */
    @Test
    void loadUserByUsername_LeererBenutzername_WirftException() {
        when(userRepository.findByBenutzername("")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(""));
    }

    /**
     * Benutzername mit nur Leerzeichen: kein Benutzer in DB, eine
     * {@link UsernameNotFoundException} wird erwartet.
     */
    @Test
    void loadUserByUsername_BenutzernameNurLeerzeichen_WirftException() {
        when(userRepository.findByBenutzername("   ")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("   "));
    }
}