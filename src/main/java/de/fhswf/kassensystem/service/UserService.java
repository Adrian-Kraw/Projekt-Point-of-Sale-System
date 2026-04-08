package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        // Passwort mit BCrypt hashen bevor es gespeichert wird
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        user.setAktiv(false);
        userRepository.save(user);
    }

    public void resetPasswort(Long id, String neuesPasswort) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(("User nicht gefunden.")));

        user.setPassword(passwordEncoder.encode(neuesPasswort));
        userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Long id){
        return userRepository.getUserById(id);
    }
}