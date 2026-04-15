package de.fhswf.kassensystem.security;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getEingeloggterUser() {
        String benutzername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByBenutzername(benutzername);
    }
}
