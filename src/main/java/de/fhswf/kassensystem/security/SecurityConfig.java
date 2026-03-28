package de.fhswf.kassensystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig konfiguriert Spring Security für den Frontend-Prototyp.
 *
 * Im Prototyp ist Security vollständig deaktiviert – alle Requests
 * sind ohne Login erlaubt. Sobald das Backend mit echter
 * Nutzerverwaltung integriert wird, wird diese Klasse angepasst:
 * - Rollenbasierter Zugriff (Kassierer vs. Manager)
 * - BCrypt-Passwort-Hashing
 * - Session-Management aus unserer Paketeinteilung
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Konfiguriert die Security-Filterchain.
     *
     * anyRequest().permitAll() erlaubt alle Requests ohne Authentifizierung.
     * CSRF und FormLogin sind deaktiviert damit Spring Security nicht
     * automatisch auf /login umleitet.
     *
     * @param http der HttpSecurity-Builder von Spring Security
     * @return die fertig konfigurierte SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}