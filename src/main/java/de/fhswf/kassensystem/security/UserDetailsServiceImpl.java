package de.fhswf.kassensystem.security;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lädt Benutzer aus der Datenbank für Spring Security.
 * Wird von SecurityConfig als AuthenticationProvider verwendet.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String benutzername)
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