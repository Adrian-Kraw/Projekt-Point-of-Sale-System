package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.User;
import de.fhswf.kassensystem.model.enums.Rolle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setBenutzername("testkassierer");
        user.setName("Test Kassierer");
        user.setPassword("gehasht");
        user.setRolle(Rolle.KASSIERER);
        user.setAktiv(true);
        entityManager.persist(user);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByBenutzername")
    class FindByBenutzername {

        @Test
        @DisplayName("Gibt Benutzer zurück wenn Benutzername existiert")
        void findByBenutzername_erfolg() {
            User result = userRepository.findByBenutzername("testkassierer");

            assertThat(result).isNotNull();
            assertThat(result.getBenutzername()).isEqualTo("testkassierer");
            assertThat(result.getRolle()).isEqualTo(Rolle.KASSIERER);
        }

        @Test
        @DisplayName("Gibt null zurück wenn Benutzername nicht existiert")
        void findByBenutzername_nichtGefunden() {
            User result = userRepository.findByBenutzername("nichtvorhanden");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Suche ist case-sensitive")
        void findByBenutzername_caseSensitive() {
            User result = userRepository.findByBenutzername("TestKassierer");

            assertThat(result).isNull();
        }
    }
}