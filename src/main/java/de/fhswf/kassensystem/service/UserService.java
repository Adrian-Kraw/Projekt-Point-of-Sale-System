package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
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
        return userRepository.save(user);
    }

    /**
     * Deaktiviert einen Benutzer.
     *
     * @param id die ID des zu deaktivierenden Benutzers
     */
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(("User nicht gefunden.")));

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
}