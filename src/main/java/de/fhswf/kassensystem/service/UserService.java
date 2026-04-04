package de.fhswf.kassensystem.service;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
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

        user.setPassword(neuesPasswort);
        userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Long id){
        return userRepository.getUserById(id);
    }
}
