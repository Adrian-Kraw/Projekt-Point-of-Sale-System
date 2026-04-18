package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.exception.BenutzernameExistiertException;
import de.fhswf.kassensystem.exception.UserNotFoundException;
import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service für die Benutzerverwaltung im Kassensystem.
 *
 * <p>
 *     Kapselt die Geschäftslogik für das Anlegen, Bearbeiten und Deaktivieren von Benutzern sowie das
 *     Zurücksetzen von Passwörtern. Passwörter werden nie im Klartext gespeichert.
 * </p>
 *
 * @author Paula Martin
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Erstellt eine neue Instanz.
     *
     * @param userRepository Repository für den Datenbankzugriff auf Benutzer
     * @param passwordEncoder Encoder für die BCrypt-Passwortverschlüsselung
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Legt einen neuen Benutzer an und speichert ihn in der Datenbank.
     *
     * <p>
     *     Das übergebene Klartextpasswort wird vor der Persistierung mit BCrypt gehasht.
     * </p>
     *
     * @param user der anzulegende Benutzer mit Klartextpasswort
     * @return der gespeicherte Benutzer inklusive generierter ID
     */
    public User createUser(User user) {
        validiereUser(user);

        if (userRepository.findByBenutzername(user.getBenutzername()) != null) {
            throw new BenutzernameExistiertException(user.getBenutzername());
        }
        // Passwort mit BCrypt hashen bevor es gespeichert wird
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Aktualisiert einen bestehenden Benutzer in der Datenbank.
     *
     * @param user der zu aktualisierende Benutzer
     * @return der aktualisierte Benutzer
     */
    public User updateUser(User user) {
        validiereUser(user);
        if (user.getId() == null) {
            throw new IllegalArgumentException("Benutzer ID darf beim Update nicht null sein.");
        }

        userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException(user.getId()));

        return userRepository.save(user);
    }

    /**
     * Deaktiviert einen Benutzer.
     *
     * @param id die ID des zu deaktivierenden Benutzers
     */
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id));

        user.setAktiv(false);
        userRepository.save(user);
    }

    /**
     * Setzt das Passwort eines Benutzers zurück.
     *
     * <p>
     *     Das neue Passwort wird vor der Persistierung mit BCrypt gehasht.
     * </p>
     *
     * @param id die ID des Benutzers, dessen Passwort zurückgesetzt werden soll
     * @param neuesPasswort das neue Passwort im Klartext
     */
    public void resetPasswort(Long id, String neuesPasswort) {
        if (neuesPasswort == null || neuesPasswort.isBlank()) {
            throw new IllegalArgumentException("Neues Passwort darf nicht leer sein.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setPassword(passwordEncoder.encode(neuesPasswort));
        userRepository.save(user);
    }

    /**
     * Gibt alle Benutzer zurück.
     *
     * @return Liste aller Benutzer, oder eine leere Liste, wenn keine vorhanden sind.
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Validiert einen Benutzer auf Pflichtfelder.
     *
     * @param user der zu validierende User
     */
    private void validiereUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Benutzer darf nicht null sein.");
        }
        if (user.getBenutzername() == null) {
            throw new IllegalArgumentException("Benutzername darf nicht null sein.");
        }
        if (user.getBenutzername().isBlank()) {
            throw new IllegalArgumentException("Benutzername darf nicht leer sein.");
        }
        if (user.getName() == null) {
            throw new IllegalArgumentException(("Name darf nicht null sein."));
        }
        if (user.getName().isBlank()) {
            throw new IllegalArgumentException(("Name darf nicht leer sein."));
        }
        if (user.getRolle() == null) {
            throw new IllegalArgumentException("Benutzername muss eine Rolle haben.");
        }
    }
}