package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.BenutzernameExistiertException;
import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import de.fhswf.kassensystem.exception.UserNotFoundException;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import de.fhswf.kassensystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setBenutzername("testkassierer");
        testUser.setName("Test Kassierer");
        testUser.setPassword("passwort123");
        testUser.setRolle(Rolle.KASSIERER);
        testUser.setAktiv(true);
    }

    // -------------------------------------------------------------------------
    // createUser
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("Speichert Benutzer mit gehashetem Passwort")
        void createUser_erfolg() {
            when(userRepository.findByBenutzername("testkassierer")).thenReturn(null);
            when(passwordEncoder.encode("passwort123")).thenReturn("gehasht");
            when(userRepository.save(testUser)).thenReturn(testUser);

            User result = userService.createUser(testUser);

            assertThat(result).isEqualTo(testUser);
            assertThat(testUser.getPassword()).isEqualTo("gehasht");
            verify(passwordEncoder).encode("passwort123");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Wirft BenutzernameExistiertException wenn Benutzername vergeben")
        void createUser_benutzernameVergeben() {
            when(userRepository.findByBenutzername("testkassierer"))
                    .thenReturn(testUser);

            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(BenutzernameExistiertException.class);

            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn User null ist")
        void createUser_nullUser() {
            assertThatThrownBy(() -> userService.createUser(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Benutzername null ist")
        void createUser_benutzernameNull() {
            testUser.setBenutzername(null);

            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Benutzername leer ist")
        void createUser_benutzername_leer() {
            testUser.setBenutzername("   ");

            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("leer");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Name null ist")
        void createUser_nameNull() {
            testUser.setName(null);

            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Name leer ist")
        void createUser_nameLeer() {
            testUser.setName("   ");

            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("leer");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Rolle null ist")
        void createUser_rolleNull() {
            testUser.setRolle(null);

            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rolle");

            verify(userRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // updateUser
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("Aktualisiert Benutzer erfolgreich")
        void updateUser_erfolg() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);

            User result = userService.updateUser(testUser);

            assertThat(result).isEqualTo(testUser);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Wirft UserNotFoundException wenn Benutzer nicht existiert")
        void updateUser_nichtGefunden() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(testUser))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn User null ist")
        void updateUser_nullUser() {
            assertThatThrownBy(() -> userService.updateUser(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn ID null ist")
        void updateUser_idNull() {
            testUser.setId(null);

            assertThatThrownBy(() -> userService.updateUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Benutzername null ist")
        void updateUser_benutzernameNull() {
            testUser.setBenutzername(null);

            assertThatThrownBy(() -> userService.updateUser(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Benutzername leer ist")
        void updateUser_benutzername_leer() {
            testUser.setBenutzername("   ");

            assertThatThrownBy(() -> userService.updateUser(testUser))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("leer");

            verify(userRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deactivateUser
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deactivateUser")
    class DeactivateUser {

        @Test
        @DisplayName("Setzt Benutzer auf inaktiv")
        void deactivateUser_erfolg() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            userService.deactivateUser(1L);

            assertThat(testUser.isAktiv()).isFalse();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Wirft UserNotFoundException wenn Benutzer nicht existiert")
        void deactivateUser_nichtGefunden() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deactivateUser(99L))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // resetPasswort
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("resetPasswort")
    class ResetPasswort {

        @Test
        @DisplayName("Setzt Passwort mit BCrypt-Hash zurück")
        void resetPasswort_erfolg() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("neuesPasswort")).thenReturn("neuerHash");

            userService.resetPasswort(1L, "neuesPasswort");

            assertThat(testUser.getPassword()).isEqualTo("neuerHash");
            verify(passwordEncoder).encode("neuesPasswort");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Wirft UserNotFoundException wenn Benutzer nicht existiert")
        void resetPasswort_nichtGefunden() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.resetPasswort(99L, "neuesPasswort"))
                    .isInstanceOf(UserNotFoundException.class);

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft IllegalArgumentException wenn Passwort null ist")
        void resetPasswort_passwortNull() {
            assertThatThrownBy(() -> userService.resetPasswort(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wirft UngueltigeEingabeException wenn Passwort leer ist")
        void resetPasswort_passwortLeer() {
            assertThatThrownBy(() -> userService.resetPasswort(1L, "   "))
                    .isInstanceOf(UngueltigeEingabeException.class)
                    .hasMessageContaining("leer");

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // findAllUsers
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findAllUsers")
    class FindAllUsers {

        @Test
        @DisplayName("Gibt alle Benutzer zurück")
        void findAllUsers_erfolg() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<User> result = userService.findAllUsers();

            assertThat(result).hasSize(1).contains(testUser);
        }

        @Test
        @DisplayName("Gibt leere Liste zurück wenn keine Benutzer vorhanden")
        void findAllUsers_leereListe() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<User> result = userService.findAllUsers();

            assertThat(result).isEmpty();
        }
    }
}