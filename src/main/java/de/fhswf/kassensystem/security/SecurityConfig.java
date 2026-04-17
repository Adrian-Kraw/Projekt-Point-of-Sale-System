package de.fhswf.kassensystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Konfigurationsklasse für Spring Security.
 *
 * <p>Die HTTP-Zugriffskontrolle wird vollständig an Vaadin delegiert.
 * Vaadin wertet die Annotations {@code @AnonymousAllowed}, {@code @RolesAllowed}
 * und {@code @PermitAll} auf den Views selbst aus – Spring Security erlaubt
 * daher alle Requests durch ({@code anyRequest().permitAll()}).
 *
 * <p>Spring Security stellt folgende Funktionalität bereit:
 * <ul>
 *   <li>Login-Formular unter {@code /login} mit Weiterleitung zum Dashboard</li>
 *   <li>Logout unter {@code /logout} mit Session-Invalidierung und Cookie-Löschung</li>
 *   <li>{@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}
 *       mit Stärke 12 für die Passwort-Verarbeitung im {@code UserDetailsService}</li>
 * </ul>
 *
 * <p>CSRF ist deaktiviert, da Vaadin einen eigenen CSRF-Schutz mitbringt.
 *
 * @author Adrian
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // Vaadin übernimmt Zugriffskontrolle
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}